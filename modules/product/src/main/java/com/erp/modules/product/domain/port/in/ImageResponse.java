package com.erp.modules.product.domain.port.in;

import java.time.Instant;

/**
 * Read model returned by image use cases, containing metadata and URLs for all size variants.
 */
public record ImageResponse(
        Long id,
        String filename,
        String originalName,
        String contentType,
        long fileSize,
        int sortOrder,
        boolean main,
        Instant createdAt,
        String thumbnailUrl,
        String cardUrl,
        String fullUrl
) {}
