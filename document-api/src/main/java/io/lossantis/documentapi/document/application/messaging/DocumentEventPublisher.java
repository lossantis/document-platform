package io.lossantis.documentapi.document.application.messaging;

import io.lossantis.documentapi.document.application.event.DocumentUploadedEvent;

public interface DocumentEventPublisher {
    void publish(DocumentUploadedEvent event);
}