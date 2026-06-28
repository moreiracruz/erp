package br.com.moreiracruz.erp.modules.product.application.usecase;

import br.com.moreiracruz.erp.modules.product.domain.model.ProdutoImagem;
import br.com.moreiracruz.erp.modules.product.domain.port.in.ImageResponse;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ImageStoragePort;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ProdutoImagemRepository;
import br.com.moreiracruz.erp.shared.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SetMainImageUseCaseImplTest {

    private ProdutoImagemRepository repository;
    private ImageStoragePort imageStoragePort;
    private SetMainImageUseCaseImpl useCase;

    private static final UUID PRODUTO_UUID = UUID.randomUUID();
    private static final Long IMAGE_ID = 1L;

    @BeforeEach
    void setUp() {
        repository = mock(ProdutoImagemRepository.class);
        imageStoragePort = mock(ImageStoragePort.class);
        useCase = new SetMainImageUseCaseImpl(repository, imageStoragePort);
    }

    @Test
    @DisplayName("Should throw NotFoundException when image does not exist for product")
    void shouldThrowNotFoundWhenImageDoesNotExist() {
        when(repository.findByIdAndProdutoUuid(IMAGE_ID, PRODUTO_UUID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.setMain(PRODUTO_UUID, IMAGE_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Imagem não encontrada");

        verify(repository, never()).clearMainByProdutoUuid(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should clear main on all images and set main on target image")
    void shouldClearMainAndSetMainOnTarget() {
        ProdutoImagem imagem = ProdutoImagem.restore(
                IMAGE_ID, PRODUTO_UUID, "abc123", "photo.jpg",
                "image/jpeg", 200_000L, 1, false, Instant.now());

        when(repository.findByIdAndProdutoUuid(IMAGE_ID, PRODUTO_UUID))
                .thenReturn(Optional.of(imagem));
        when(repository.save(any(ProdutoImagem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.setMain(PRODUTO_UUID, IMAGE_ID);

        // Verify clear was called before save
        var inOrder = inOrder(repository);
        inOrder.verify(repository).findByIdAndProdutoUuid(IMAGE_ID, PRODUTO_UUID);
        inOrder.verify(repository).clearMainByProdutoUuid(PRODUTO_UUID);
        inOrder.verify(repository).save(any(ProdutoImagem.class));

        // Verify the saved image has main set to true
        ArgumentCaptor<ProdutoImagem> saveCaptor = ArgumentCaptor.forClass(ProdutoImagem.class);
        verify(repository).save(saveCaptor.capture());
        assertThat(saveCaptor.getValue().isMain()).isTrue();
        assertThat(saveCaptor.getValue().getId()).isEqualTo(IMAGE_ID);
    }

    @Test
    @DisplayName("Should return ImageResponse with resolved URLs for JPEG image")
    void shouldReturnImageResponseWithResolvedUrlsForJpeg() {
        Instant createdAt = Instant.parse("2024-01-15T10:30:00Z");
        ProdutoImagem imagem = ProdutoImagem.restore(
                IMAGE_ID, PRODUTO_UUID, "abc123", "photo.jpg",
                "image/jpeg", 200_000L, 2, false, createdAt);

        when(repository.findByIdAndProdutoUuid(IMAGE_ID, PRODUTO_UUID))
                .thenReturn(Optional.of(imagem));
        when(repository.save(any(ProdutoImagem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(imageStoragePort.resolveUrl(PRODUTO_UUID, "abc123_thumb.jpg"))
                .thenReturn("/uploads/products/" + PRODUTO_UUID + "/abc123_thumb.jpg");
        when(imageStoragePort.resolveUrl(PRODUTO_UUID, "abc123_card.jpg"))
                .thenReturn("/uploads/products/" + PRODUTO_UUID + "/abc123_card.jpg");
        when(imageStoragePort.resolveUrl(PRODUTO_UUID, "abc123_full.jpg"))
                .thenReturn("/uploads/products/" + PRODUTO_UUID + "/abc123_full.jpg");

        ImageResponse response = useCase.setMain(PRODUTO_UUID, IMAGE_ID);

        assertThat(response.id()).isEqualTo(IMAGE_ID);
        assertThat(response.filename()).isEqualTo("abc123");
        assertThat(response.originalName()).isEqualTo("photo.jpg");
        assertThat(response.contentType()).isEqualTo("image/jpeg");
        assertThat(response.fileSize()).isEqualTo(200_000L);
        assertThat(response.sortOrder()).isEqualTo(2);
        assertThat(response.main()).isTrue();
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.thumbnailUrl()).contains("abc123_thumb.jpg");
        assertThat(response.cardUrl()).contains("abc123_card.jpg");
        assertThat(response.fullUrl()).contains("abc123_full.jpg");
    }

    @Test
    @DisplayName("Should resolve correct extension for PNG image")
    void shouldResolveCorrectExtensionForPng() {
        ProdutoImagem imagem = ProdutoImagem.restore(
                2L, PRODUTO_UUID, "def456", "logo.png",
                "image/png", 100_000L, 0, false, Instant.now());

        when(repository.findByIdAndProdutoUuid(2L, PRODUTO_UUID))
                .thenReturn(Optional.of(imagem));
        when(repository.save(any(ProdutoImagem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(imageStoragePort.resolveUrl(eq(PRODUTO_UUID), anyString()))
                .thenAnswer(inv -> "/uploads/products/" + PRODUTO_UUID + "/" + inv.getArgument(1));

        ImageResponse response = useCase.setMain(PRODUTO_UUID, 2L);

        assertThat(response.thumbnailUrl()).contains("def456_thumb.png");
        assertThat(response.cardUrl()).contains("def456_card.png");
        assertThat(response.fullUrl()).contains("def456_full.png");
    }

    @Test
    @DisplayName("Should resolve correct extension for WebP image")
    void shouldResolveCorrectExtensionForWebp() {
        ProdutoImagem imagem = ProdutoImagem.restore(
                3L, PRODUTO_UUID, "ghi789", "banner.webp",
                "image/webp", 150_000L, 0, false, Instant.now());

        when(repository.findByIdAndProdutoUuid(3L, PRODUTO_UUID))
                .thenReturn(Optional.of(imagem));
        when(repository.save(any(ProdutoImagem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(imageStoragePort.resolveUrl(eq(PRODUTO_UUID), anyString()))
                .thenAnswer(inv -> "/uploads/products/" + PRODUTO_UUID + "/" + inv.getArgument(1));

        ImageResponse response = useCase.setMain(PRODUTO_UUID, 3L);

        assertThat(response.thumbnailUrl()).contains("ghi789_thumb.webp");
        assertThat(response.cardUrl()).contains("ghi789_card.webp");
        assertThat(response.fullUrl()).contains("ghi789_full.webp");
    }
}
