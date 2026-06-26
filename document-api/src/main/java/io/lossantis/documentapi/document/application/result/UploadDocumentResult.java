package io.lossantis.documentapi.document.application.result;

import io.lossantis.documentapi.document.domain.model.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record UploadDocumentResult(
        UUID id,
        String originalFilename,
        String contentType,
        long size,
        DocumentStatus status,
        Instant createdAt
) {
}