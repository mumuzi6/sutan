package com.mumuzi.sutan.backend.rag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 法条语料入库：读取 classpath:/legal_seed.json，带 metadata 写入 pgvector。
 * metadata 含 doc_type / source / article_no，供检索时强制溯源。
 * 触发：POST /api/admin/ingest
 */
@Service
public class LegalCorpusIngestor {

    private static final Logger log = LoggerFactory.getLogger(LegalCorpusIngestor.class);

    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;

    public LegalCorpusIngestor(VectorStore vectorStore, ObjectMapper objectMapper) {
        this.vectorStore = vectorStore;
        this.objectMapper = objectMapper;
    }

    public int ingest() {
        List<SeedEntry> entries = loadSeed();
        List<Document> documents = new ArrayList<>();
        for (SeedEntry e : entries) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("doc_type", e.docType());
            metadata.put("source", e.source());
            metadata.put("article_no", e.articleNo());
            documents.add(new Document(e.content(), metadata));
        }
        vectorStore.add(documents);
        log.info("语料入库完成，共 {} 条", documents.size());
        return documents.size();
    }

    private List<SeedEntry> loadSeed() {
        try (InputStream is = new ClassPathResource("legal_seed.json").getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("读取 legal_seed.json 失败: " + e.getMessage(), e);
        }
    }

    public record SeedEntry(
            @JsonProperty("doc_type") String docType,
            @JsonProperty("source") String source,
            @JsonProperty("article_no") String articleNo,
            @JsonProperty("content") String content
    ) {
    }
}
