package com.ingestion.rag.provider.api;

import com.ingestion.rag.provider.DocumentProvider;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

//@Component
public class ExternalAPIProvider
//        implements DocumentProvider
{

//    private final RestClient restClient;
//
//    public ExternalAPIProvider(RestClient.Builder builder) {
//        this.restClient = builder.baseUrl("https://api.yourcompany.com/v1").build();
//    }
//
//    @Override
//    public List<Document> fetchUpdates() {
//        // Fetching data from a hypothetical 'articles' endpoint
//        List<ArticleResponse> articles = restClient.get()
//                .uri("/latest-articles")
//                .retrieve()
//                .body(new ParameterizedTypeReference<List<ArticleResponse>>() {});
//
//        return articles.stream()
//                .map(article -> new Document(
//                        article.body(),
//                        Map.of(
//                                "api_origin", "InternalCMS",
//                                "article_id", article.id(),
//                                "url", article.link()
//                        )
//                ))
//                .toList();
//    }
//
//    @Override
//    public String getSourceType() {
//        return "REST_API";
//    }
}

// Simple DTO for JSON mapping
record ArticleResponse(String id, String body, String link) {}
