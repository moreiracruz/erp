package br.com.moreiracruz.erp.modules.product.domain.port.in;

/**
 * Inbound port for registering a new product in the catalog.
 */
public interface RegisterProductUseCase {

    ProdutoResponse register(RegisterProductCommand cmd);
}
