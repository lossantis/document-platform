package io.lossantis.documentapi.document.application.command;

public record UploadDocumentCommand(
        String originalFilename,
        String contentType,
        long size
) {
}
