package br.com.moreiracruz.erp.infrastructure.eventbus;

import br.com.moreiracruz.erp.modules.inventory.domain.port.in.CommitReserveUseCase;
import br.com.moreiracruz.erp.shared.events.EventEnvelope;
import br.com.moreiracruz.erp.shared.events.SaleCompletedPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens for SaleCompletedEvent and commits inventory reservations for each sale item.
 * Includes idempotency guard via domain_events table.
 */
@Component
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final CommitReserveUseCase commitReserveUseCase;
    private final DomainEventJpaRepository domainEventRepository;

    public InventoryEventConsumer(CommitReserveUseCase commitReserveUseCase,
                                   DomainEventJpaRepository domainEventRepository) {
        this.commitReserveUseCase = commitReserveUseCase;
        this.domainEventRepository = domainEventRepository;
    }

    @EventListener
    public void onSaleCompleted(EventEnvelope<?> event) {
        if (!"SaleCompleted".equals(event.eventType()) || !(event.payload() instanceof SaleCompletedPayload payload)) {
            return;
        }

        // Idempotency guard: check if this event was already processed.
        if (domainEventRepository.existsByEventId(event.eventId())) {
            log.info("Event {} already processed by InventoryEventConsumer, skipping", event.eventId());
            return;
        }

        log.info("Committing inventory reservations for sale {}", payload.saleUuid());

        commitReserveUseCase.commit(payload.saleUuid());

        // Record event as delivered
        DomainEventJpaEntity record = new DomainEventJpaEntity();
        record.setEventId(event.eventId());
        record.setEventType(event.eventType());
        record.setPayload("{}"); // minimal payload for idempotency tracking
        record.setOccurredAt(event.occurredAt());
        record.setStatus("DELIVERED");
        domainEventRepository.save(record);
    }
}
