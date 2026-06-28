package br.com.moreiracruz.erp.modules.product.domain.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Public storefront read model with price range and active variants.
 */
public record CatalogProductResponse(
        UUID uuid,
        String name,
        String brand,
        String category,
        boolean active,
        List<CatalogVariantResponse> variants,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Instant createdAt
) {}
