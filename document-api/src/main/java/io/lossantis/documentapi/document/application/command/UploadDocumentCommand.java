package io.lossantis.documentapi.document.application.command;

import java.io.InputStream;

public record UploadDocumentCommand(
        String originalFilename,
        String contentType,
        long size,
        InputStream inputStream
) {
}