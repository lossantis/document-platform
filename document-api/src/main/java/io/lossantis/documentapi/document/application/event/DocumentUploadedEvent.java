package io.lossantis.documentapi.document.application.event;

import io.lossantis.documentapi.document.domain.model.Document;

import java.time.Instant;
import java.util.UUID;

public record DocumentUploadedEvent(
        UUID eventId,
        UUID documentId,
        String storageKey,
        String originalFilename,
        String contentType,
        long size,
        Instant uploadedAt
) {

    public static DocumentUploadedEvent from(Document document) {
        return new DocumentUploadedEvent(
                UUID.randomUUID(),
                document.getId(),
                document.getStorageKey(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getSize(),
                document.getCreatedAt()
        );
    }
}