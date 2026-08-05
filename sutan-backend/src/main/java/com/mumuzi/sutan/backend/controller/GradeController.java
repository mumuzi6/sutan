package com.mumuzi.sutan.backend.controller;

import com.mumuzi.sutan.backend.grade.GradeReport;
import com.mumuzi.sutan.backend.grade.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 主观题批改 API：提交作答→结构化批改报告 + 历史记录。
 */
@RestController
@RequestMapping("/api/grade")
@Tag(name = "主观题批改", description = "模拟阅卷人批改与历史")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @PostMapping("/submit")
    @Operation(summary = "提交主观题作答，返回结构化批改报告")
    public GradeReport submit(@RequestBody Map<String, String> body) {
        String question = body.getOrDefault("question", "");
        String answer = body.getOrDefault("answer", "");
        String userIdStr = body.getOrDefault("userId", "1");
        Long userId = Long.parseLong(userIdStr);
        return gradeService.grade(question, answer, userId);
    }

    @GetMapping("/history")
    @Operation(summary = "查询用户批改历史")
    public List<Map<String, Object>> history(@RequestParam(defaultValue = "1") Long userId) {
        // 简版：从 submissions 表取最近 20 条
        return List.of(); // TODO: 实现 JDBC 查询
    }
}
