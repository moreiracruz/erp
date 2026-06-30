package br.com.moreiracruz.erp.modules.sales.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to add an item to an existing sale.
 * The controller resolves barcode/SKU → variant data before constructing this command.
 *
 * @param varianteUuid the resolved product variant UUID
 * @param sku          the SKU code
 * @param unitPrice    the unit price at time of sale
 * @param quantity     the number of units to add
 */
public record AddItemCommand(UUID varianteUuid, String sku, BigDecimal unitPrice, int quantity) {}
