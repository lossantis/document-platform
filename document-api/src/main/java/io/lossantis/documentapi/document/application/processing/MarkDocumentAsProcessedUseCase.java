package io.lossantis.documentapi.document.application.processing;

import io.lossantis.documentapi.document.domain.model.Document;
import io.lossantis.documentapi.document.domain.repository.DocumentRepository;
import org.springframework.stereotype.Service;

@Service
public class MarkDocumentAsProcessedUseCase {

    private final DocumentRepository documentRepository;

    public MarkDocumentAsProcessedUseCase(
            DocumentRepository documentRepository
    ) {
        this.documentRepository = documentRepository;
    }

    public void execute(MarkDocumentAsProcessedCommand command) {

        Document document = documentRepository
                .findById(command.documentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Document not found: " + command.documentId()
                ));

        Document processedDocument =
                document.markAsProcessed();

        documentRepository.save(processedDocument);

        System.out.println("========================================");
        System.out.println("Document marked as processed");
        System.out.println("documentId: " + command.documentId());
        System.out.println("characterCount: " + command.characterCount());
        System.out.println("wordCount: " + command.wordCount());
        System.out.println("lineCount: " + command.lineCount());
        System.out.println("processedAt: " + command.processedAt());
        System.out.println("========================================");
    }
}