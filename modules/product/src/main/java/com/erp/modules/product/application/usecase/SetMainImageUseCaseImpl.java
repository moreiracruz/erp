package com.erp.modules.product.application.usecase;

import com.erp.modules.product.domain.model.ProdutoImagem;
import com.erp.modules.product.domain.port.in.ImageResponse;
import com.erp.modules.product.domain.port.in.SetMainImageUseCase;
import com.erp.modules.product.domain.port.out.ImageStoragePort;
import com.erp.modules.product.domain.port.out.ProdutoImagemRepository;
import com.erp.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Use case implementation for designating an image as the main/cover image for a product.
 *
 * <p>Clears the {@code is_main} flag on all images for the product, then sets it
 * on the specified image, ensuring the invariant that at most one image per product
 * is marked as main.
 */
@Service
@Transactional
public class SetMainImageUseCaseImpl implements SetMainImageUseCase {

    private static final Map<String, String> CONTENT_TYPE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final ProdutoImagemRepository repository;
    private final ImageStoragePort imageStoragePort;

    public SetMainImageUseCaseImpl(ProdutoImagemRepository repository,
                                   ImageStoragePort imageStoragePort) {
        this.repository = repository;
        this.imageStoragePort = imageStoragePort;
    }

    @Override
    public ImageResponse setMain(UUID produtoUuid, Long imageId) {
        ProdutoImagem imagem = repository.findByIdAndProdutoUuid(imageId, produtoUuid)
                .orElseThrow(() -> new NotFoundException("Imagem não encontrada"));

        repository.clearMainByProdutoUuid(produtoUuid);

        imagem.setMain(true);
        ProdutoImagem saved = repository.save(imagem);

        return toResponse(produtoUuid, saved);
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
