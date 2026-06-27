package com.erp.modules.product.adapter.out.storage;

import com.erp.modules.product.application.config.ImageProperties;
import com.erp.modules.product.domain.port.out.ImageStoragePort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Local filesystem implementation of {@link ImageStoragePort}.
 * Stores product image files under {@code {basePath}/{produtoUuid}/{filename}}.
 */
@Component
public class LocalImageStorageAdapter implements ImageStoragePort {

    private final Path basePath;

    public LocalImageStorageAdapter(ImageProperties imageProperties) {
        this.basePath = Path.of(imageProperties.getBasePath());
    }

    @Override
    public void store(UUID produtoUuid, String filename, byte[] content) {
        Path filePath = resolvePath(produtoUuid, filename);
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, content);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Erro ao salvar arquivo: " + filePath, e);
        }
    }

    @Override
    public void delete(UUID produtoUuid, String filename) {
        Path filePath = resolvePath(produtoUuid, filename);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Erro ao deletar arquivo: " + filePath, e);
        }
    }

    @Override
    public void deleteAll(UUID produtoUuid, List<String> filenames) {
        for (String filename : filenames) {
            delete(produtoUuid, filename);
        }
    }

    @Override
    public String resolveUrl(UUID produtoUuid, String filename) {
        return "/uploads/products/" + produtoUuid + "/" + filename;
    }

    private Path resolvePath(UUID produtoUuid, String filename) {
        return basePath.resolve(produtoUuid.toString()).resolve(filename);
    }
}
