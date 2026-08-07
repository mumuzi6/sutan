package com.mumuzi.sutan.backend.grade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mumuzi.sutan.backend.rag.Citation;
import com.mumuzi.sutan.backend.rag.RetrievalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 阅卷人服务（深化版）：
 * 1. RAG 检索相关法条 → 校验考生法条引用是否准确
 * 2. DeepSeek 三层标准批改 → 解析为结构化 GradeReport
 * 3. 存入 submissions 表（供历史查看与 admin 看板）
 *
 * 这是诉探差异化卖点的内核，也是 Agent 岗面试亮点。
 */
@Service
public class GradeService {

    private static final Logger log = LoggerFactory.getLogger(GradeService.class);

    private static final String GRADER_PROMPT = """
            你是法考主观题阅卷人。请按以下 JSON 格式输出批改报告（只输出 JSON，不要其他文字）：
            {
              "conclusion": "结论判断：对/部分对/错",
              "hitPoints": ["命中的采分点1", "命中的采分点2"],
              "missedPoints": ["缺失的采分点1"],
              "lawCitationIssue": "法条引用问题分析",
              "logicFlaw": "逻辑漏洞分析",
              "suggestion": "改进建议",
              "score": 75
            }
            评分流程：
            1. 先排查扣分项（结论错误、法条引用错误）。
            2. 再对照标准采分点逐一核对命中。
            3. 最后核定得分(0-100)。
            以下为相关法条供参考（考生引用应与之一致）：
            """;

    private final ChatModel chatModel;
    private final RetrievalService retrievalService;
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public GradeService(ChatModel chatModel, RetrievalService retrievalService,
                        JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.retrievalService = retrievalService;
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public GradeReport grade(String question, String answer, Long userId) {
        // 1. RAG 检索相关法条（用于校验考生引用）
        List<Citation> laws = retrievalService.search(question, 5);
        List<GradeReport.LawRef> lawRefs = laws.stream()
                .map(c -> new GradeReport.LawRef(c.source(), c.articleNo(), c.content()))
                .toList();

        // 2. DeepSeek 结构化批改
        String lawContext = formatLaws(laws);
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(GRADER_PROMPT + lawContext),
                new UserMessage("题目：" + question + "\n\n考生作答：" + answer)
        ));
        GradeReport report;
        try {
            ChatResponse response = chatModel.call(prompt);
            String json = response.getResult().getOutput().getText();
            report = parseReport(json, lawRefs);
        } catch (Exception e) {
            log.error("阅卷失败", e);
            report = fallbackReport(lawRefs, e.getMessage());
        }

        // 3. 存入 submissions 表
        saveSubmission(userId, question, answer, report);

        return report;
    }

    private GradeReport parseReport(String json, List<GradeReport.LawRef> lawRefs) {
        try {
            // 容错：去除可能的 ```json 包裹
            String clean = json.trim();
            if (clean.startsWith("```")) {
                clean = clean.replaceAll("^```\\w*\\n?", "").replaceAll("\\n?```$", "");
            }
            return objectMapper.readValue(clean, GradeReport.class);
        } catch (Exception e) {
            log.warn("解析批改 JSON 失败，使用兜底: {}", e.getMessage());
            return new GradeReport("解析失败", List.of(), List.of(),
                    "N/A", "N/A", json, 0, lawRefs);
        }
    }

    private GradeReport fallbackReport(List<GradeReport.LawRef> lawRefs, String err) {
        return new GradeReport("批改失败", List.of(), List.of(),
                "N/A", "N/A", "服务异常: " + err, 0, lawRefs);
    }

    private String formatLaws(List<Citation> laws) {
        if (laws.isEmpty()) return "（未检索到相关法条）\n";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < laws.size(); i++) {
            Citation c = laws.get(i);
            sb.append("[").append(i + 1).append("] ")
                    .append(c.source()).append(" ").append(c.articleNo()).append("\n")
                    .append(c.content()).append("\n\n");
        }
        return sb.toString();
    }

    private void saveSubmission(Long userId, String question, String answer, GradeReport report) {
        try {
            String json = objectMapper.writeValueAsString(report);
            jdbc.update("insert into submissions (user_id, answer, grade_report, score) values (?,?,?::jsonb,?)",
                    userId, answer, json, report.score());
        } catch (Exception e) {
            log.warn("保存批改记录失败: {}", e.getMessage());
        }
    }
}
