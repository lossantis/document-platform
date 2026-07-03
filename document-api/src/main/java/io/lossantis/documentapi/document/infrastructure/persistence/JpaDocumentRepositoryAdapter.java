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
        DocumentJpaEntity entity = toEntity(document);
        DocumentJpaEntity savedEntity = springDataDocumentRepository.save(entity);

        return toDomain(savedEntity);
    }

    private DocumentJpaEntity toEntity(Document document) {
        return new DocumentJpaEntity(
                document.getId(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getSize(),
                document.getStatus(),
                document.getStorageKey(),
                document.getCreatedAt()
        );
    }

    private Document toDomain(DocumentJpaEntity entity) {
        return Document.restore(
                entity.getId(),
                entity.getOriginalFilename(),
                entity.getContentType(),
                entity.getSize(),
                entity.getStatus(),
                entity.getStorageKey(),
                entity.getCreatedAt()
        );
    }
}
