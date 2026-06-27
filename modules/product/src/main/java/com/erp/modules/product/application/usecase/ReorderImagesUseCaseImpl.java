package com.erp.modules.product.application.usecase;

import com.erp.modules.product.domain.model.ProdutoImagem;
import com.erp.modules.product.domain.port.in.ImageResponse;
import com.erp.modules.product.domain.port.in.ReorderImagesUseCase;
import com.erp.modules.product.domain.port.out.ImageStoragePort;
import com.erp.modules.product.domain.port.out.ProdutoImagemRepository;
import com.erp.shared.exceptions.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case implementation for reordering images of a product.
 *
 * <p>Validates that the provided ordered IDs are an exact permutation of the
 * product's image IDs (no missing, no extra, no duplicates), then updates
 * sort_order based on array position.
 */
@Service
@Transactional
public class ReorderImagesUseCaseImpl implements ReorderImagesUseCase {

    private static final Map<String, String> CONTENT_TYPE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final ProdutoImagemRepository repository;
    private final ImageStoragePort imageStoragePort;

    public ReorderImagesUseCaseImpl(ProdutoImagemRepository repository,
                                    ImageStoragePort imageStoragePort) {
        this.repository = repository;
        this.imageStoragePort = imageStoragePort;
    }

    @Override
    public List<ImageResponse> reorder(UUID produtoUuid, List<Long> orderedIds) {
        List<ProdutoImagem> images = repository.findByProdutoUuidOrderBySortOrder(produtoUuid);

        validatePermutation(images, orderedIds);

        // Build a lookup map: id → image
        Map<Long, ProdutoImagem> imageById = images.stream()
                .collect(Collectors.toMap(ProdutoImagem::getId, img -> img));

        // Update sort_order based on position in the orderedIds list
        for (int i = 0; i < orderedIds.size(); i++) {
            ProdutoImagem image = imageById.get(orderedIds.get(i));
            image.setSortOrder(i);
        }

        List<ProdutoImagem> saved = repository.saveAll(images);

        // Return sorted by new sort_order
        return saved.stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(img -> toResponse(produtoUuid, img))
                .toList();
    }

    // ── Validation ───────────────────────────────────────────────────────────

    private void validatePermutation(List<ProdutoImagem> images, List<Long> orderedIds) {
        Set<Long> existingIds = images.stream()
                .map(ProdutoImagem::getId)
                .collect(Collectors.toSet());

        Set<Long> providedIds = new HashSet<>(orderedIds);

        // Check for duplicates in provided list
        boolean hasDuplicates = orderedIds.size() != providedIds.size();

        // Check exact match
        if (hasDuplicates || !existingIds.equals(providedIds)) {
            throw new ValidationException("Lista de IDs inválida");
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

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
