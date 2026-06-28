package br.com.moreiracruz.erp.modules.product.domain.port.in;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port for retrieving a product by its public UUID.
 */
public interface GetProductUseCase {

    List<ProdutoResponse> findAll();

    ProdutoResponse findByUuid(UUID uuid);
}
