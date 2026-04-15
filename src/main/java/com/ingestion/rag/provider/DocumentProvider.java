package com.ingestion.rag.provider;

import org.springframework.ai.document.Document;

import java.util.List;

public interface DocumentProvider {
    /**
     * Fetches new or updated data and converts it into
     * the Spring AI Document format.
     */
    List<Document> fetchUpdates();

    /**
     * Returns a unique identifier for the source (e.g., "S3", "SQL_DB")
     */
    String getSourceType();
}
