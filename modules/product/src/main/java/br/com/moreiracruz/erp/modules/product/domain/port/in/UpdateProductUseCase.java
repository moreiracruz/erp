package br.com.moreiracruz.erp.modules.product.domain.port.in;

import java.util.UUID;

/**
 * Inbound port for updating an existing product's attributes.
 */
public interface UpdateProductUseCase {

    ProdutoResponse update(UUID uuid, UpdateProductCommand cmd);
}
