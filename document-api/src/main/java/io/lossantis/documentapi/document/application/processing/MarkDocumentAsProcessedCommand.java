package io.lossantis.documentapi.document.application.processing;

import java.time.Instant;
import java.util.UUID;

public record MarkDocumentAsProcessedCommand(
        UUID documentId,
        long characterCount,
        long wordCount,
        long lineCount,
        Instant processedAt
) {
}