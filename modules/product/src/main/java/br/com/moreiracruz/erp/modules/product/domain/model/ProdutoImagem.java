package br.com.moreiracruz.erp.modules.product.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain model representing a product image.
 *
 * <p>Each image belongs to a {@link Produto} (via {@code produtoUuid}) and holds
 * metadata about the uploaded file. Use {@link #create} for new uploads (with
 * validation) and {@link #restore} for loading from persistence (no validation).
 */
public class ProdutoImagem {

    private Long id;
    private UUID produtoUuid;
    private String filename;
    private String originalName;
    private String contentType;
    private long fileSize;
    private int sortOrder;
    private boolean main;
    private Instant createdAt;

    private ProdutoImagem() {}

    /**
     * Factory method for creating a new image record with full validation.
     *
     * @param produtoUuid the UUID of the owning product
     * @param filename    sanitized storage filename
     * @param originalName original upload filename
     * @param contentType MIME type (image/jpeg, image/png, image/webp)
     * @param fileSize    file size in bytes
     * @param sortOrder   display order position
     * @param main        whether this is the main/cover image
     * @return a validated {@code ProdutoImagem} instance
     * @throws ValidationException if any field fails validation
     */
    public static ProdutoImagem create(UUID produtoUuid, String filename,
                                       String originalName, String contentType,
                                       long fileSize, int sortOrder, boolean main) {
        Objects.requireNonNull(produtoUuid, "produtoUuid must not be null");

        if (filename == null || filename.isBlank()) {
            throw new ValidationException("filename", "não pode ser vazio");
        }
        if (originalName == null || originalName.isBlank()) {
            throw new ValidationException("originalName", "não pode ser vazio");
        }
        if (contentType == null || !ImageValidator.isAllowedContentType(contentType)) {
            throw new ValidationException("contentType",
                    "tipo não aceito. Aceitos: image/jpeg, image/png, image/webp");
        }
        ImageValidator.validateFileSize(fileSize);
        ImageValidator.validateFilename(filename);

        ProdutoImagem imagem = new ProdutoImagem();
        imagem.produtoUuid = produtoUuid;
        imagem.filename = filename;
        imagem.originalName = originalName;
        imagem.contentType = contentType;
        imagem.fileSize = fileSize;
        imagem.sortOrder = sortOrder;
        imagem.main = main;
        imagem.createdAt = Instant.now();
        return imagem;
    }

    /**
     * Restores a {@code ProdutoImagem} from its persisted state, bypassing validation.
     *
     * <p>Intended for use by persistence adapters only — all values are trusted to
     * be already validated at creation time.
     */
    public static ProdutoImagem restore(Long id, UUID produtoUuid, String filename,
                                        String originalName, String contentType,
                                        long fileSize, int sortOrder, boolean main,
                                        Instant createdAt) {
        ProdutoImagem imagem = new ProdutoImagem();
        imagem.id = id;
        imagem.produtoUuid = produtoUuid;
        imagem.filename = filename;
        imagem.originalName = originalName;
        imagem.contentType = contentType;
        imagem.fileSize = fileSize;
        imagem.sortOrder = sortOrder;
        imagem.main = main;
        imagem.createdAt = createdAt;
        return imagem;
    }

    // ── Mutators (for use case layer) ────────────────────────────────────────

    public void setMain(boolean main) {
        this.main = main;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public UUID getProdutoUuid() {
        return produtoUuid;
    }

    public String getFilename() {
        return filename;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isMain() {
        return main;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
