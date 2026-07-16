package io.lossantis.documentprocessor.document.infrastructure.messaging;

import io.lossantis.documentprocessor.document.application.messaging.DocumentProcessedPublisher;
import io.lossantis.documentprocessor.document.domain.ProcessedText;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class KafkaDocumentProcessedPublisher implements DocumentProcessedPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaDocumentProcessedPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.document-processed}")
            String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(
            UUID documentId,
            ProcessedText result
    ) {
        DocumentProcessedEvent event =
                new DocumentProcessedEvent(
                        UUID.randomUUID(),
                        documentId,
                        result.characterCount(),
                        result.wordCount(),
                        result.lineCount(),
                        Instant.now()
                );
        kafkaTemplate.send(
                topic,
                documentId.toString(),
                event
        );
    }
}