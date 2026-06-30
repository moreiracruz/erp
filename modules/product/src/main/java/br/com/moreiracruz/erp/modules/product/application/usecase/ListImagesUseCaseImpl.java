package br.com.moreiracruz.erp.modules.product.application.usecase;

import br.com.moreiracruz.erp.modules.product.domain.model.ProdutoImagem;
import br.com.moreiracruz.erp.modules.product.domain.port.in.ImageResponse;
import br.com.moreiracruz.erp.modules.product.domain.port.in.ListImagesUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ImageStoragePort;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ProdutoImagemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case implementation for listing all images of a product in display order.
 */
@Service
@Transactional(readOnly = true)
public class ListImagesUseCaseImpl implements ListImagesUseCase {

    private final ProdutoImagemRepository repository;
    private final ImageStoragePort imageStoragePort;

    public ListImagesUseCaseImpl(ProdutoImagemRepository repository,
                                 ImageStoragePort imageStoragePort) {
        this.repository = repository;
        this.imageStoragePort = imageStoragePort;
    }

    @Override
    public List<ImageResponse> listByProduct(UUID produtoUuid) {
        List<ProdutoImagem> imagens = repository.findByProdutoUuidOrderBySortOrder(produtoUuid);
        return imagens.stream()
                .map(imagem -> toResponse(imagem, produtoUuid))
                .toList();
    }

    private ImageResponse toResponse(ProdutoImagem imagem, UUID produtoUuid) {
        String ext = deriveExtension(imagem.getContentType());
        String baseName = imagem.getFilename();

        String thumbnailUrl = imageStoragePort.resolveUrl(produtoUuid, baseName + "_thumb." + ext);
        String cardUrl = imageStoragePort.resolveUrl(produtoUuid, baseName + "_card." + ext);
        String fullUrl = imageStoragePort.resolveUrl(produtoUuid, baseName + "_full." + ext);

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

    private String deriveExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}
