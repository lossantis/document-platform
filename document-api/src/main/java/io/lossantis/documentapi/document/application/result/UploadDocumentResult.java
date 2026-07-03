package io.lossantis.documentapi.document.application.result;

import io.lossantis.documentapi.document.domain.model.Document;
import io.lossantis.documentapi.document.domain.model.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record UploadDocumentResult(
        UUID id,
        String originalFilename,
        String contentType,
        long size,
        DocumentStatus status,
        String storageKey,
        Instant createdAt
) {

    public static UploadDocumentResult from(Document document) {
        return new UploadDocumentResult(
                document.getId(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getSize(),
                document.getStatus(),
                document.getStorageKey(),
                document.getCreatedAt()
        );
    }
}