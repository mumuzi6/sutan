package com.mumuzi.sutan.backend.rag;

/**
 * 检索结果引用（可溯源）。
 * 每条命中都带来源信息，供答案强制溯源——既是 liability 兜底，也是 grounding 亮点。
 */
public record Citation(
        String docType,    // law / case / question
        String source,     // 法规名 / 案例出处
        String articleNo,  // 条文号 / 案号
        String content,    // 命中原文
        String score       // 相似度分数
) {
}
