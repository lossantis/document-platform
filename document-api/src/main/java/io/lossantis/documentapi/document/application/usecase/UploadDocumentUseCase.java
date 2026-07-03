package io.lossantis.documentapi.document.application.usecase;

import io.lossantis.documentapi.document.application.UploadDocumentResult;
import io.lossantis.documentapi.document.application.command.UploadDocumentCommand;
import io.lossantis.documentapi.document.application.storage.DocumentStorage;
import io.lossantis.documentapi.document.domain.model.Document;
import io.lossantis.documentapi.document.domain.repository.DocumentRepository;
import org.springframework.stereotype.Service;

@Service
public class UploadDocumentUseCase {

    private final DocumentRepository documentRepository;
    private final DocumentStorage documentStorage;

    public UploadDocumentUseCase(
            DocumentRepository documentRepository,
            DocumentStorage documentStorage
    ) {
        this.documentRepository = documentRepository;
        this.documentStorage = documentStorage;
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

        return UploadDocumentResult.from(savedDocument);
    }
}
