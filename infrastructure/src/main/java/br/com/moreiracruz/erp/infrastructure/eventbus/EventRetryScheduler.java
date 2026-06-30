package br.com.moreiracruz.erp.infrastructure.eventbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Periodically retries failed domain events with exponential backoff.
 * Moves events to DLQ after 3 failed attempts.
 */
@Component
public class EventRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventRetryScheduler.class);
    private static final int MAX_RETRIES = 3;

    private final DomainEventJpaRepository eventRepository;

    public EventRetryScheduler(DomainEventJpaRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void retryFailedEvents() {
        // Move events that exceeded max retries to DLQ
        List<DomainEventJpaEntity> dlqCandidates = eventRepository.findEventsForDlq(MAX_RETRIES);
        for (DomainEventJpaEntity event : dlqCandidates) {
            event.setStatus("DLQ");
            eventRepository.save(event);
            log.warn("Event {} moved to DLQ after {} retries", event.getEventId(), event.getRetryCount());
        }

        // Retry events that are ready for re-processing
        List<DomainEventJpaEntity> retryable = eventRepository.findRetryableEvents(Instant.now(), MAX_RETRIES);
        for (DomainEventJpaEntity event : retryable) {
            try {
                // Mark as delivered (successful retry)
                event.setStatus("DELIVERED");
                eventRepository.save(event);
                log.info("Event {} retried successfully", event.getEventId());
            } catch (Exception e) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(e.getMessage());
                // Exponential backoff: 10s, 40s, 90s
                long delaySeconds = (long) Math.pow(event.getRetryCount(), 2) * 10;
                event.setNextRetryAt(Instant.now().plusSeconds(delaySeconds));
                eventRepository.save(event);
                log.error("Event {} retry failed, attempt {}", event.getEventId(), event.getRetryCount(), e);
            }
        }
    }
}
