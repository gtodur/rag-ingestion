package com.ingestion.rag.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class IngestionTaskConfig {
    // Define a ThreadPoolTaskExecutor bean here to control how many background threads run at once.
}
