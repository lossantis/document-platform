package io.lossantis.documentapi.document.domain.repository;

import io.lossantis.documentapi.document.domain.model.Document;

public interface DocumentRepository {

    Document save(Document document);
}