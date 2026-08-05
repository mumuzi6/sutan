package com.mumuzi.sutan.backend.agent.tools;

import com.mumuzi.sutan.backend.agent.Tool;
import org.springframework.stereotype.Component;

/**
 * 查真题工具：MVP 占位（真题语料未入库），返回提示。
 * 后续语料入库后接 RetrievalService 检索 exam_questions。
 */
@Component
public class SearchQuestionTool implements Tool {

    @Override
    public String name() {
        return "search_question";
    }

    @Override
    public String description() {
        return "检索法考真题及参考答案。输入：考点或关键词；输出：相关真题与采分点。"
                + "用户问及真题/考点/如何作答时调用。";
    }

    @Override
    public ToolResult execute(String input) {
        // TODO: 真题语料入库后接 RetrievalService.search(input, 3)
        return ToolResult.of("真题库尚在建设中，暂无命中。可结合法条与法理作答。");
    }
}
