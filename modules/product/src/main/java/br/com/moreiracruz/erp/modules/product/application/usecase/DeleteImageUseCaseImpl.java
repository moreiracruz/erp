package br.com.moreiracruz.erp.modules.product.application.usecase;

import br.com.moreiracruz.erp.modules.product.domain.model.ProdutoImagem;
import br.com.moreiracruz.erp.modules.product.domain.port.in.DeleteImageUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ImageStoragePort;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ProdutoImagemRepository;
import br.com.moreiracruz.erp.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case implementation for deleting a single product image.
 *
 * <p>Removes all size variants from storage, deletes the DB record, and
 * promotes the lowest sort_order image to main if the deleted image was main.
 */
@Service
@Transactional
public class DeleteImageUseCaseImpl implements DeleteImageUseCase {

    private final ProdutoImagemRepository repository;
    private final ImageStoragePort imageStoragePort;

    public DeleteImageUseCaseImpl(ProdutoImagemRepository repository,
                                  ImageStoragePort imageStoragePort) {
        this.repository = repository;
        this.imageStoragePort = imageStoragePort;
    }

    @Override
    public void delete(UUID produtoUuid, Long imageId) {
        ProdutoImagem imagem = repository.findByIdAndProdutoUuid(imageId, produtoUuid)
                .orElseThrow(() -> new NotFoundException("Imagem não encontrada"));

        // Build variant filenames and delete from storage
        List<String> variantFilenames = buildVariantFilenames(imagem);
        imageStoragePort.deleteAll(produtoUuid, variantFilenames);

        // Remove DB record
        boolean wasMain = imagem.isMain();
        repository.delete(imagem);

        // Promote next image if deleted was main
        if (wasMain) {
            List<ProdutoImagem> remaining = repository.findByProdutoUuidOrderBySortOrder(produtoUuid);
            if (!remaining.isEmpty()) {
                ProdutoImagem first = remaining.get(0);
                first.setMain(true);
                repository.save(first);
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private List<String> buildVariantFilenames(ProdutoImagem imagem) {
        String baseName = imagem.getFilename();
        String ext = extensionFromContentType(imagem.getContentType());
        return List.of(
                baseName + "_thumb." + ext,
                baseName + "_card." + ext,
                baseName + "_full." + ext
        );
    }

    private String extensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}
