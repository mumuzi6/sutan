package com.mumuzi.sutan.backend.controller;

import com.mumuzi.sutan.backend.rag.Citation;
import com.mumuzi.sutan.backend.rag.RetrievalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG 检索接口：验证向量召回与可溯源 Citation。
 */
@RestController
@RequestMapping("/api/rag")
@Tag(name = "RAG 检索", description = "法条/案例/真题相似度检索")
public class RagController {

    private final RetrievalService retrievalService;

    public RagController(RetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @GetMapping("/search")
    @Operation(summary = "相似度检索 Top-K，返回可溯源引用")
    public Map<String, Object> search(@RequestParam String q,
                                      @RequestParam(defaultValue = "5") int topK) {
        List<Citation> hits = retrievalService.search(q, topK);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("query", q);
        result.put("topK", topK);
        result.put("count", hits.size());
        result.put("hits", hits);
        return result;
    }
}
