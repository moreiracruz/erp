package br.com.moreiracruz.erp.modules.product.domain.port.in;

import java.util.UUID;

/**
 * Inbound port for deleting a single image from a product.
 */
public interface DeleteImageUseCase {

    void delete(UUID produtoUuid, Long imageId);
}
