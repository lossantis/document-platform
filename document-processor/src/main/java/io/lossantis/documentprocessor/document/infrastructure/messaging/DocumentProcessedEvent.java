package io.lossantis.documentprocessor.document.infrastructure.messaging;

import java.time.Instant;
import java.util.UUID;

public record DocumentProcessedEvent(
        UUID eventId,
        UUID documentId,
        long characterCount,
        long wordCount,
        long lineCount,
        Instant processedAt
) {
}