package com.erp.modules.product.domain.port.in;

import java.util.UUID;

/**
 * Inbound port for designating an image as the main/cover image for a product.
 */
public interface SetMainImageUseCase {

    ImageResponse setMain(UUID produtoUuid, Long imageId);
}
