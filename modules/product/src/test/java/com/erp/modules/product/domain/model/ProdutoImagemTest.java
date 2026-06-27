package com.erp.modules.product.domain.model;

import com.erp.shared.exceptions.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProdutoImagem domain model")
class ProdutoImagemTest {

    private static final UUID PRODUCT_UUID = UUID.randomUUID();

    @Test
    @DisplayName("create() builds a valid image with all fields set")
    void createBuildsValidImage() {
        ProdutoImagem imagem = ProdutoImagem.create(
                PRODUCT_UUID, "abc123", "foto.jpg", "image/jpeg", 1024L, 0, true);

        assertThat(imagem.getProdutoUuid()).isEqualTo(PRODUCT_UUID);
        assertThat(imagem.getFilename()).isEqualTo("abc123");
        assertThat(imagem.getOriginalName()).isEqualTo("foto.jpg");
        assertThat(imagem.getContentType()).isEqualTo("image/jpeg");
        assertThat(imagem.getFileSize()).isEqualTo(1024L);
        assertThat(imagem.getSortOrder()).isEqualTo(0);
        assertThat(imagem.isMain()).isTrue();
        assertThat(imagem.getCreatedAt()).isNotNull();
        assertThat(imagem.getId()).isNull(); // not persisted yet
    }

    @Test
    @DisplayName("create() rejects null produtoUuid")
    void createRejectsNullProductUuid() {
        assertThatThrownBy(() -> ProdutoImagem.create(
                null, "abc123", "foto.jpg", "image/jpeg", 1024L, 0, false))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("create() rejects blank filename")
    void createRejectsBlankFilename() {
        assertThatThrownBy(() -> ProdutoImagem.create(
                PRODUCT_UUID, "", "foto.jpg", "image/jpeg", 1024L, 0, false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("create() rejects blank originalName")
    void createRejectsBlankOriginalName() {
        assertThatThrownBy(() -> ProdutoImagem.create(
                PRODUCT_UUID, "abc123", "", "image/jpeg", 1024L, 0, false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("create() rejects unsupported content type")
    void createRejectsUnsupportedContentType() {
        assertThatThrownBy(() -> ProdutoImagem.create(
                PRODUCT_UUID, "abc123", "image.gif", "image/gif", 1024L, 0, false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("create() rejects file size exceeding 5MB")
    void createRejectsLargeFile() {
        long sixMB = 6L * 1024 * 1024;
        assertThatThrownBy(() -> ProdutoImagem.create(
                PRODUCT_UUID, "abc123", "big.jpg", "image/jpeg", sixMB, 0, false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("create() rejects filename with path traversal")
    void createRejectsPathTraversalFilename() {
        assertThatThrownBy(() -> ProdutoImagem.create(
                PRODUCT_UUID, "../etc/passwd", "hack.jpg", "image/jpeg", 1024L, 0, false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("restore() loads all fields without validation")
    void restoreLoadsAllFields() {
        Instant created = Instant.parse("2024-01-15T10:30:00Z");
        ProdutoImagem imagem = ProdutoImagem.restore(
                42L, PRODUCT_UUID, "file123", "original.png",
                "image/png", 2048L, 3, false, created);

        assertThat(imagem.getId()).isEqualTo(42L);
        assertThat(imagem.getProdutoUuid()).isEqualTo(PRODUCT_UUID);
        assertThat(imagem.getFilename()).isEqualTo("file123");
        assertThat(imagem.getOriginalName()).isEqualTo("original.png");
        assertThat(imagem.getContentType()).isEqualTo("image/png");
        assertThat(imagem.getFileSize()).isEqualTo(2048L);
        assertThat(imagem.getSortOrder()).isEqualTo(3);
        assertThat(imagem.isMain()).isFalse();
        assertThat(imagem.getCreatedAt()).isEqualTo(created);
    }

    @Test
    @DisplayName("setMain() and setSortOrder() mutators work")
    void mutatorsWork() {
        ProdutoImagem imagem = ProdutoImagem.create(
                PRODUCT_UUID, "abc123", "foto.jpg", "image/jpeg", 1024L, 0, false);

        imagem.setMain(true);
        assertThat(imagem.isMain()).isTrue();

        imagem.setSortOrder(5);
        assertThat(imagem.getSortOrder()).isEqualTo(5);
    }
}
