package br.com.moreiracruz.erp.modules.product.domain.port.in;

import java.util.UUID;

/**
 * Read model returned by product use cases.
 */
public record ProdutoResponse(UUID uuid, String name, String brand, String category, boolean active) {}
