package io.lossantis.documentprocessor.document.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextDocumentProcessor {

    private static final Pattern WORD_PATTERN =
            Pattern.compile("\\S+");

    public ProcessedText process(String text) {

        long characterCount = text.length();

        long wordCount = countWords(text);

        long lineCount = text.lines().count();

        return new ProcessedText(
                characterCount,
                wordCount,
                lineCount
        );
    }

    private long countWords(String text) {

        Matcher matcher = WORD_PATTERN.matcher(text);

        long count = 0;

        while (matcher.find()) {
            count++;
        }

        return count;
    }
}