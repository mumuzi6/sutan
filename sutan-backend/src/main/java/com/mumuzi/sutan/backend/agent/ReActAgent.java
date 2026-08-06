package com.mumuzi.sutan.backend.agent;

import com.mumuzi.sutan.backend.rag.Citation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手写 ReAct 编排器（项目灵魂）。
 *
 * 实现 Reasoning + Acting 经典范式：让 LLM 输出结构化文本
 * （Thought / Action / Action Input / Final Answer），自己解析、调度工具、回流观察，
 * 形成思考-行动-观察闭环。不依赖 Spring AI Function Calling —— 显原理、可讲、可改。
 *
 * 强制溯源：工具调用收集 citations，最终答案若涉及法律却无引用则标记 ungrounded。
 *
 * 灵感参照：OpenManus / 鱼皮 YuManus 的 ReAct 思路，但领域与溯源是诉探独有。
 */
@Service
public class ReActAgent {

    private static final Logger log = LoggerFactory.getLogger(ReActAgent.class);
    private static final int MAX_STEPS = 6;

    private static final Pattern THOUGHT_P = Pattern.compile("Thought:\\s*(.*?)(?=Action:|Final Answer:|$)", Pattern.DOTALL);
    private static final Pattern ACTION_P = Pattern.compile("Action:\\s*(.*?)(?=Action Input:|$)", Pattern.DOTALL);
    private static final Pattern INPUT_P = Pattern.compile("Action Input:\\s*(.*?)(?=Thought:|Final Answer:|$)", Pattern.DOTALL);
    private static final Pattern FINAL_P = Pattern.compile("Final Answer:\\s*(.*)", Pattern.DOTALL);

    private final ChatModel chatModel;
    private final ToolRegistry toolRegistry;
    private final io.micrometer.observation.ObservationRegistry observationRegistry;

    public ReActAgent(ChatModel chatModel, ToolRegistry toolRegistry,
                      io.micrometer.observation.ObservationRegistry observationRegistry) {
        this.chatModel = chatModel;
        this.toolRegistry = toolRegistry;
        this.observationRegistry = observationRegistry;
    }

    /** 运行 Agent，返回完整结果（含步骤与溯源）。 */
    public AgentResult run(String userQuery) {
        return io.micrometer.observation.Observation
                .createNotStarted("react.agent.run", observationRegistry)
                .contextualName("ReAct: " + (userQuery.length() > 50 ? userQuery.substring(0, 50) : userQuery))
                .observe(() -> doRun(userQuery));
    }

    private AgentResult doRun(String userQuery) {
        AgentResult result = new AgentResult();
        String scratchpad = "";

        for (int step = 0; step < MAX_STEPS; step++) {
            String llmOutput = think(userQuery, scratchpad);
            AgentStep s = parse(llmOutput);
            result.getSteps().add(s);

            if (s.isFinalAnswer()) {
                result.setAnswer(s.thought != null ? s.thought : "");
                // 最终答案若含"法/条/罪"等法律词却无任何引用 → 标记未溯源
                groundCheck(result);
                return result;
            }

            Tool tool = toolRegistry.get(s.action);
            if (tool == null) {
                s.observation = "工具 " + s.action + " 不存在，可用工具见系统提示。";
            } else {
                Tool.ToolResult tr = tool.execute(s.actionInput);
                s.observation = tr.observation();
                s.citations = tr.citations();
                result.addCitations(tr.citations());
            }

            scratchpad += formatStep(s) + "\n";
        }

        // 达到上限仍未给出 Final Answer
        result.setAnswer("达到最大推理步数仍未给出最终答案。");
        result.setGrounded(false);
        result.setGroundNote("推理未完成");
        return result;
    }

    /** 让 LLM 思考一步：系统提示 + 工具说明 + 历史思考过程 */
    private String think(String userQuery, String scratchpad) {
        String systemPrompt = buildSystemPrompt();
        String userPrompt = "问题：" + userQuery + "\n\n" + scratchpad + "\n请继续推理。";
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(userPrompt)
        ));
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    private String buildSystemPrompt() {
        return """
                你是"诉探"，面向法考考生的 Agent 导师，擅长法律推理与主观题批改。
                你必须严格使用 ReAct 协议回答，每一步按以下格式输出：

                Thought: 你的思考（分析问题、决定下一步）
                Action: 要调用的工具名（可选工具见下），或
                Final Answer: 直接给出最终答案

                如果要调用工具，格式为：
                Thought: ...
                Action: 工具名
                Action Input: 给工具的自然语言输入

                规则：
                1. 涉及法律规定、法条、案例时，必须先调用 search_law 检索，再作答。
                2. 最终答案中的法律结论必须基于检索到的法条，并在答案中注明出处。
                3. 用户请求批改主观题时，调用 grade_answer。
                4. 最多推理 {MAX_STEPS} 步，得出 Final Answer。

                可用工具：
                """.replace("{MAX_STEPS}", String.valueOf(MAX_STEPS))
                + toolRegistry.toolDescriptions();
    }

    /** 解析 LLM 输出为 AgentStep */
    private AgentStep parse(String output) {
        AgentStep s = new AgentStep();
        Matcher fm = FINAL_P.matcher(output);
        if (fm.find()) {
            s.thought = fm.group(1).trim();
            s.action = "Final Answer";
            return s;
        }
        Matcher tm = THOUGHT_P.matcher(output);
        if (tm.find()) s.thought = tm.group(1).trim();
        Matcher am = ACTION_P.matcher(output);
        if (am.find()) s.action = am.group(1).trim();
        Matcher im = INPUT_P.matcher(output);
        if (im.find()) s.actionInput = im.group(1).trim();
        return s;
    }

    private String formatStep(AgentStep s) {
        return "Thought: " + s.thought + "\nAction: " + s.action
                + "\nAction Input: " + s.actionInput + "\nObservation: " + s.observation;
    }

    /** 强制溯源校验：答案涉及法律却无引用 → 标记 ungrounded */
    private void groundCheck(AgentResult result) {
        if (result.getCitations().isEmpty() && mentionsLaw(result.getAnswer())) {
            result.setGrounded(false);
            result.setGroundNote("答案涉及法律内容但未检索到法条引用，请核对官方文本。");
        }
    }

    private boolean mentionsLaw(String text) {
        if (text == null) return false;
        return text.contains("法条") || text.contains("法律规定") || text.contains("根据刑法")
                || text.contains("根据民法典") || text.contains("构成犯罪") || text.contains("罪");
    }
}
