package br.com.moreiracruz.erp.modules.product.domain.port.in;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port for reordering images of a product.
 */
public interface ReorderImagesUseCase {

    List<ImageResponse> reorder(UUID produtoUuid, List<Long> orderedIds);
}
