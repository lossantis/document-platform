package io.lossantis.documentapi.document.domain.repository;

import io.lossantis.documentapi.document.domain.model.Document;

import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository {

    Document save(Document document);

    Optional<Document> findById(UUID id);
}