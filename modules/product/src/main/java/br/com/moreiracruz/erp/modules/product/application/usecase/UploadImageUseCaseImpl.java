package br.com.moreiracruz.erp.modules.product.application.usecase;

import br.com.moreiracruz.erp.modules.product.application.config.ImageProperties;
import br.com.moreiracruz.erp.modules.product.domain.model.ImageValidator;
import br.com.moreiracruz.erp.modules.product.domain.model.ProdutoImagem;
import br.com.moreiracruz.erp.modules.product.domain.port.in.ImageResponse;
import br.com.moreiracruz.erp.modules.product.domain.port.in.UploadImageCommand;
import br.com.moreiracruz.erp.modules.product.domain.port.in.UploadImageUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ImageProcessorPort;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ImageStoragePort;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ProdutoImagemRepository;
import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Use case implementation for uploading one or more images to a product.
 *
 * <p>Validates file type (content-type + magic bytes), file size, image count limit,
 * and storage limit. Generates sanitized UUID-based filenames, delegates to
 * {@link ImageProcessorPort} for resize, stores variants via {@link ImageStoragePort},
 * and saves metadata to the repository.
 */
@Service
@Transactional
public class UploadImageUseCaseImpl implements UploadImageUseCase {

    private static final Logger log = LoggerFactory.getLogger(UploadImageUseCaseImpl.class);

    private static final Map<String, String> CONTENT_TYPE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final ProdutoImagemRepository repository;
    private final ImageStoragePort imageStoragePort;
    private final ImageProcessorPort imageProcessorPort;
    private final ImageProperties imageProperties;

    public UploadImageUseCaseImpl(ProdutoImagemRepository repository,
                                  ImageStoragePort imageStoragePort,
                                  ImageProcessorPort imageProcessorPort,
                                  ImageProperties imageProperties) {
        this.repository = repository;
        this.imageStoragePort = imageStoragePort;
        this.imageProcessorPort = imageProcessorPort;
        this.imageProperties = imageProperties;
    }

    @Override
    public List<ImageResponse> upload(UUID produtoUuid, List<UploadImageCommand> files) {
        validateBatch(produtoUuid, files);

        int currentCount = repository.countByProdutoUuid(produtoUuid);
        int currentMaxSortOrder = repository.findMaxSortOrderByProdutoUuid(produtoUuid);
        boolean isFirstImage = currentCount == 0;

        List<ImageResponse> responses = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            UploadImageCommand cmd = files.get(i);

            validateSingleFile(cmd);

            String baseName = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            String extension = CONTENT_TYPE_EXTENSIONS.get(cmd.contentType().toLowerCase());
            int sortOrder = currentMaxSortOrder + 1 + i;
            boolean isMain = isFirstImage && i == 0;

            // Resize image into variants
            Map<String, byte[]> variants = imageProcessorPort.resize(cmd.content(), cmd.contentType());

            // Store each variant
            List<String> storedFilenames = new ArrayList<>();
            try {
                for (Map.Entry<String, byte[]> entry : variants.entrySet()) {
                    String sizeSuffix = entry.getKey();
                    byte[] variantBytes = entry.getValue();
                    String variantFilename = baseName + "_" + sizeSuffix + "." + extension;
                    imageStoragePort.store(produtoUuid, variantFilename, variantBytes);
                    storedFilenames.add(variantFilename);
                }
            } catch (Exception e) {
                // Cleanup stored files on failure
                cleanupStoredFiles(produtoUuid, storedFilenames);
                throw new RuntimeException("Erro ao salvar arquivo", e);
            }

            // Sanitize original filename for metadata
            String sanitizedOriginalName = ImageValidator.sanitizeFilename(cmd.originalFilename());

            // Save metadata to repository
            ProdutoImagem imagem = ProdutoImagem.create(
                    produtoUuid,
                    baseName,
                    sanitizedOriginalName,
                    cmd.contentType(),
                    cmd.size(),
                    sortOrder,
                    isMain
            );

            ProdutoImagem saved = repository.save(imagem);
            responses.add(toResponse(produtoUuid, saved));
        }

        return responses;
    }

    // ── Validation ───────────────────────────────────────────────────────────

    private void validateBatch(UUID produtoUuid, List<UploadImageCommand> files) {
        int currentCount = repository.countByProdutoUuid(produtoUuid);

        // Check image count limit
        if (currentCount + files.size() > imageProperties.getMaxImagesPerProduct()) {
            throw new ValidationException(
                    "Limite de imagens excedido. Máximo " + imageProperties.getMaxImagesPerProduct()
                            + " imagens por produto");
        }

        // Check storage limit
        long currentStorage = repository.sumFileSizeByProdutoUuid(produtoUuid);
        long batchSize = files.stream().mapToLong(UploadImageCommand::size).sum();
        if (currentStorage + batchSize > imageProperties.getMaxStoragePerProduct()) {
            throw new ValidationException(
                    "Limite de armazenamento excedido. Máximo 50MB por produto");
        }
    }

    private void validateSingleFile(UploadImageCommand cmd) {
        // Validate content-type header
        if (!ImageValidator.isAllowedContentType(cmd.contentType())) {
            throw new ValidationException(
                    "Tipo de arquivo não aceito. Aceitos: JPEG, PNG, WebP");
        }

        // Validate magic bytes
        ImageValidator.validateMagicBytes(cmd.content());

        // Validate file size
        ImageValidator.validateFileSize(cmd.size());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void cleanupStoredFiles(UUID produtoUuid, List<String> filenames) {
        try {
            imageStoragePort.deleteAll(produtoUuid, filenames);
        } catch (Exception cleanupEx) {
            log.error("Failed to cleanup stored files for product {}: {}",
                    produtoUuid, filenames, cleanupEx);
        }
    }

    private ImageResponse toResponse(UUID produtoUuid, ProdutoImagem imagem) {
        String baseName = imagem.getFilename();
        String extension = CONTENT_TYPE_EXTENSIONS.get(imagem.getContentType().toLowerCase());

        String thumbnailUrl = imageStoragePort.resolveUrl(produtoUuid, baseName + "_thumb." + extension);
        String cardUrl = imageStoragePort.resolveUrl(produtoUuid, baseName + "_card." + extension);
        String fullUrl = imageStoragePort.resolveUrl(produtoUuid, baseName + "_full." + extension);

        return new ImageResponse(
                imagem.getId(),
                imagem.getFilename(),
                imagem.getOriginalName(),
                imagem.getContentType(),
                imagem.getFileSize(),
                imagem.getSortOrder(),
                imagem.isMain(),
                imagem.getCreatedAt(),
                thumbnailUrl,
                cardUrl,
                fullUrl
        );
    }
}
