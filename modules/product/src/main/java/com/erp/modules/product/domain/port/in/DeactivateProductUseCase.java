package com.erp.modules.product.domain.port.in;

import java.util.UUID;

/**
 * Inbound port for deactivating a product and all its variants.
 */
public interface DeactivateProductUseCase {

    void deactivate(UUID uuid);
}
