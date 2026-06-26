package io.lossantis.documentapi.document.infrastructure.persistence;

import io.lossantis.documentapi.document.domain.model.Document;
import io.lossantis.documentapi.document.domain.model.DocumentStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class DocumentJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size", nullable = false)
    private long size;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DocumentStatus status;

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
            Instant createdAt
    ) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static DocumentJpaEntity fromDomain(Document document) {
        return new DocumentJpaEntity(
                document.getId(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getSize(),
                document.getStatus(),
                document.getCreatedAt()
        );
    }

    public Document toDomain() {
        return new Document(
                id,
                originalFilename,
                contentType,
                size,
                status,
                createdAt
        );
    }
}
