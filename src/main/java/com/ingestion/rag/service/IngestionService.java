package com.ingestion.rag.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IngestionService {

    private final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;
    private final EmbeddingModel embeddingModel;

    public IngestionService(JdbcClient jdbcClient, EmbeddingModel embeddingModel) {
        this.jdbcClient = jdbcClient;
        this.objectMapper = new ObjectMapper();
        this.embeddingModel = embeddingModel;
    }

    public void ingest(List<Document> rawDocs, List<Document> chunkedDocs, String source) {
        // 1. Create parent record in rag_documents
        var docId = UUID.randomUUID();
        Map<String, Object> rawDocsMetadata = rawDocs.get(0).getMetadata();
        try {
            jdbcClient.sql("INSERT INTO rag_documents (id, source, modality, title, filepath, metadata) VALUES (?, ?, ?, ?, ?, ?::jsonb)")
                    .params(docId, source, "TEXT", rawDocsMetadata.get("filename"),
                            rawDocsMetadata.get("fullFilePath"),
                            objectMapper.writeValueAsString(rawDocsMetadata))
                    .update();
        } catch (Exception e) {
            log.error("Error while persisting record into rag_documents table", e);
        }

        saveChunksManually(chunkedDocs, docId);
    }

    private void saveChunksManually(List<Document> chunkedDocs, UUID documentId) {
        for (Document chunk : chunkedDocs) {
            var chunkId = UUID.randomUUID();
            String textToEmbed = chunk.getText();
            try {
            float[] vector = embeddingModel.embed(textToEmbed);

            jdbcClient.sql("""
                            INSERT INTO rag_chunks (id, document_id, content, text_embedding, metadata)
                            VALUES (:id, :docId, :content, :vector::vector, :metadata::jsonb)
                            """)
                    .param("id", chunkId)
                    .param("docId", documentId)
                    .param("content", chunk.getText())
                    .param("vector", vector)
                    .param("metadata", chunk.getMetadata())
                    .update();
        } catch (Exception e) {
            log.error("Error while persisting record into rag_chunks table", e);
        }
        }
    }
}
