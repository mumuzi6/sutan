package com.mumuzi.sutan.backend.controller;

import com.mumuzi.sutan.backend.rag.LegalCorpusIngestor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 管理接口：语料入库等运维操作。
 * TODO: 加权限校验（目前仅本地/内网使用）。
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理", description = "语料入库等运维操作")
public class AdminController {

    private final LegalCorpusIngestor ingestor;

    public AdminController(LegalCorpusIngestor ingestor) {
        this.ingestor = ingestor;
    }

    @PostMapping("/ingest")
    @Operation(summary = "触发法条语料入库（写 pgvector）")
    public Map<String, Object> ingest() {
        int count = ingestor.ingest();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put("ingested", count);
        return result;
    }
}
