package br.com.moreiracruz.erp.modules.product.domain.port.out;

import java.util.List;
import java.util.UUID;

/**
 * Outbound port for storing and retrieving product image files on the filesystem.
 */
public interface ImageStoragePort {

    void store(UUID produtoUuid, String filename, byte[] content);

    void delete(UUID produtoUuid, String filename);

    void deleteAll(UUID produtoUuid, List<String> filenames);

    String resolveUrl(UUID produtoUuid, String filename);
}
