package io.lossantis.documentprocessor.document.application;

import io.lossantis.documentprocessor.document.application.messaging.DocumentProcessedPublisher;
import io.lossantis.documentprocessor.document.application.storage.DocumentStorage;
import io.lossantis.documentprocessor.document.domain.ProcessedText;
import io.lossantis.documentprocessor.document.domain.TextDocumentProcessor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class ProcessDocumentUseCase {

    private final DocumentStorage documentStorage;
    private final DocumentProcessedPublisher documentProcessedPublisher;
    private final TextDocumentProcessor textDocumentProcessor;

    public ProcessDocumentUseCase(
            DocumentStorage documentStorage,
            DocumentProcessedPublisher documentProcessedPublisher
    ) {
        this.documentStorage = documentStorage;
        this.documentProcessedPublisher = documentProcessedPublisher;
        this.textDocumentProcessor = new TextDocumentProcessor();
    }

    public void execute(ProcessDocumentCommand command) {

        byte[] content = documentStorage.download(
                command.storageKey()
        );

        String text = new String(
                content,
                StandardCharsets.UTF_8
        );

        ProcessedText result =
                textDocumentProcessor.process(text);

        documentProcessedPublisher.publish(
                command.documentId(),
                result
        );

        System.out.println("========================================");
        System.out.println("Document processed");
        System.out.println("documentId: " + command.documentId());
        System.out.println("storageKey: " + command.storageKey());
        System.out.println("contentType: " + command.contentType());
        System.out.println("originalSize: " + command.size());
        System.out.println("----------------------------------------");
        System.out.println("characterCount: " + result.characterCount());
        System.out.println("wordCount: " + result.wordCount());
        System.out.println("lineCount: " + result.lineCount());
        System.out.println("----------------------------------------");
        System.out.println("DocumentProcessed published");
        System.out.println("========================================");
    }
}