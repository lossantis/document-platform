package io.lossantis.documentprocessor.document.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DocumentUploadedKafkaConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(
            topics = "${app.kafka.topics.document-uploaded}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) throws JsonProcessingException {

        DocumentUploadedEvent event = objectMapper.readValue(
                message,
                DocumentUploadedEvent.class
        );

        System.out.println("========================================");
        System.out.println("DocumentUploaded received");
        System.out.println("eventId:     " + event.eventId());
        System.out.println("documentId:  " + event.documentId());
        System.out.println("storageKey:  " + event.storageKey());
        System.out.println("contentType: " + event.contentType());
        System.out.println("size:        " + event.size());
        System.out.println("========================================");
    }
}