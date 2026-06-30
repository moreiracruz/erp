package br.com.moreiracruz.erp.infrastructure.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ReservaEstoqueJpaEntity}.
 */
public interface ReservaEstoqueJpaRepository extends JpaRepository<ReservaEstoqueJpaEntity, Long> {

    Optional<ReservaEstoqueJpaEntity> findByUuid(UUID uuid);

    List<ReservaEstoqueJpaEntity> findBySaleUuid(UUID saleUuid);

    @Query("SELECT r FROM ReservaEstoqueJpaEntity r WHERE r.status = 'ACTIVE' AND r.expiresAt <= :before")
    List<ReservaEstoqueJpaEntity> findExpiredActive(@Param("before") Instant before);
}
