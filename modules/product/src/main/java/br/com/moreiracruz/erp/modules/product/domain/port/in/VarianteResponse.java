package br.com.moreiracruz.erp.modules.product.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Read model returned by variant use cases.
 */
public record VarianteResponse(
        UUID uuid,
        UUID produtoUuid,
        String sku,
        String size,
        String color,
        String barcode,
        BigDecimal price,
        BigDecimal cost,
        boolean active
) {}
