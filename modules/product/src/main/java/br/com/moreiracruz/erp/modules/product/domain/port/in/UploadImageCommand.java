package br.com.moreiracruz.erp.modules.product.domain.port.in;

import java.util.Arrays;
import java.util.Objects;

/**
 * Command carrying the data for a single image upload.
 */
public record UploadImageCommand(String originalFilename, String contentType, long size, byte[] content) {

    public UploadImageCommand {
        content = content == null ? null : content.clone();
    }

    @Override
    public byte[] content() {
        return content == null ? null : content.clone();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UploadImageCommand that)) {
            return false;
        }
        return size == that.size
                && Objects.equals(originalFilename, that.originalFilename)
                && Objects.equals(contentType, that.contentType)
                && Arrays.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(originalFilename, contentType, size);
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }

    @Override
    public String toString() {
        return "UploadImageCommand[originalFilename=%s, contentType=%s, size=%d, contentLength=%d]"
                .formatted(originalFilename, contentType, size, content == null ? 0 : content.length);
    }
}
