package io.lossantis.documentprocessor.document.application.messaging;

import io.lossantis.documentprocessor.document.domain.ProcessedText;

import java.util.UUID;

public interface DocumentProcessedPublisher {
    void publish(
            UUID documentId,
            ProcessedText result
    );
}