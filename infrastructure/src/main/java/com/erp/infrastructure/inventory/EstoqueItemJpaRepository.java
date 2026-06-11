package com.erp.infrastructure.inventory;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link EstoqueItemJpaEntity}.
 */
public interface EstoqueItemJpaRepository extends JpaRepository<EstoqueItemJpaEntity, Long> {

    Optional<EstoqueItemJpaEntity> findByVarianteUuid(UUID varianteUuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT e FROM EstoqueItemJpaEntity e WHERE e.varianteUuid = :uuid")
    Optional<EstoqueItemJpaEntity> findByVarianteUuidForUpdate(@Param("uuid") UUID uuid);
}
