package com.erp.modules.product.domain.port.in;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port for listing all images of a product in display order.
 */
public interface ListImagesUseCase {

    List<ImageResponse> listByProduct(UUID produtoUuid);
}
