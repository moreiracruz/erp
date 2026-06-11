package com.erp.modules.sales.domain.port.in;

import java.util.UUID;

/**
 * Inbound port for opening a new sale.
 */
public interface OpenSaleUseCase {

    VendaResponse open(UUID operatorUuid, OpenSaleCommand cmd);
}
