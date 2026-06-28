package br.com.moreiracruz.erp.modules.product.domain.port.in;

import java.util.UUID;

/**
 * Inbound port for registering a new variant under an existing product.
 */
public interface RegisterVariantUseCase {

    VarianteResponse register(UUID produtoUuid, RegisterVariantCommand cmd);
}
