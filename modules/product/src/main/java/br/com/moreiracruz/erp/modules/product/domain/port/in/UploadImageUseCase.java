package br.com.moreiracruz.erp.modules.product.domain.port.in;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port for uploading one or more images to a product.
 */
public interface UploadImageUseCase {

    List<ImageResponse> upload(UUID produtoUuid, List<UploadImageCommand> files);
}
