package com.mumuzi.sutan.backend.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * RAG 检索服务：基于 pgvector 相似度召回，返回可溯源的 Citation 列表。
 * 后续可叠加：query 改写、BM25 混合检索、bge-reranker 重排。
 */
@Service
public class RetrievalService {

    private static final double DEFAULT_THRESHOLD = 0.6;

    private final VectorStore vectorStore;

    public RetrievalService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Citation> search(String query, int topK) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(DEFAULT_THRESHOLD)
                .build();
        List<Document> docs = vectorStore.similaritySearch(request);
        if (docs == null) {
            return List.of();
        }
        return docs.stream().map(this::toCitation).toList();
    }

    private Citation toCitation(Document doc) {
        Map<String, Object> meta = doc.getMetadata() == null ? Map.of() : doc.getMetadata();
        return new Citation(
                String.valueOf(meta.getOrDefault("doc_type", "unknown")),
                String.valueOf(meta.getOrDefault("source", "")),
                String.valueOf(meta.getOrDefault("article_no", "")),
                doc.getText(),
                meta.get("distance") == null ? null : String.valueOf(meta.get("distance"))
        );
    }
}
