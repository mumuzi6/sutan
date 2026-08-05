package com.mumuzi.sutan.backend.grade;

import java.util.List;

/**
 * 结构化阅卷报告（模拟阅卷人三层标准输出）。
 * 对标真实阅卷：扣分排查 → 采分点核对 → 得分核定。
 */
public record GradeReport(
        String conclusion,              // 结论判断（对/部分对/错）
        List<String> hitPoints,         // 命中的采分点
        List<String> missedPoints,      // 缺失的采分点
        String lawCitationIssue,        // 法条引用问题
        String logicFlaw,               // 逻辑漏洞
        String suggestion,              // 改进建议
        int score,                      // 预估得分 0-100
        List<LawRef> relatedLaws        // RAG 检索到的相关法条（校验用）
) {
    /** 相关法条引用（来自 RAG 溯源） */
    public record LawRef(String source, String articleNo, String content) {
    }
}
