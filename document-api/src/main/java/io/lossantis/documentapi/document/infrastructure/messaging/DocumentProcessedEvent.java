package io.lossantis.documentapi.document.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DocumentProcessedEvent(
        UUID eventId,
        UUID documentId,
        long characterCount,
        long wordCount,
        long lineCount,
        Instant processedAt
) {
}
