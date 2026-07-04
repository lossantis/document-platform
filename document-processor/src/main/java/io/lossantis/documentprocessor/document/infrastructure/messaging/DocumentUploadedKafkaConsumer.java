package io.lossantis.documentprocessor.document.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lossantis.documentprocessor.document.application.ProcessDocumentCommand;
import io.lossantis.documentprocessor.document.application.ProcessDocumentUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DocumentUploadedKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final ProcessDocumentUseCase processDocumentUseCase;

    public DocumentUploadedKafkaConsumer(
            ObjectMapper objectMapper,
            ProcessDocumentUseCase processDocumentUseCase
    ) {
        this.objectMapper = objectMapper;
        this.processDocumentUseCase = processDocumentUseCase;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.document-uploaded}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) throws JsonProcessingException {

        DocumentUploadedEvent event = objectMapper.readValue(
                message,
                DocumentUploadedEvent.class
        );

        ProcessDocumentCommand command = new ProcessDocumentCommand(
                event.documentId(),
                event.storageKey(),
                event.contentType(),
                event.size()
        );

        processDocumentUseCase.execute(command);
    }
}