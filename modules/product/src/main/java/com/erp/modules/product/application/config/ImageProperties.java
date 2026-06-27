package com.erp.modules.product.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Configuration properties for product image management,
 * bound to the {@code app.images} prefix in application.yml.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.images")
public class ImageProperties {

    /**
     * Base filesystem path for storing product images.
     */
    private String basePath = "./uploads/products";

    /**
     * Maximum file size in bytes for a single uploaded image (default 5MB).
     */
    private long maxFileSize = 5_242_880L;

    /**
     * Maximum number of images allowed per product.
     */
    private int maxImagesPerProduct = 10;

    /**
     * Maximum total storage in bytes per product (default 50MB).
     */
    private long maxStoragePerProduct = 52_428_800L;

    /**
     * List of accepted MIME types for image uploads.
     */
    private List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/webp");

    /**
     * Map of size variant names to their target widths in pixels.
     */
    private Map<String, Integer> sizes = Map.of(
            "thumbnail", 200,
            "card", 400,
            "full", 800
    );
}
