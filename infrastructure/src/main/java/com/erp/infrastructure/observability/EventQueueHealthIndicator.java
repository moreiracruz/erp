package com.erp.infrastructure.observability;

import com.erp.infrastructure.eventbus.DomainEventJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator that monitors the domain event queue.
 * Reports DOWN if the number of FAILED events exceeds a configurable threshold.
 */
@Component
public class EventQueueHealthIndicator implements HealthIndicator {

    private final DomainEventJpaRepository eventRepo;

    @Value("${health.event-queue.max-failed:100}")
    private int maxFailed;

    public EventQueueHealthIndicator(DomainEventJpaRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    @Override
    public Health health() {
        long failedCount = eventRepo.countByStatus("FAILED");
        if (failedCount > maxFailed) {
            return Health.down().withDetail("failedEvents", failedCount).build();
        }
        return Health.up().withDetail("failedEvents", failedCount).build();
    }
}
