package com.mumuzi.sutan.backend.agent.tools;

import com.mumuzi.sutan.backend.agent.Tool;
import com.mumuzi.sutan.backend.rag.Citation;
import com.mumuzi.sutan.backend.rag.RetrievalService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 查法条工具：基于 RAG 向量召回，返回法条原文 + 出处（强制溯源）。
 */
@Component
public class SearchLawTool implements Tool {

    private final RetrievalService retrievalService;

    public SearchLawTool(RetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @Override
    public String name() {
        return "search_law";
    }

    @Override
    public String description() {
        return "根据法律问题检索相关法条原文。输入：法律问题或关键词；输出：法条原文及其出处。"
                + "回答涉及法律规定时必须先调用此工具。";
    }

    @Override
    public ToolResult execute(String input) {
        List<Citation> hits = retrievalService.search(input, 3);
        if (hits.isEmpty()) {
            return ToolResult.of("未检索到相关法条。");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hits.size(); i++) {
            Citation c = hits.get(i);
            sb.append("[").append(i + 1).append("] ")
                    .append(c.source()).append(" ").append(c.articleNo()).append("\n")
                    .append(c.content()).append("\n\n");
        }
        return ToolResult.of(sb.toString(), hits);
    }
}
