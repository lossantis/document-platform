package io.lossantis.documentprocessor.document.domain;

public record ProcessedText(
        long characterCount,
        long wordCount,
        long lineCount
) {
}