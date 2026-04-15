package com.ingestion.rag.provider.db;

import com.ingestion.rag.provider.DocumentProvider;
//import com.ingestion.rag.repository.IngestionLogRepository;
import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseDocumentProvider implements DocumentProvider {

    private final JdbcClient jdbcClient;
    //private final IngestionLogRepository logRepository;

    public DatabaseDocumentProvider(JdbcClient jdbcClient
          //                          IngestionLogRepository logRepository
    ) {
        this.jdbcClient = jdbcClient;
       // this.logRepository = logRepository;
    }

    @Override
    public List<Document> fetchUpdates() {
        // 1. Get the last time we successfully ran this specific source
//        LocalDateTime lastRun = logRepository.findLastSuccessTime(getSourceType())
//                .orElse(LocalDateTime.MIN);

        // 2. Query only the 'deltas' (changed rows)
        // Assume we are pulling from a 'product_catalog' or 'knowledge_base' table
        return jdbcClient.sql("SELECT id, content, category, updated_at FROM knowledge_base WHERE updated_at > :lastRun")
                //.param("lastRun", lastRun)
                .query((rs, rowNum) -> {
                    // 3. Map SQL Row to Spring AI Document
                    return new Document(
                            rs.getString("content"),
                            Map.of(
                                    "source_id", rs.getLong("id"),
                                    "category", rs.getString("category"),
                                    "updated_at", rs.getTimestamp("updated_at").toString()
                            )
                    );
                })
                .list();
    }

    @Override
    public String getSourceType() {
        return "SQL_DATABASE";
    }
}
