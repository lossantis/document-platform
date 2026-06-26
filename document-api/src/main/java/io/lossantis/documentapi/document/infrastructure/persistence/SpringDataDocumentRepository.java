package io.lossantis.documentapi.document.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataDocumentRepository extends JpaRepository<DocumentJpaEntity, UUID> {
}
