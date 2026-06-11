package com.erp.shared.kernel;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

import java.util.UUID;

/**
 * Base class for all aggregate roots in the system.
 *
 * <p>Provides the dual-identity pattern used throughout the domain:
 * <ul>
 *   <li>{@code id} — internal BIGSERIAL surrogate key (never exposed in APIs)</li>
 *   <li>{@code uuid} — public UUID identifier, generated on first persist</li>
 * </ul>
 *
 * <p>The {@link PrePersist} callback guarantees that {@code uuid} is always
 * populated before the entity is written to the database, equivalent to
 * PostgreSQL's {@code DEFAULT gen_random_uuid()}.
 */
@MappedSuperclass
public abstract class AggregateRoot implements Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected UUID uuid;

    @PrePersist
    protected void ensureUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public Long getId() {
        return id;
    }
}
