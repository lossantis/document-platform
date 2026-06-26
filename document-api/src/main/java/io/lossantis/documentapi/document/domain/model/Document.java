package io.lossantis.documentapi.document.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Document {

    private final UUID id;
    private final String originalFilename;
    private final String contentType;
    private final long size;
    private final DocumentStatus status;
    private final Instant createdAt;

    public Document(
            UUID id,
            String originalFilename,
            String contentType,
            long size,
            DocumentStatus status,
            Instant createdAt
    ) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Document upload(String originalFilename, String contentType, long size) {
        return new Document(
                UUID.randomUUID(),
                originalFilename,
                contentType,
                size,
                DocumentStatus.UPLOADED,
                Instant.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}