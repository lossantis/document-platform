package io.lossantis.documentapi.document.infrastructure.persistence;

import io.lossantis.documentapi.document.domain.model.Document;
import io.lossantis.documentapi.document.domain.repository.DocumentRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaDocumentRepositoryAdapter implements DocumentRepository {

    private final SpringDataDocumentRepository springDataDocumentRepository;

    public JpaDocumentRepositoryAdapter(SpringDataDocumentRepository springDataDocumentRepository) {
        this.springDataDocumentRepository = springDataDocumentRepository;
    }

    @Override
    public Document save(Document document) {
        DocumentJpaEntity entity = DocumentJpaEntity.fromDomain(document);
        DocumentJpaEntity savedEntity = springDataDocumentRepository.save(entity);

        return savedEntity.toDomain();
    }
}
