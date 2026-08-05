package com.mumuzi.sutan.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查 / 烟雾测试接口。
 * 用于验证：服务存活 + 数据库连通 + 表已由 Flyway 建好。
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "健康检查", description = "服务与数据库连通性探活")
public class HealthController {

    private final JdbcTemplate jdbc;

    public HealthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    @Operation(summary = "综合健康检查")
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "sutan-backend");
        result.put("status", "UP");
        try {
            Integer userCount = jdbc.queryForObject("select count(*) from users", Integer.class);
            result.put("db", "UP");
            result.put("users", userCount);
        } catch (Exception e) {
            result.put("db", "DOWN");
            result.put("dbError", e.getMessage());
        }
        return result;
    }
}
