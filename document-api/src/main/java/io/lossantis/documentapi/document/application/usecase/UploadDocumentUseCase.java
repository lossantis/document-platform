package io.lossantis.documentapi.document.application.usecase;

import io.lossantis.documentapi.document.application.event.DocumentUploadedEvent;
import io.lossantis.documentapi.document.application.messaging.DocumentEventPublisher;
import io.lossantis.documentapi.document.application.result.UploadDocumentResult;
import io.lossantis.documentapi.document.application.command.UploadDocumentCommand;
import io.lossantis.documentapi.document.application.storage.DocumentStorage;
import io.lossantis.documentapi.document.domain.model.Document;
import io.lossantis.documentapi.document.domain.repository.DocumentRepository;
import org.springframework.stereotype.Service;

@Service
public class UploadDocumentUseCase {
    private final DocumentRepository documentRepository;
    private final DocumentStorage documentStorage;
    private final DocumentEventPublisher documentEventPublisher;

    public UploadDocumentUseCase(
            DocumentRepository documentRepository,
            DocumentStorage documentStorage,
            DocumentEventPublisher documentEventPublisher
    ) {
        this.documentRepository = documentRepository;
        this.documentStorage = documentStorage;
        this.documentEventPublisher = documentEventPublisher;
    }

    public UploadDocumentResult execute(UploadDocumentCommand command) {
        String storageKey = documentStorage.upload(
                command.inputStream(),
                command.size(),
                command.contentType(),
                command.originalFilename()
        );

        Document document = Document.upload(
                command.originalFilename(),
                command.contentType(),
                command.size(),
                storageKey
        );

        Document savedDocument = documentRepository.save(document);

        DocumentUploadedEvent event = DocumentUploadedEvent.from(savedDocument);
        documentEventPublisher.publish(event);

        return UploadDocumentResult.from(savedDocument);
    }
}
