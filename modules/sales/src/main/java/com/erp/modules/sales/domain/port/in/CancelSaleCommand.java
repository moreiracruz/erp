package com.erp.modules.sales.domain.port.in;

/**
 * Command to cancel an open sale.
 *
 * @param reason mandatory cancellation reason (1–255 chars)
 */
public record CancelSaleCommand(String reason) {}
