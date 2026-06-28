package br.com.moreiracruz.erp.modules.sales.domain.port.in;

import java.util.UUID;

/**
 * Inbound port for finalizing a sale (checkout).
 */
public interface FinalizeSaleUseCase {

    FinalizationResponse finalize(UUID vendaUuid, FinalizeSaleCommand cmd);
}
