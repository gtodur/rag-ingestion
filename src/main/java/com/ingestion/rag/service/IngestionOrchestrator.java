package com.ingestion.rag.service;

//import com.ingestion.rag.entity.IngestionLog;
//import com.ingestion.rag.model.IngestionStatus;
import com.ingestion.rag.provider.DocumentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IngestionOrchestrator {
    private final Logger log = LoggerFactory.getLogger(IngestionOrchestrator.class);

    private final List<DocumentProvider> providers;
    private final VectorStore vectorStore;
    private final IngestionService ingestionService;

    // In-memory status tracker (In production, move this to a DB/Redis)
    //private final Map<String, IngestionStatus> statusMap = new ConcurrentHashMap<>();

    public IngestionOrchestrator(List<DocumentProvider> providers, VectorStore vectorStore, IngestionService ingestionService) {
        this.providers = providers;
        this.vectorStore = vectorStore;
        this.ingestionService = ingestionService;
    }

    @Async
    public void runFullIngestionAsync() {
        providers.forEach(provider -> processProvider(provider));
    }

    @Async
    public void runSingleSourceIngestionAsync(String sourceType) {
        providers.stream()
                .filter(p -> p.getSourceType().equalsIgnoreCase(sourceType))
                .findFirst()
                .ifPresent(this::processProvider);
    }

    private void processProvider(DocumentProvider provider) {
        String type = provider.getSourceType();
        LocalDateTime startTime = LocalDateTime.now();
        try {
            // log this run with STARTED in DB for audit trail

            // 1. Fetch
            List<Document> rawDocs = provider.fetchUpdates();

            if (!rawDocs.isEmpty()) {
                // Since 1 token ≈ 4 characters for English,
                // so for a 500-token chunk with a 50-token overlap, setting chunkSize to 2000 and overlap to 200.
//                TokenTextSplitter splitter = TokenTextSplitter.builder()
//                        .withChunkSize(2000)
//                        .withMinChunkLengthToEmbed(10)
//                        .withPunctuationMarks(List.of('.', '?', '!', '\n'))
//                        .withKeepSeparator(true)
//                        .build();
                // Aiming for ~700 tokens to stay well within the 1024 limit
                // after tokenizer conversion and overhead.
                TokenTextSplitter splitter = TokenTextSplitter.builder()
                        .withChunkSize(400)
                        .withMinChunkLengthToEmbed(10)
                        // Adding space ' ' helps the splitter find break points
                        // and prevents it from "over-running" the limit.
                        .withPunctuationMarks(List.of('.', '?', '!', '\n', ' '))
                        .withKeepSeparator(true)
                        .build();
                //TokenTextSplitter splitter = new TokenTextSplitter();
                List<Document> chunkedDocs = splitter.apply(rawDocs);
                ingestionService.ingest(rawDocs, chunkedDocs, "AWS_S3");
                //vectorStore.accept(chunkedDocs);

                // Record the success in the database
//                IngestionLog log = new IngestionLog();
//                log.setSourceType(type);
//                log.setLastSuccessfulRun(startTime);
//                log.setStatus("SUCCESS");
//                logRepository.save(log);

//                Overwriting by ID: When creating your Document objects in the providers,
//                ensure you generate a consistent ID (e.g., a hash of the file path or the DB primary key).
//                Most Vector Stores (like Pinecone or PgVector) will perform an "upsert"
//                —meaning it will update the existing entry rather than creating a duplicate.
//
//                Tagging & Purging: Add a version or timestamp metadata tag to your documents.
//                You can then run a cleanup job to delete any vectors that weren't updated in the latest sync.

                // update the DB entry to success
                log.info("Chunked and stored in pgvector successfully for type {}", type);
            }
        } catch (Exception e) {
            log.error("Exception during ingestion for : {} ", type, e);
            // update the DB entry to failure
        }
    }

//    private void updateStatus(String type, String status, int count, String error) {
//        statusMap.put(type, new IngestionStatus(type, status, count, LocalDateTime.now(), error));
//    }
//
//    public List<IngestionStatus> getLatestStatus() {
//        return new ArrayList<>(statusMap.values());
//    }
}
