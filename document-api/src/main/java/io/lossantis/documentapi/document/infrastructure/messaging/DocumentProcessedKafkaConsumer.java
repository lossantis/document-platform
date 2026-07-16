package io.lossantis.documentapi.document.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lossantis.documentapi.document.application.processing.MarkDocumentAsProcessedCommand;
import io.lossantis.documentapi.document.application.processing.MarkDocumentAsProcessedUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DocumentProcessedKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final MarkDocumentAsProcessedUseCase markDocumentAsProcessedUseCase;

    public DocumentProcessedKafkaConsumer(
            ObjectMapper objectMapper,
            MarkDocumentAsProcessedUseCase markDocumentAsProcessedUseCase
    ) {
        this.objectMapper = objectMapper;
        this.markDocumentAsProcessedUseCase = markDocumentAsProcessedUseCase;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.document-processed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) throws JsonProcessingException {

        DocumentProcessedEvent event = objectMapper.readValue(
                message,
                DocumentProcessedEvent.class
        );

        MarkDocumentAsProcessedCommand command =
                new MarkDocumentAsProcessedCommand(
                        event.documentId(),
                        event.characterCount(),
                        event.wordCount(),
                        event.lineCount(),
                        event.processedAt()
                );

        markDocumentAsProcessedUseCase.execute(command);
    }
}