package br.com.moreiracruz.erp.shared.kernel;

import java.util.UUID;

/**
 * Base class for all aggregate roots in the system.
 *
 * <p>Provides the dual-identity pattern used throughout the domain:
 * <ul>
 *   <li>{@code id} — internal BIGSERIAL surrogate key (never exposed in APIs)</li>
 *   <li>{@code uuid} — public UUID identifier</li>
 * </ul>
 */
public abstract class AggregateRoot implements Identifiable {

    protected Long id;

    protected UUID uuid;

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public Long getId() {
        return id;
    }
}
