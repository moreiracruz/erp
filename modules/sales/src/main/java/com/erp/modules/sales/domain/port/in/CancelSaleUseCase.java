package com.erp.modules.sales.domain.port.in;

import java.util.UUID;

/**
 * Inbound port for cancelling an open sale.
 */
public interface CancelSaleUseCase {

    void cancel(UUID vendaUuid, CancelSaleCommand cmd);
}
