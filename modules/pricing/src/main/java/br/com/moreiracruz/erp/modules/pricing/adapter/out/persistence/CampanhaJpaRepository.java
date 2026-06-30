package br.com.moreiracruz.erp.modules.pricing.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampanhaJpaRepository extends JpaRepository<CampanhaJpaEntity, Long> {

    Optional<CampanhaJpaEntity> findByUuid(UUID uuid);

    @Query("""
            SELECT c FROM CampanhaJpaEntity c
            WHERE c.active = true
              AND c.type = :type
              AND c.targetType = :targetType
              AND (:targetUuid IS NULL OR c.targetUuid = :targetUuid)
              AND (:targetCategory IS NULL OR c.targetCategory = :targetCategory)
              AND c.startsAt < :to
              AND c.endsAt > :from
            """)
    List<CampanhaJpaEntity> findActiveOverlapping(
            @Param("type") String type,
            @Param("targetType") String targetType,
            @Param("targetUuid") UUID targetUuid,
            @Param("targetCategory") String targetCategory,
            @Param("from") Instant from,
            @Param("to") Instant to);

    List<CampanhaJpaEntity> findByActiveTrue();
}
