package com.erp.modules.product.domain.port.in;

import java.util.UUID;

/**
 * Inbound port for retrieving a product by its public UUID.
 */
public interface GetProductUseCase {

    ProdutoResponse findByUuid(UUID uuid);
}
