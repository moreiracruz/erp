package br.com.moreiracruz.erp.modules.product.adapter.out.storage;

import br.com.moreiracruz.erp.modules.product.application.config.ImageProperties;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ImageProcessorPort;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Infrastructure adapter that uses Thumbnailator to resize images into
 * multiple size variants (thumbnail, card, full).
 *
 * <p>JPEG images are output at 85% quality. PNG images are preserved as PNG.
 * WebP images fall back to PNG output since Thumbnailator does not natively support WebP writing.
 */
@Component
public class ThumbnailatorImageProcessorAdapter implements ImageProcessorPort {

    private static final double JPEG_QUALITY = 0.85;
    private static final String CONTENT_TYPE_JPEG = "image/jpeg";
    private static final String CONTENT_TYPE_PNG = "image/png";
    private static final String CONTENT_TYPE_WEBP = "image/webp";

    private static final Map<String, String> SIZE_KEY_TO_SUFFIX = Map.of(
            "thumbnail", "thumb",
            "card", "card",
            "full", "full"
    );

    private final ImageProperties imageProperties;

    public ThumbnailatorImageProcessorAdapter(ImageProperties imageProperties) {
        this.imageProperties = imageProperties;
    }

    @Override
    public Map<String, byte[]> resize(byte[] originalContent, String contentType) {
        String outputFormat = resolveOutputFormat(contentType);
        double quality = resolveQuality(contentType);

        Map<String, byte[]> result = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> sizeEntry : imageProperties.getSizes().entrySet()) {
            String sizeKey = sizeEntry.getKey();
            int targetWidth = sizeEntry.getValue();
            String suffix = SIZE_KEY_TO_SUFFIX.getOrDefault(sizeKey, sizeKey);

            byte[] resized = resizeToWidth(originalContent, targetWidth, outputFormat, quality);
            result.put(suffix, resized);
        }

        return result;
    }

    private byte[] resizeToWidth(byte[] content, int targetWidth, String outputFormat, double quality) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(content);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Thumbnails.of(bais)
                    .width(targetWidth)
                    .keepAspectRatio(true)
                    .outputFormat(outputFormat)
                    .outputQuality(quality)
                    .toOutputStream(baos);

            return baos.toByteArray();
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to resize image to width " + targetWidth, e);
        }
    }

    /**
     * Determines the output format string for Thumbnailator based on the original content type.
     * WebP falls back to PNG since Thumbnailator does not natively support WebP output.
     */
    private String resolveOutputFormat(String contentType) {
        if (contentType == null) {
            return "jpeg";
        }
        return switch (contentType.toLowerCase()) {
            case CONTENT_TYPE_JPEG -> "jpeg";
            case CONTENT_TYPE_PNG -> "png";
            case CONTENT_TYPE_WEBP -> "png"; // fallback: Thumbnailator doesn't support WebP write
            default -> "jpeg";
        };
    }

    /**
     * Resolves the output quality. Only JPEG uses a reduced quality setting (85%).
     * PNG and WebP (output as PNG) use maximum quality since PNG is lossless.
     */
    private double resolveQuality(String contentType) {
        if (CONTENT_TYPE_JPEG.equalsIgnoreCase(contentType)) {
            return JPEG_QUALITY;
        }
        return 1.0;
    }
}
