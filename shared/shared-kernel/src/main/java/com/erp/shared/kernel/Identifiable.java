package com.erp.shared.kernel;

import java.util.UUID;

/**
 * Marker interface for domain objects that expose a public UUID identifier.
 * All aggregate roots should implement this interface to provide a stable,
 * externally-visible identity that is safe to expose in APIs.
 */
public interface Identifiable {

    UUID getUuid();
}
