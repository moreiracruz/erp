package br.com.moreiracruz.erp.modules.product.adapter.out.storage;

import br.com.moreiracruz.erp.modules.product.application.config.ImageProperties;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates: Requirements 2.5
 *
 * Property 9: Storage path construction follows naming convention.
 * For any product UUID and base filename, resolveUrl(produtoUuid, filename) should return
 * a string matching the pattern /uploads/products/{produtoUuid}/{filename}.
 */
@Tag("product-images")
class ImageStoragePathProperties {

    private final LocalImageStorageAdapter adapter;

    ImageStoragePathProperties() {
        ImageProperties props = new ImageProperties();
        props.setBasePath("./uploads/products");
        this.adapter = new LocalImageStorageAdapter(props);
    }

    @Property(tries = 20)
    @Label("Feature: product-images, Property 9: Storage path construction follows naming convention")
    void storagePathFollowsNamingConvention(
            @ForAll("productUuids") UUID produtoUuid,
            @ForAll("validFilenames") String filename) {

        String url = adapter.resolveUrl(produtoUuid, filename);

        // The URL starts with /uploads/products/
        assertThat(url).startsWith("/uploads/products/");

        // Contains the UUID string representation
        assertThat(url).contains(produtoUuid.toString());

        // Ends with the provided filename
        assertThat(url).endsWith(filename);

        // Has the exact format /uploads/products/{UUID}/{filename}
        String expected = "/uploads/products/" + produtoUuid + "/" + filename;
        assertThat(url).isEqualTo(expected);
    }

    @Provide
    Arbitrary<UUID> productUuids() {
        return Arbitraries.create(UUID::randomUUID);
    }

    @Provide
    Arbitrary<String> validFilenames() {
        Arbitrary<String> baseNames = Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(4).ofMaxLength(12);

        Arbitrary<String> sizeSuffixes = Arbitraries.of("thumb", "card", "full");

        Arbitrary<String> extensions = Arbitraries.of("jpg", "png", "webp");

        return Combinators.combine(baseNames, sizeSuffixes, extensions)
                .as((base, suffix, ext) -> base + "_" + suffix + "." + ext);
    }
}
