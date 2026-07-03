package io.lossantis.documentapi.document.application.storage;

import java.time.LocalDate;
import java.util.UUID;

public class DocumentStorageKeyGenerator {

    public String generate(String originalFilename) {
        String safeFilename = sanitize(originalFilename);
        LocalDate today = LocalDate.now();

        return "documents/%d/%02d/%02d/%s-%s".formatted(
                today.getYear(),
                today.getMonthValue(),
                today.getDayOfMonth(),
                UUID.randomUUID(),
                safeFilename
        );
    }

    private String sanitize(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file";
        }

        return filename
                .replace("\\", "_")
                .replace("/", "_")
                .replace(" ", "_");
    }
}