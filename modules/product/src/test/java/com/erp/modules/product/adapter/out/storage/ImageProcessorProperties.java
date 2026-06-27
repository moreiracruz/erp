package com.erp.modules.product.adapter.out.storage;

import com.erp.modules.product.application.config.ImageProperties;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Tag;
import net.jqwik.api.constraints.IntRange;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates: Requirements 2.1, 2.2, 2.3, 2.4
 */
class ImageProcessorProperties {

    private ThumbnailatorImageProcessorAdapter createProcessor() {
        ImageProperties props = new ImageProperties();
        props.setSizes(Map.of(
                "thumbnail", 200,
                "card", 400,
                "full", 800
        ));
        return new ThumbnailatorImageProcessorAdapter(props);
    }

    private byte[] createJpegImage(int width, int height) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Fill with a solid color to make it a valid image
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                img.setRGB(x, y, 0x336699);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpeg", baos);
        return baos.toByteArray();
    }

    private byte[] createPngImage(int width, int height) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                img.setRGB(x, y, 0xFF336699);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    /**
     * Property 7: Image resize preserves aspect ratio and produces correct widths.
     *
     * For any source JPEG image of size W×H, the processor produces 3 variants
     * with widths exactly 200, 400, 800, and heights approximately round(target_width * H / W)
     * within ±1px tolerance.
     *
     * Validates: Requirements 2.1, 2.2
     */
    @Property(tries = 20)
    @Tag("product-images")
    @Label("Property 7: Image resize preserves aspect ratio and produces correct widths")
    void resizePreservesAspectRatioAndProducesCorrectWidths(
            @ForAll @IntRange(min = 800, max = 2000) int sourceWidth,
            @ForAll @IntRange(min = 50, max = 2000) int sourceHeight) throws IOException {

        byte[] jpegContent = createJpegImage(sourceWidth, sourceHeight);
        ThumbnailatorImageProcessorAdapter processor = createProcessor();

        Map<String, byte[]> variants = processor.resize(jpegContent, "image/jpeg");

        assertThat(variants).hasSize(3);
        assertThat(variants).containsKeys("thumb", "card", "full");

        int[] targetWidths = {200, 400, 800};
        String[] keys = {"thumb", "card", "full"};

        for (int i = 0; i < targetWidths.length; i++) {
            int targetWidth = targetWidths[i];
            String key = keys[i];
            byte[] resizedBytes = variants.get(key);

            BufferedImage resized = ImageIO.read(new ByteArrayInputStream(resizedBytes));
            assertThat(resized).as("Resized image for key '%s' should not be null", key).isNotNull();

            // Width should be exactly target
            assertThat(resized.getWidth())
                    .as("Width for '%s' variant should be exactly %d", key, targetWidth)
                    .isEqualTo(targetWidth);

            // Height should maintain aspect ratio within ±1px tolerance
            long expectedHeight = Math.round((double) targetWidth * sourceHeight / sourceWidth);
            assertThat(resized.getHeight())
                    .as("Height for '%s' variant should be ~%d (aspect ratio preserved)", key, expectedHeight)
                    .isBetween((int) expectedHeight - 1, (int) expectedHeight + 1);
        }
    }

    /**
     * Property 8: Non-JPEG format is preserved through resize.
     *
     * For any source PNG image, the output format should be PNG (verified by checking
     * PNG magic bytes: 0x89 0x50 0x4E 0x47 in the output).
     * For WebP, the implementation falls back to PNG, which is also acceptable.
     *
     * Validates: Requirements 2.3, 2.4
     */
    @Property(tries = 20)
    @Tag("product-images")
    @Label("Property 8: Non-JPEG format is preserved through resize")
    void nonJpegFormatIsPreservedThroughResize(
            @ForAll @IntRange(min = 800, max = 1500) int sourceWidth,
            @ForAll @IntRange(min = 50, max = 1500) int sourceHeight) throws IOException {

        byte[] pngContent = createPngImage(sourceWidth, sourceHeight);
        ThumbnailatorImageProcessorAdapter processor = createProcessor();

        Map<String, byte[]> variants = processor.resize(pngContent, "image/png");

        assertThat(variants).hasSize(3);

        // PNG magic bytes: 0x89 0x50 0x4E 0x47
        byte[] pngMagic = {(byte) 0x89, 0x50, 0x4E, 0x47};

        for (Map.Entry<String, byte[]> entry : variants.entrySet()) {
            byte[] resizedBytes = entry.getValue();
            assertThat(resizedBytes.length).as("Resized '%s' variant should have content", entry.getKey())
                    .isGreaterThan(4);

            byte[] firstFourBytes = new byte[4];
            System.arraycopy(resizedBytes, 0, firstFourBytes, 0, 4);
            assertThat(firstFourBytes)
                    .as("Resized '%s' variant should have PNG magic bytes", entry.getKey())
                    .isEqualTo(pngMagic);
        }
    }

    /**
     * Property 8 (WebP fallback): WebP content type falls back to PNG output.
     *
     * Validates: Requirements 2.4
     */
    @Property(tries = 20)
    @Tag("product-images")
    @Label("Property 8b: WebP format falls back to PNG through resize")
    void webpFormatFallsToPngThroughResize(
            @ForAll @IntRange(min = 800, max = 1500) int sourceWidth,
            @ForAll @IntRange(min = 50, max = 1500) int sourceHeight) throws IOException {

        // Since Java's ImageIO doesn't natively write WebP, we create a PNG image
        // but pass "image/webp" as content type to verify the adapter's fallback behavior
        byte[] pngContent = createPngImage(sourceWidth, sourceHeight);
        ThumbnailatorImageProcessorAdapter processor = createProcessor();

        Map<String, byte[]> variants = processor.resize(pngContent, "image/webp");

        assertThat(variants).hasSize(3);

        // WebP falls back to PNG output, so output should have PNG magic bytes
        byte[] pngMagic = {(byte) 0x89, 0x50, 0x4E, 0x47};

        for (Map.Entry<String, byte[]> entry : variants.entrySet()) {
            byte[] resizedBytes = entry.getValue();
            assertThat(resizedBytes.length).as("Resized '%s' variant should have content", entry.getKey())
                    .isGreaterThan(4);

            byte[] firstFourBytes = new byte[4];
            System.arraycopy(resizedBytes, 0, firstFourBytes, 0, 4);
            assertThat(firstFourBytes)
                    .as("Resized '%s' variant (from WebP) should have PNG magic bytes (fallback)", entry.getKey())
                    .isEqualTo(pngMagic);
        }
    }
}
