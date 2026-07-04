package io.lossantis.documentprocessor.document.application;

import java.util.UUID;

public record ProcessDocumentCommand(
        UUID documentId,
        String storageKey,
        String contentType,
        long size
) {
}