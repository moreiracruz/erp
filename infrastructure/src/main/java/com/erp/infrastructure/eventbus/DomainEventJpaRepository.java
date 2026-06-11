package com.erp.infrastructure.eventbus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for domain_events table — supports idempotency checks and retry scheduling.
 */
@Repository
public interface DomainEventJpaRepository extends JpaRepository<DomainEventJpaEntity, Long> {

    boolean existsByEventId(UUID eventId);

    @Query("SELECT e FROM DomainEventJpaEntity e WHERE e.status = 'FAILED' AND e.nextRetryAt <= :now AND e.retryCount < :maxRetries")
    List<DomainEventJpaEntity> findRetryableEvents(@Param("now") Instant now, @Param("maxRetries") int maxRetries);

    @Query("SELECT e FROM DomainEventJpaEntity e WHERE e.status = 'FAILED' AND e.retryCount >= :maxRetries")
    List<DomainEventJpaEntity> findEventsForDlq(@Param("maxRetries") int maxRetries);

    long countByStatus(String status);
}
