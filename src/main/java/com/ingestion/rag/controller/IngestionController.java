package com.ingestion.rag.controller;

import com.ingestion.rag.service.IngestionOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ingestion")
// Add spring security to method level as these are admin level tasks
public class IngestionController {

    private final IngestionOrchestrator orchestrator;

    public IngestionController(IngestionOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * Kick off the entire pipeline (S3 + DB + API)
     */
    @PostMapping("/sync-all")
    public ResponseEntity<String> syncAll() {
        // Run as async to prevent the UI from hanging
        orchestrator.runFullIngestionAsync();
        return ResponseEntity.accepted().body("Full ingestion pipeline started...");
    }

    /**
     * Kick off sync for a specific provider (e.g., /sync/S3)
     */
    @PostMapping("/sync/{sourceType}")
    public ResponseEntity<String> syncSource(@PathVariable String sourceType) {
        orchestrator.runSingleSourceIngestionAsync(sourceType);
        return ResponseEntity.accepted().body("Ingestion for " + sourceType + " started...");
    }

    /**
     * Get the status of current or past jobs
     */
//    @GetMapping("/status")
//    public ResponseEntity<List<IngestionStatus>> getStatus() {
//        return ResponseEntity.ok(orchestrator.getLatestStatus());
//    }
}

