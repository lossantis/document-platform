package io.lossantis.documentapi.document.infrastructure.persistence;

import io.lossantis.documentapi.document.domain.model.DocumentStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class DocumentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @Column(name = "storage_key")
    private String storageKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DocumentJpaEntity() {
    }

    public DocumentJpaEntity(
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
