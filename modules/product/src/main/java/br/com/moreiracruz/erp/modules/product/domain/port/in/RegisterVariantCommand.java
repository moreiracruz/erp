package br.com.moreiracruz.erp.modules.product.domain.port.in;

import java.math.BigDecimal;

/**
 * Command to register a new variant (SKU) for an existing product.
 */
public record RegisterVariantCommand(
        String sku,
        String size,
        String color,
        String barcode,
        BigDecimal price,
        BigDecimal cost
) {}
