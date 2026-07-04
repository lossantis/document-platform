package io.lossantis.documentprocessor.document.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DocumentUploadedEvent(
        UUID eventId,
        UUID documentId,
        String storageKey,
        String contentType,
        long size
) {
}