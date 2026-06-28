package br.com.moreiracruz.erp.modules.product.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Public storefront read model for a purchasable product variant.
 */
public record CatalogVariantResponse(
        UUID uuid,
        String sku,
        String size,
        String color,
        String barcode,
        BigDecimal price,
        boolean active
) {}
