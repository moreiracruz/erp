package com.erp.events;

import com.erp.modules.inventory.domain.port.in.RegisterEntryUseCase;
import com.erp.modules.inventory.domain.port.in.ReserveStockUseCase;
import com.erp.modules.inventory.domain.port.in.StockEntryCommand;
import com.erp.modules.inventory.domain.port.in.StockReserveCommand;
import com.erp.shared.events.EventEnvelope;
import com.erp.shared.events.SaleCompletedPayload;
import com.erp.test.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test verifying event consumer idempotency.
 * Property 10: N deliveries → 1 side-effect.
 */
class EventIdempotencyIT extends AbstractIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private RegisterEntryUseCase registerEntryUseCase;

    @Autowired
    private ReserveStockUseCase reserveStockUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Publishing the same SaleCompletedEvent 5 times creates exactly 1 RECEITA entry")
    void duplicateEvents_createExactlyOneFinanceEntry() {
        UUID saleUuid = UUID.randomUUID();
        UUID operatorUuid = UUID.randomUUID();
        UUID varianteUuid = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        // Setup: create stock and reserve it for this sale
        registerEntryUseCase.registerEntry(new StockEntryCommand(varianteUuid, 10, operatorUuid));
        reserveStockUseCase.reserve(new StockReserveCommand(varianteUuid, saleUuid, 2));

        // Create event envelope
        SaleCompletedPayload payload = new SaleCompletedPayload(
                saleUuid,
                operatorUuid,
                List.of(new SaleCompletedPayload.SaleItem("SKU-001", 2)),
                new BigDecimal("99.90"),
                "PIX"
        );
        EventEnvelope<SaleCompletedPayload> event = new EventEnvelope<>(
                eventId, "SaleCompleted", Instant.now(), payload);

        // Publish the SAME event 5 times
        for (int i = 0; i < 5; i++) {
            eventPublisher.publishEvent(event);
        }

        // Assert: exactly 1 RECEITA in lancamentos_financeiros
        Integer receitaCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM lancamentos_financeiros WHERE sale_uuid = ? AND type = 'RECEITA'",
                Integer.class, saleUuid);
        assertThat(receitaCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Duplicate SaleCompletedEvent commits stock exactly once")
    void duplicateEvents_commitStockExactlyOnce() {
        UUID saleUuid = UUID.randomUUID();
        UUID operatorUuid = UUID.randomUUID();
        UUID varianteUuid = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        // Setup: create stock and reserve it
        registerEntryUseCase.registerEntry(new StockEntryCommand(varianteUuid, 20, operatorUuid));
        reserveStockUseCase.reserve(new StockReserveCommand(varianteUuid, saleUuid, 3));

        // Verify initial state: physical=20, reserved=3
        Integer initialPhysical = jdbcTemplate.queryForObject(
                "SELECT physical_stock FROM estoque_items WHERE variante_uuid = ?",
                Integer.class, varianteUuid);
        assertThat(initialPhysical).isEqualTo(20);

        SaleCompletedPayload payload = new SaleCompletedPayload(
                saleUuid,
                operatorUuid,
                List.of(new SaleCompletedPayload.SaleItem("SKU-002", 3)),
                new BigDecimal("149.70"),
                "CREDITO"
        );
        EventEnvelope<SaleCompletedPayload> event = new EventEnvelope<>(
                eventId, "SaleCompleted", Instant.now(), payload);

        // Publish same event 5 times
        for (int i = 0; i < 5; i++) {
            eventPublisher.publishEvent(event);
        }

        // Stock committed only once: reserved should be 0 after commit
        Integer reservedStock = jdbcTemplate.queryForObject(
                "SELECT reserved_stock FROM estoque_items WHERE variante_uuid = ?",
                Integer.class, varianteUuid);
        assertThat(reservedStock).isEqualTo(0);
    }
}
