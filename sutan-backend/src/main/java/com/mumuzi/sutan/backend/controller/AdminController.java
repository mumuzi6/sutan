package com.mumuzi.sutan.backend.controller;

import com.mumuzi.sutan.backend.rag.LegalCorpusIngestor;
import com.mumuzi.sutan.backend.stats.UsageStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 管理接口：语料入库 + 运营看板（真实用户数据）。
 * TODO: 加权限校验（目前仅本地/内网使用）。
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理", description = "语料入库 + 运营看板")
public class AdminController {

    private final LegalCorpusIngestor ingestor;
    private final UsageStatsService statsService;

    public AdminController(LegalCorpusIngestor ingestor, UsageStatsService statsService) {
        this.ingestor = ingestor;
        this.statsService = statsService;
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

    @GetMapping("/stats")
    @Operation(summary = "运营看板：今日+累计+近7天趋势")
    public Map<String, Object> stats() {
        return statsService.getDashboard();
    }

    @PostMapping("/stats/aggregate")
    @Operation(summary = "手动触发统计聚合（前一天）")
    public Map<String, Object> aggregate() {
        statsService.aggregateForDate(LocalDate.now().minusDays(1));
        return Map.of("status", "ok");
    }
}
