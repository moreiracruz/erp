package com.erp.shared.kernel;

/**
 * Marker interface for all domain events in the system.
 * Implementing this interface signals that a class represents something
 * meaningful that happened in the domain. Used for type-safe event dispatch
 * through the in-process event bus.
 */
public interface DomainEvent {
    // Marker interface — no methods required
}
