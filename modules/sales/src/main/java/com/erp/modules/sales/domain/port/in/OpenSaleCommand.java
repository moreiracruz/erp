package com.erp.modules.sales.domain.port.in;

import java.util.UUID;

/**
 * Command to open a new sale on a POS terminal.
 *
 * @param terminalId   the POS terminal identifier
 * @param clienteUuid  the customer UUID (nullable — anonymous sale)
 */
public record OpenSaleCommand(String terminalId, UUID clienteUuid) {}
