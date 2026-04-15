package com.ingestion.rag.provider.s3;

import com.ingestion.rag.provider.DocumentProvider;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.util.List;

@Component
public class S3DocumentProvider implements DocumentProvider {

    private final S3Client s3Client;

    @Value("${amazon.s3.ingestion.bucket}")
    private String ingestionS3Bucket;

    public S3DocumentProvider(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public List<Document> fetchUpdates() {
        // 1. List objects in the bucket
        ListObjectsV2Response listResponse = s3Client.listObjectsV2(r -> r.bucket(ingestionS3Bucket));

        return listResponse.contents().stream()
                .map(s3Object -> {
                    // 2. Fetch the file content
                    ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(r ->
                            r.bucket(ingestionS3Bucket).key(s3Object.key()));

                    // 3. Convert S3 Resource to Spring AI Document
                    // TikaDocumentReader is great for PDFs and Word docs
                    TikaDocumentReader reader = new TikaDocumentReader(new InputStreamResource(inputStream));
                    List<Document> docs = reader.get();

                    // 4. Enrich with metadata (crucial for production)
                    docs.forEach(doc -> {
                        doc.getMetadata().put("filename", s3Object.key());
                        doc.getMetadata().put("source", s3Object.key());
                        doc.getMetadata().put("last_modified", s3Object.lastModified().toString());
                        doc.getMetadata().put("fullFilePath", "s3://" + ingestionS3Bucket + "/" + s3Object.key());
                    });

                    return docs;
                })
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public String getSourceType() {
        return "AWS_S3";
    }
}
