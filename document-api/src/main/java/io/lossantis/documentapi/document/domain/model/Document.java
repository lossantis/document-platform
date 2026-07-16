package io.lossantis.documentapi.document.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Document {

    private final UUID id;
    private final String originalFilename;
    private final String contentType;
    private final long size;
    private final DocumentStatus status;
    private final String storageKey;
    private final Instant createdAt;

    private Document(
            UUID id,
            String originalFilename,
            String contentType,
            long size,
            DocumentStatus status,
            String storageKey,
            Instant createdAt
    ) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.status = status;
        this.storageKey = storageKey;
        this.createdAt = createdAt;
    }

    public static Document upload(
            String originalFilename,
            String contentType,
            long size,
            String storageKey
    ) {
        return new Document(
                UUID.randomUUID(),
                originalFilename,
                contentType,
                size,
                DocumentStatus.UPLOADED,
                storageKey,
                Instant.now()
        );
    }

    public static Document restore(
            UUID id,
            String originalFilename,
            String contentType,
            long size,
            DocumentStatus status,
            String storageKey,
            Instant createdAt
    ) {
        return new Document(
                id,
                originalFilename,
                contentType,
                size,
                status,
                storageKey,
                createdAt
        );
    }

    public Document markAsProcessed() {
        return new Document(
                this.id,
                this.originalFilename,
                this.contentType,
                this.size,
                DocumentStatus.PROCESSED,
                this.storageKey,
                this.createdAt
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

    public String getStorageKey() {
        return storageKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}