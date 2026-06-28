package br.com.moreiracruz.erp.shared.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Generic envelope that wraps any domain event payload with metadata.
 *
 * @param <T> the type of the event payload
 */
public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        T payload
) {}
