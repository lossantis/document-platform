package io.lossantis.documentapi.document.infrastructure.messaging;

import io.lossantis.documentapi.document.application.event.DocumentUploadedEvent;
import io.lossantis.documentapi.document.application.messaging.DocumentEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaDocumentEventPublisher implements DocumentEventPublisher {

    private final KafkaTemplate<String, DocumentUploadedEvent> kafkaTemplate;

    private final String documentUploadedTopic;

    public KafkaDocumentEventPublisher(
            KafkaTemplate<String, DocumentUploadedEvent> kafkaTemplate,
            @Value("${app.kafka.topics.document-uploaded}")
            String documentUploadedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.documentUploadedTopic = documentUploadedTopic;
    }

    @Override
    public void publish(DocumentUploadedEvent event) {
        kafkaTemplate
                .send(
                        documentUploadedTopic,
                        event.documentId().toString(),
                        event
                )
                .join();
    }
}