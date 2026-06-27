package com.erp.modules.product.application.usecase;

import com.erp.modules.product.application.config.ImageProperties;
import com.erp.modules.product.domain.model.ProdutoImagem;
import com.erp.modules.product.domain.port.in.ImageResponse;
import com.erp.modules.product.domain.port.in.UploadImageCommand;
import com.erp.modules.product.domain.port.out.ImageProcessorPort;
import com.erp.modules.product.domain.port.out.ImageStoragePort;
import com.erp.modules.product.domain.port.out.ProdutoImagemRepository;
import com.erp.shared.exceptions.ValidationException;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for the image service use case layer.
 *
 * Validates: Requirements 1.10, 3.1, 4.2, 5.1, 5.2, 6.1, 12.4
 */
@Tag("product-images")
class ImageServiceProperties {

    // ── Property 6: Sort order assignment is sequential after current maximum ──

    /**
     * Given any current max sort_order m >= -1 and batch size k >= 1,
     * the newly assigned sort_orders should be m+1, m+2, ..., m+k.
     *
     * Validates: Requirements 1.10
     */
    @Property(tries = 20)
    @Label("Property 6: Sort order assignment is sequential after current maximum")
    void sortOrderAssignmentIsSequentialAfterCurrentMaximum(
            @ForAll @IntRange(min = -1, max = 100) int currentMaxSortOrder,
            @ForAll @IntRange(min = 1, max = 5) int batchSize) {

        UUID produtoUuid = UUID.randomUUID();

        ProdutoImagemRepository repository = mock(ProdutoImagemRepository.class);
        ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);
        ImageProcessorPort imageProcessorPort = mock(ImageProcessorPort.class);
        ImageProperties imageProperties = new ImageProperties();

        when(repository.countByProdutoUuid(produtoUuid)).thenReturn(1); // at least 1 so not first
        when(repository.findMaxSortOrderByProdutoUuid(produtoUuid)).thenReturn(currentMaxSortOrder);
        when(repository.sumFileSizeByProdutoUuid(produtoUuid)).thenReturn(0L);
        when(imageProcessorPort.resize(any(), any())).thenReturn(
                Map.of("thumb", new byte[10], "card", new byte[10], "full", new byte[10]));
        when(imageStoragePort.resolveUrl(any(), any())).thenReturn("http://test/img.jpg");
        when(repository.save(any(ProdutoImagem.class))).thenAnswer(invocation -> {
            ProdutoImagem img = invocation.getArgument(0);
            return ProdutoImagem.restore(
                    1L, img.getProdutoUuid(), img.getFilename(), img.getOriginalName(),
                    img.getContentType(), img.getFileSize(), img.getSortOrder(),
                    img.isMain(), Instant.now());
        });

        UploadImageUseCaseImpl useCase = new UploadImageUseCaseImpl(
                repository, imageStoragePort, imageProcessorPort, imageProperties);

        // Build a batch of valid JPEG upload commands
        byte[] jpegContent = createMinimalJpegBytes();
        List<UploadImageCommand> commands = IntStream.range(0, batchSize)
                .mapToObj(i -> new UploadImageCommand("photo" + i + ".jpg", "image/jpeg", 1000L, jpegContent))
                .toList();

        List<ImageResponse> responses = useCase.upload(produtoUuid, commands);

        // Verify sort orders are sequential starting from currentMaxSortOrder + 1
        List<Integer> sortOrders = responses.stream()
                .map(ImageResponse::sortOrder)
                .toList();

        List<Integer> expected = IntStream.rangeClosed(currentMaxSortOrder + 1, currentMaxSortOrder + batchSize)
                .boxed()
                .toList();

        assertThat(sortOrders).isEqualTo(expected);
    }

    // ── Property 10: Image listing is always sorted by sort_order ascending ──

    /**
     * Given any list of ProdutoImagem with random sort_orders,
     * ListImagesUseCaseImpl.listByProduct returns them sorted ascending by sortOrder.
     *
     * Validates: Requirements 3.1
     */
    @Property(tries = 20)
    @Label("Property 10: Image listing is always sorted by sort_order ascending")
    void imageListingIsAlwaysSortedBySortOrderAscending(
            @ForAll("randomImageList") List<ProdutoImagem> images) {

        UUID produtoUuid = UUID.randomUUID();

        ProdutoImagemRepository repository = mock(ProdutoImagemRepository.class);
        ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);

        // Simulate repository returning images already sorted (as the contract requires)
        List<ProdutoImagem> sortedImages = images.stream()
                .sorted(Comparator.comparingInt(ProdutoImagem::getSortOrder))
                .toList();
        when(repository.findByProdutoUuidOrderBySortOrder(produtoUuid)).thenReturn(sortedImages);
        when(imageStoragePort.resolveUrl(any(), any())).thenReturn("http://test/img.jpg");

        ListImagesUseCaseImpl useCase = new ListImagesUseCaseImpl(repository, imageStoragePort);

        List<ImageResponse> result = useCase.listByProduct(produtoUuid);

        // Verify ascending sort_order in output
        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i).sortOrder())
                    .isLessThanOrEqualTo(result.get(i + 1).sortOrder());
        }
    }

    // ── Property 11: Main image promotion selects lowest sort_order ──

    /**
     * Given a list of images where one is main, after delete of the main image,
     * the image with lowest sort_order among remaining becomes main.
     *
     * Validates: Requirements 4.2
     */
    @Property(tries = 20)
    @Label("Property 11: Main image promotion selects lowest sort_order")
    void mainImagePromotionSelectsLowestSortOrder(
            @ForAll @IntRange(min = 1, max = 9) int remainingCount) {

        UUID produtoUuid = UUID.randomUUID();
        Long mainImageId = 100L;

        // Create the main image to be deleted
        ProdutoImagem mainImage = ProdutoImagem.restore(
                mainImageId, produtoUuid, "main0", "main.jpg",
                "image/jpeg", 1000L, 0, true, Instant.now());

        // Create remaining images with random sort_orders
        Random random = new Random();
        List<ProdutoImagem> remaining = new ArrayList<>();
        Set<Integer> usedSortOrders = new HashSet<>();
        for (int i = 0; i < remainingCount; i++) {
            int sortOrder;
            do {
                sortOrder = random.nextInt(1, 100);
            } while (!usedSortOrders.add(sortOrder));

            remaining.add(ProdutoImagem.restore(
                    (long) (i + 1), produtoUuid, "img" + i, "photo" + i + ".jpg",
                    "image/jpeg", 1000L, sortOrder, false, Instant.now()));
        }

        // Sort remaining by sort_order (as the repository would return them)
        remaining.sort(Comparator.comparingInt(ProdutoImagem::getSortOrder));

        ProdutoImagemRepository repository = mock(ProdutoImagemRepository.class);
        ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);

        when(repository.findByIdAndProdutoUuid(mainImageId, produtoUuid))
                .thenReturn(Optional.of(mainImage));
        when(repository.findByProdutoUuidOrderBySortOrder(produtoUuid))
                .thenReturn(remaining);
        when(repository.save(any(ProdutoImagem.class))).thenAnswer(inv -> inv.getArgument(0));

        DeleteImageUseCaseImpl useCase = new DeleteImageUseCaseImpl(repository, imageStoragePort);

        useCase.delete(produtoUuid, mainImageId);

        // Verify the promoted image is the one with lowest sort_order
        var saveCaptor = org.mockito.ArgumentCaptor.forClass(ProdutoImagem.class);
        verify(repository).save(saveCaptor.capture());
        ProdutoImagem promoted = saveCaptor.getValue();

        int expectedLowestSortOrder = remaining.stream()
                .mapToInt(ProdutoImagem::getSortOrder)
                .min()
                .orElseThrow();

        assertThat(promoted.isMain()).isTrue();
        assertThat(promoted.getSortOrder()).isEqualTo(expectedLowestSortOrder);
    }

    // ── Property 12: Reorder maps array positions to sort_order values ──

    /**
     * Given any permutation of image IDs, after reorder, each image at position i
     * has sortOrder == i.
     *
     * Validates: Requirements 5.1
     */
    @Property(tries = 20)
    @Label("Property 12: Reorder maps array positions to sort_order values")
    void reorderMapsArrayPositionsToSortOrderValues(
            @ForAll @IntRange(min = 1, max = 10) int imageCount) {

        UUID produtoUuid = UUID.randomUUID();

        // Create images with sequential IDs
        List<ProdutoImagem> images = new ArrayList<>();
        for (int i = 0; i < imageCount; i++) {
            images.add(ProdutoImagem.restore(
                    (long) (i + 1), produtoUuid, "img" + i, "photo" + i + ".jpg",
                    "image/jpeg", 1000L, i, i == 0, Instant.now()));
        }

        // Generate a random permutation of the IDs
        List<Long> ids = images.stream().map(ProdutoImagem::getId).collect(Collectors.toList());
        Collections.shuffle(ids);

        ProdutoImagemRepository repository = mock(ProdutoImagemRepository.class);
        ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);

        when(repository.findByProdutoUuidOrderBySortOrder(produtoUuid)).thenReturn(new ArrayList<>(images));
        when(repository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(imageStoragePort.resolveUrl(any(), any())).thenReturn("http://test/img.jpg");

        ReorderImagesUseCaseImpl useCase = new ReorderImagesUseCaseImpl(repository, imageStoragePort);

        List<ImageResponse> result = useCase.reorder(produtoUuid, ids);

        // Verify each image at position i in the result has sortOrder == i
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i).sortOrder()).isEqualTo(i);
        }

        // Also verify that the order of IDs in the result matches the requested order
        List<Long> resultIds = result.stream().map(ImageResponse::id).toList();
        assertThat(resultIds).isEqualTo(ids);
    }

    // ── Property 13: Reorder rejects incomplete or excessive ID lists ──

    /**
     * Given image IDs [1,2,3], any list that is not an exact permutation
     * (missing, extra, duplicates) causes ValidationException.
     *
     * Validates: Requirements 5.2
     */
    @Property(tries = 20)
    @Label("Property 13: Reorder rejects incomplete or excessive ID lists")
    void reorderRejectsIncompleteOrExcessiveIdLists(
            @ForAll("invalidPermutations") List<Long> invalidIds) {

        UUID produtoUuid = UUID.randomUUID();

        // Fixed set of 3 images with IDs 1, 2, 3
        List<ProdutoImagem> images = List.of(
                ProdutoImagem.restore(1L, produtoUuid, "img1", "p1.jpg", "image/jpeg", 1000L, 0, true, Instant.now()),
                ProdutoImagem.restore(2L, produtoUuid, "img2", "p2.jpg", "image/jpeg", 1000L, 1, false, Instant.now()),
                ProdutoImagem.restore(3L, produtoUuid, "img3", "p3.jpg", "image/jpeg", 1000L, 2, false, Instant.now())
        );

        ProdutoImagemRepository repository = mock(ProdutoImagemRepository.class);
        ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);

        when(repository.findByProdutoUuidOrderBySortOrder(produtoUuid)).thenReturn(images);

        ReorderImagesUseCaseImpl useCase = new ReorderImagesUseCaseImpl(repository, imageStoragePort);

        assertThatThrownBy(() -> useCase.reorder(produtoUuid, invalidIds))
                .isInstanceOf(ValidationException.class);
    }

    // ── Property 14: At most one main image per product (uniqueness invariant) ──

    /**
     * After any sequence of setMain operations, at most one image has is_main=true.
     *
     * Validates: Requirements 6.1, 12.4
     */
    @Property(tries = 20)
    @Label("Property 14: At most one main image per product (uniqueness invariant)")
    void atMostOneMainImagePerProduct(
            @ForAll @IntRange(min = 2, max = 8) int imageCount,
            @ForAll @IntRange(min = 1, max = 10) int operationCount) {

        UUID produtoUuid = UUID.randomUUID();

        // Create images in memory — simulating a real repository state
        List<ProdutoImagem> images = new ArrayList<>();
        for (int i = 0; i < imageCount; i++) {
            images.add(ProdutoImagem.restore(
                    (long) (i + 1), produtoUuid, "img" + i, "photo" + i + ".jpg",
                    "image/jpeg", 1000L, i, i == 0, Instant.now()));
        }

        ProdutoImagemRepository repository = mock(ProdutoImagemRepository.class);
        ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);

        // clearMainByProdutoUuid sets all images to not main
        doAnswer(inv -> {
            images.forEach(img -> img.setMain(false));
            return null;
        }).when(repository).clearMainByProdutoUuid(produtoUuid);

        when(imageStoragePort.resolveUrl(any(), any())).thenReturn("http://test/img.jpg");

        SetMainImageUseCaseImpl useCase = new SetMainImageUseCaseImpl(repository, imageStoragePort);

        Random random = new Random();

        // Perform random setMain operations
        for (int op = 0; op < operationCount; op++) {
            int targetIdx = random.nextInt(imageCount);
            ProdutoImagem target = images.get(targetIdx);
            Long targetId = target.getId();

            when(repository.findByIdAndProdutoUuid(targetId, produtoUuid))
                    .thenReturn(Optional.of(target));
            when(repository.save(any(ProdutoImagem.class))).thenAnswer(inv -> inv.getArgument(0));

            useCase.setMain(produtoUuid, targetId);

            // After each operation, verify at most one image is main
            long mainCount = images.stream().filter(ProdutoImagem::isMain).count();
            assertThat(mainCount).isLessThanOrEqualTo(1);

            // Since we have at least one image, exactly one should be main
            assertThat(mainCount).isEqualTo(1);

            // The main image should be the one we just set
            assertThat(target.isMain()).isTrue();
        }
    }

    // ── Arbitraries ──────────────────────────────────────────────────────────

    @Provide
    Arbitrary<List<ProdutoImagem>> randomImageList() {
        return Arbitraries.integers().between(1, 10).flatMap(count -> {
            UUID produtoUuid = UUID.randomUUID();
            return Arbitraries.integers().between(0, 100)
                    .list().ofSize(count)
                    .map(sortOrders -> {
                        List<ProdutoImagem> images = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            images.add(ProdutoImagem.restore(
                                    (long) (i + 1), produtoUuid, "img" + i, "photo" + i + ".jpg",
                                    "image/jpeg", 1000L, sortOrders.get(i),
                                    i == 0, Instant.now()));
                        }
                        return images;
                    });
        });
    }

    @Provide
    Arbitrary<List<Long>> invalidPermutations() {
        // Generate lists that are NOT exact permutations of [1, 2, 3]
        List<List<Long>> invalidCases = List.of(
                List.of(1L, 2L),            // Missing elements
                List.of(1L, 2L, 3L, 4L),    // Extra elements
                List.of(1L, 1L, 3L),        // Duplicates
                List.of(1L, 2L, 99L),       // Wrong IDs
                List.of(),                   // Empty list
                List.of(1L),                // Single element
                List.of(2L, 2L, 2L),        // All duplicates
                List.of(1L, 2L, 3L, 3L)    // Extra with duplicates
        );
        return Arbitraries.of(invalidCases);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Creates minimal valid JPEG bytes (starts with FF D8 FF magic bytes).
     */
    private byte[] createMinimalJpegBytes() {
        byte[] bytes = new byte[100];
        bytes[0] = (byte) 0xFF;
        bytes[1] = (byte) 0xD8;
        bytes[2] = (byte) 0xFF;
        bytes[3] = (byte) 0xE0; // APP0 marker
        return bytes;
    }
}
