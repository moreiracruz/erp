package com.erp.modules.product.domain.port.out;

import java.util.Map;

/**
 * Outbound port for resizing uploaded images into multiple size variants.
 */
public interface ImageProcessorPort {

    /**
     * Resizes the original image content into multiple size variants.
     *
     * @param originalContent the raw bytes of the uploaded image
     * @param contentType     the MIME type of the original image (e.g., image/jpeg, image/png, image/webp)
     * @return a map of size-suffix to processed bytes (e.g., "thumb" → bytes, "card" → bytes, "full" → bytes)
     */
    Map<String, byte[]> resize(byte[] originalContent, String contentType);
}
