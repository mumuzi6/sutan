package com.mumuzi.sutan.backend.agent.tools;

import com.mumuzi.sutan.backend.agent.Tool;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 模拟阅卷人工具：评估主观题作答，给出采分点命中/缺失、法条引用、逻辑漏洞。
 * 对标真实阅卷三层标准（扣分→核对采分点→核定得分）。
 * 这是"模拟阅卷人"差异化卖点的内核，也是 Agent 岗面试亮点。
 */
@Component
public class GradeAnswerTool implements Tool {

    private static final String GRADER_SYSTEM_PROMPT = """
            你是法考主观题阅卷人，严格按以下流程评分：
            1. 先排查答案中需要扣分的地方（结论错误、法条引用错误）。
            2. 再对照标准采分点逐一核对命中情况。
            3. 最后核定得分。
            输出：结论、采分点命中清单、缺失项、法条引用问题、逻辑漏洞、改进建议、预估得分(0-100)。
            """;

    private final ChatModel chatModel;

    public GradeAnswerTool(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String name() {
        return "grade_answer";
    }

    @Override
    public String description() {
        return "模拟法考阅卷人批改主观题作答。输入：题目与考生作答；输出：采分点/法条/逻辑/改进建议/得分。"
                + "用户提交主观题作答请求批改时调用。";
    }

    @Override
    public ToolResult execute(String input) {
        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(GRADER_SYSTEM_PROMPT),
                    new UserMessage("请批改以下作答：\n\n" + input)
            ));
            ChatResponse response = chatModel.call(prompt);
            String text = response.getResult().getOutput().getText();
            return ToolResult.of(text);
        } catch (Exception e) {
            return ToolResult.of("阅卷失败：" + e.getMessage());
        }
    }
}
