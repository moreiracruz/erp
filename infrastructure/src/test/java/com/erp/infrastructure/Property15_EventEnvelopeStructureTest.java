package com.erp.infrastructure;

import com.erp.shared.events.EventEnvelope;
import com.erp.shared.events.SaleCompletedPayload;
import net.jqwik.api.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates: Requirements 1.2
 */
class Property15_EventEnvelopeStructureTest {

    @Property(tries = 1000)
    @Label("Feature: erp-loja-roupas, Property 15: Domain event envelope is always structurally valid")
    void eventEnvelopeIsAlwaysValid(
            @ForAll("eventTypes") String eventType,
            @ForAll("totals") BigDecimal total) {

        var envelope = new EventEnvelope<>(UUID.randomUUID(), eventType, Instant.now(),
                new SaleCompletedPayload(UUID.randomUUID(), UUID.randomUUID(), List.of(), total, "DINHEIRO"));

        assertThat(envelope.eventId()).isNotNull();
        assertThat(envelope.eventType()).isNotNull().isNotBlank();
        assertThat(envelope.occurredAt()).isNotNull();
        assertThat(envelope.payload()).isNotNull();
        // Validate UUID v4 format
        assertThat(envelope.eventId().version()).isEqualTo(4);
    }

    @Provide
    Arbitrary<String> eventTypes() {
        return Arbitraries.of("SaleCompleted", "StockReserved", "PaymentApproved");
    }

    @Provide
    Arbitrary<BigDecimal> totals() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("99999.99"))
                .ofScale(2);
    }
}
