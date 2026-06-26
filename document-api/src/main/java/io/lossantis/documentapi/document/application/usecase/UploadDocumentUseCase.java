package io.lossantis.documentapi.document.application.usecase;

import io.lossantis.documentapi.document.application.command.UploadDocumentCommand;
import io.lossantis.documentapi.document.application.result.UploadDocumentResult;
import io.lossantis.documentapi.document.domain.model.Document;
import io.lossantis.documentapi.document.domain.repository.DocumentRepository;
import org.springframework.stereotype.Service;

@Service
public class UploadDocumentUseCase {

    private final DocumentRepository documentRepository;

    public UploadDocumentUseCase(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public UploadDocumentResult execute(UploadDocumentCommand command) {
        Document document = Document.upload(
                command.originalFilename(),
                command.contentType(),
                command.size()
        );

        Document savedDocument = documentRepository.save(document);

        return new UploadDocumentResult(
                savedDocument.getId(),
                savedDocument.getOriginalFilename(),
                savedDocument.getContentType(),
                savedDocument.getSize(),
                savedDocument.getStatus(),
                savedDocument.getCreatedAt()
        );
    }
}
