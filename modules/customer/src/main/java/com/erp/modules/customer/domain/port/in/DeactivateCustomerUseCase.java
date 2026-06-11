package com.erp.modules.customer.domain.port.in;

import java.util.UUID;

/**
 * Inbound port for deactivating a customer.
 */
public interface DeactivateCustomerUseCase {
    void deactivate(UUID uuid);
}
