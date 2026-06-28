package br.com.moreiracruz.erp.modules.product.application.usecase;

import br.com.moreiracruz.erp.modules.product.domain.model.ProdutoImagem;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ImageStoragePort;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ProdutoImagemRepository;
import br.com.moreiracruz.erp.shared.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DeleteImageUseCaseImplTest {

    private ProdutoImagemRepository repository;
    private ImageStoragePort imageStoragePort;
    private DeleteImageUseCaseImpl useCase;

    private static final UUID PRODUTO_UUID = UUID.randomUUID();
    private static final Long IMAGE_ID = 1L;

    @BeforeEach
    void setUp() {
        repository = mock(ProdutoImagemRepository.class);
        imageStoragePort = mock(ImageStoragePort.class);
        useCase = new DeleteImageUseCaseImpl(repository, imageStoragePort);
    }

    @Test
    @DisplayName("Should throw NotFoundException when image does not exist")
    void shouldThrowNotFoundWhenImageDoesNotExist() {
        when(repository.findByIdAndProdutoUuid(IMAGE_ID, PRODUTO_UUID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(PRODUTO_UUID, IMAGE_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Imagem não encontrada");

        verify(imageStoragePort, never()).deleteAll(any(), any());
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Should delete all file variants and DB record for JPEG image")
    void shouldDeleteAllVariantsAndRecordForJpeg() {
        ProdutoImagem imagem = ProdutoImagem.restore(
                IMAGE_ID, PRODUTO_UUID, "abc123", "photo.jpg",
                "image/jpeg", 200_000L, 0, false, Instant.now());

        when(repository.findByIdAndProdutoUuid(IMAGE_ID, PRODUTO_UUID))
                .thenReturn(Optional.of(imagem));

        useCase.delete(PRODUTO_UUID, IMAGE_ID);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> filenamesCaptor = ArgumentCaptor.forClass(List.class);
        verify(imageStoragePort).deleteAll(eq(PRODUTO_UUID), filenamesCaptor.capture());

        List<String> deletedFilenames = filenamesCaptor.getValue();
        assertThat(deletedFilenames).containsExactlyInAnyOrder(
                "abc123_thumb.jpg", "abc123_card.jpg", "abc123_full.jpg");

        verify(repository).delete(imagem);
    }

    @Test
    @DisplayName("Should delete all file variants for PNG image")
    void shouldDeleteAllVariantsForPng() {
        ProdutoImagem imagem = ProdutoImagem.restore(
                IMAGE_ID, PRODUTO_UUID, "def456", "logo.png",
                "image/png", 100_000L, 1, false, Instant.now());

        when(repository.findByIdAndProdutoUuid(IMAGE_ID, PRODUTO_UUID))
                .thenReturn(Optional.of(imagem));

        useCase.delete(PRODUTO_UUID, IMAGE_ID);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> filenamesCaptor = ArgumentCaptor.forClass(List.class);
        verify(imageStoragePort).deleteAll(eq(PRODUTO_UUID), filenamesCaptor.capture());

        assertThat(filenamesCaptor.getValue()).containsExactlyInAnyOrder(
                "def456_thumb.png", "def456_card.png", "def456_full.png");
    }

    @Test
    @DisplayName("Should delete all file variants for WebP image")
    void shouldDeleteAllVariantsForWebp() {
        ProdutoImagem imagem = ProdutoImagem.restore(
                IMAGE_ID, PRODUTO_UUID, "ghi789", "banner.webp",
                "image/webp", 150_000L, 2, false, Instant.now());

        when(repository.findByIdAndProdutoUuid(IMAGE_ID, PRODUTO_UUID))
                .thenReturn(Optional.of(imagem));

        useCase.delete(PRODUTO_UUID, IMAGE_ID);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> filenamesCaptor = ArgumentCaptor.forClass(List.class);
        verify(imageStoragePort).deleteAll(eq(PRODUTO_UUID), filenamesCaptor.capture());

        assertThat(filenamesCaptor.getValue()).containsExactlyInAnyOrder(
                "ghi789_thumb.webp", "ghi789_card.webp", "ghi789_full.webp");
    }

    @Test
    @DisplayName("Should promote lowest sort_order image to main when main is deleted")
    void shouldPromoteLowestSortOrderWhenMainDeleted() {
        ProdutoImagem mainImage = ProdutoImagem.restore(
                1L, PRODUTO_UUID, "main1", "main.jpg",
                "image/jpeg", 200_000L, 0, true, Instant.now());

        ProdutoImagem secondImage = ProdutoImagem.restore(
                2L, PRODUTO_UUID, "second2", "second.jpg",
                "image/jpeg", 150_000L, 1, false, Instant.now());

        ProdutoImagem thirdImage = ProdutoImagem.restore(
                3L, PRODUTO_UUID, "third3", "third.jpg",
                "image/jpeg", 180_000L, 2, false, Instant.now());

        when(repository.findByIdAndProdutoUuid(1L, PRODUTO_UUID))
                .thenReturn(Optional.of(mainImage));
        when(repository.findByProdutoUuidOrderBySortOrder(PRODUTO_UUID))
                .thenReturn(List.of(secondImage, thirdImage));

        useCase.delete(PRODUTO_UUID, 1L);

        verify(repository).delete(mainImage);

        // The second image (lowest sort_order remaining) should be promoted to main
        ArgumentCaptor<ProdutoImagem> saveCaptor = ArgumentCaptor.forClass(ProdutoImagem.class);
        verify(repository).save(saveCaptor.capture());
        assertThat(saveCaptor.getValue().getId()).isEqualTo(2L);
        assertThat(saveCaptor.getValue().isMain()).isTrue();
    }

    @Test
    @DisplayName("Should not promote any image when main is deleted and no others remain")
    void shouldNotPromoteWhenMainDeletedAndNoOthersRemain() {
        ProdutoImagem mainImage = ProdutoImagem.restore(
                1L, PRODUTO_UUID, "only1", "only.jpg",
                "image/jpeg", 200_000L, 0, true, Instant.now());

        when(repository.findByIdAndProdutoUuid(1L, PRODUTO_UUID))
                .thenReturn(Optional.of(mainImage));
        when(repository.findByProdutoUuidOrderBySortOrder(PRODUTO_UUID))
                .thenReturn(List.of());

        useCase.delete(PRODUTO_UUID, 1L);

        verify(repository).delete(mainImage);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should not attempt promotion when deleted image was not main")
    void shouldNotPromoteWhenDeletedImageWasNotMain() {
        ProdutoImagem nonMainImage = ProdutoImagem.restore(
                2L, PRODUTO_UUID, "second2", "second.jpg",
                "image/jpeg", 150_000L, 1, false, Instant.now());

        when(repository.findByIdAndProdutoUuid(2L, PRODUTO_UUID))
                .thenReturn(Optional.of(nonMainImage));

        useCase.delete(PRODUTO_UUID, 2L);

        verify(repository).delete(nonMainImage);
        verify(repository, never()).findByProdutoUuidOrderBySortOrder(any());
        verify(repository, never()).save(any());
    }
}
