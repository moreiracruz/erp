package br.com.moreiracruz.erp.modules.product.adapter.out.storage;

import br.com.moreiracruz.erp.modules.product.application.config.ImageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalImageStorageAdapterTest {

    @TempDir
    Path tempDir;

    private LocalImageStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        ImageProperties props = new ImageProperties();
        props.setBasePath(tempDir.toString());
        adapter = new LocalImageStorageAdapter(props);
    }

    @Test
    void store_createsDirectoriesAndWritesFile() throws Exception {
        UUID produtoUuid = UUID.randomUUID();
        String filename = "image_thumb.jpg";
        byte[] content = "fake image data".getBytes();

        adapter.store(produtoUuid, filename, content);

        Path expectedPath = tempDir.resolve(produtoUuid.toString()).resolve(filename);
        assertThat(expectedPath).exists();
        assertThat(Files.readAllBytes(expectedPath)).isEqualTo(content);
    }

    @Test
    void store_overwritesExistingFile() throws Exception {
        UUID produtoUuid = UUID.randomUUID();
        String filename = "image_full.png";
        byte[] original = "original".getBytes();
        byte[] updated = "updated content".getBytes();

        adapter.store(produtoUuid, filename, original);
        adapter.store(produtoUuid, filename, updated);

        Path expectedPath = tempDir.resolve(produtoUuid.toString()).resolve(filename);
        assertThat(Files.readAllBytes(expectedPath)).isEqualTo(updated);
    }

    @Test
    void delete_removesExistingFile() throws Exception {
        UUID produtoUuid = UUID.randomUUID();
        String filename = "to_delete.jpg";
        byte[] content = "data".getBytes();

        adapter.store(produtoUuid, filename, content);
        Path filePath = tempDir.resolve(produtoUuid.toString()).resolve(filename);
        assertThat(filePath).exists();

        adapter.delete(produtoUuid, filename);
        assertThat(filePath).doesNotExist();
    }

    @Test
    void delete_doesNotThrowWhenFileDoesNotExist() {
        UUID produtoUuid = UUID.randomUUID();
        // Should not throw even if file never existed
        adapter.delete(produtoUuid, "nonexistent.jpg");
    }

    @Test
    void deleteAll_removesMultipleFiles() throws Exception {
        UUID produtoUuid = UUID.randomUUID();
        adapter.store(produtoUuid, "img_thumb.jpg", "t".getBytes());
        adapter.store(produtoUuid, "img_card.jpg", "c".getBytes());
        adapter.store(produtoUuid, "img_full.jpg", "f".getBytes());

        adapter.deleteAll(produtoUuid, List.of("img_thumb.jpg", "img_card.jpg", "img_full.jpg"));

        Path dir = tempDir.resolve(produtoUuid.toString());
        assertThat(dir.resolve("img_thumb.jpg")).doesNotExist();
        assertThat(dir.resolve("img_card.jpg")).doesNotExist();
        assertThat(dir.resolve("img_full.jpg")).doesNotExist();
    }

    @Test
    void resolveUrl_returnsCorrectUrlPath() {
        UUID produtoUuid = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        String filename = "abc123_thumb.jpg";

        String url = adapter.resolveUrl(produtoUuid, filename);

        assertThat(url).isEqualTo("/uploads/products/a1b2c3d4-e5f6-7890-abcd-ef1234567890/abc123_thumb.jpg");
    }

    @Test
    void resolveUrl_doesNotDependOnFilesystem() {
        UUID produtoUuid = UUID.randomUUID();
        // resolveUrl should work even if no file was stored
        String url = adapter.resolveUrl(produtoUuid, "any_file.png");

        assertThat(url).startsWith("/uploads/products/");
        assertThat(url).contains(produtoUuid.toString());
        assertThat(url).endsWith("any_file.png");
    }
}
