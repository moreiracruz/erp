package br.com.moreiracruz.erp.modules.product.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;

import java.util.Set;

/**
 * Utility class providing validation logic for product image uploads.
 *
 * <p>Validates file content (magic bytes), file size, and filename safety.
 * All methods are static and throw {@link ValidationException} on failure.
 */
public final class ImageValidator {

    /** Maximum allowed file size: 5MB in bytes. */
    public static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5,242,880 bytes

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    // Magic byte signatures
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] RIFF_MAGIC = {0x52, 0x49, 0x46, 0x46}; // "RIFF"
    private static final byte[] WEBP_MARKER = {0x57, 0x45, 0x42, 0x50}; // "WEBP" at offset 8

    private ImageValidator() {
        // utility class
    }

    /**
     * Validates that the given byte array starts with magic bytes corresponding
     * to an accepted image format (JPEG, PNG, or WebP).
     *
     * @param content the raw file bytes (at least the first 12 bytes needed)
     * @throws ValidationException if the content does not match any accepted format
     */
    public static void validateMagicBytes(byte[] content) {
        if (content == null || content.length < 4) {
            throw new ValidationException("Tipo de arquivo não aceito: conteúdo inválido");
        }

        if (isJpeg(content) || isPng(content) || isWebp(content)) {
            return;
        }

        throw new ValidationException("Tipo de arquivo não aceito. Aceitos: JPEG, PNG, WebP");
    }

    /**
     * Detects the content type from magic bytes.
     *
     * @param content the raw file bytes
     * @return the detected MIME type, or {@code null} if unrecognized
     */
    public static String detectContentType(byte[] content) {
        if (content == null || content.length < 4) {
            return null;
        }
        if (isJpeg(content)) {
            return "image/jpeg";
        }
        if (isPng(content)) {
            return "image/png";
        }
        if (isWebp(content)) {
            return "image/webp";
        }
        return null;
    }

    /**
     * Validates that the file size does not exceed the 5MB limit.
     *
     * @param fileSize the file size in bytes
     * @throws ValidationException if the file exceeds 5MB
     */
    public static void validateFileSize(long fileSize) {
        if (fileSize <= 0) {
            throw new ValidationException("fileSize", "tamanho do arquivo deve ser maior que zero");
        }
        if (fileSize > MAX_FILE_SIZE) {
            throw new ValidationException("Arquivo muito grande. Tamanho máximo: 5MB");
        }
    }

    /**
     * Validates and sanitizes a filename by checking for path traversal sequences,
     * null bytes, and other dangerous characters.
     *
     * @param filename the filename to validate
     * @throws ValidationException if the filename contains dangerous sequences
     */
    public static void validateFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new ValidationException("filename", "nome do arquivo não pode ser vazio");
        }
        if (filename.contains("\0")) {
            throw new ValidationException("Nome de arquivo inválido");
        }
        if (filename.contains("../") || filename.contains("..\\")) {
            throw new ValidationException("Nome de arquivo inválido");
        }
        if (filename.contains("/") || filename.contains("\\")) {
            throw new ValidationException("Nome de arquivo inválido");
        }
    }

    /**
     * Sanitizes a filename by removing all dangerous characters.
     * Returns a safe version of the filename suitable for storage.
     *
     * <p>Removes: path separators ({@code /}, {@code \}), parent directory references
     * ({@code ..}), null bytes ({@code \0}), and other special characters that could
     * be used for path traversal or injection.
     *
     * @param filename the original filename
     * @return the sanitized filename
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "unnamed";
        }

        // Remove null bytes
        String sanitized = filename.replace("\0", "");

        // Remove path separators and parent directory references
        sanitized = sanitized.replace("/", "");
        sanitized = sanitized.replace("\\", "");
        sanitized = sanitized.replace("..", "");

        // Remove other potentially dangerous characters
        sanitized = sanitized.replaceAll("[<>:\"|?*]", "");

        // Trim leading/trailing dots and spaces
        sanitized = sanitized.replaceAll("^[.\\s]+", "");
        sanitized = sanitized.replaceAll("[.\\s]+$", "");

        if (sanitized.isBlank()) {
            return "unnamed";
        }

        return sanitized;
    }

    /**
     * Checks whether the given content type is in the allowed set.
     *
     * @param contentType the MIME type to check
     * @return {@code true} if the type is accepted
     */
    public static boolean isAllowedContentType(String contentType) {
        return contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase());
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private static boolean isJpeg(byte[] content) {
        return content.length >= 3
                && content[0] == JPEG_MAGIC[0]
                && content[1] == JPEG_MAGIC[1]
                && content[2] == JPEG_MAGIC[2];
    }

    private static boolean isPng(byte[] content) {
        return content.length >= 4
                && content[0] == PNG_MAGIC[0]
                && content[1] == PNG_MAGIC[1]
                && content[2] == PNG_MAGIC[2]
                && content[3] == PNG_MAGIC[3];
    }

    private static boolean isWebp(byte[] content) {
        // WebP format: starts with "RIFF" (4 bytes), then 4 bytes file size, then "WEBP"
        if (content.length < 12) {
            return false;
        }
        return content[0] == RIFF_MAGIC[0]
                && content[1] == RIFF_MAGIC[1]
                && content[2] == RIFF_MAGIC[2]
                && content[3] == RIFF_MAGIC[3]
                && content[8] == WEBP_MARKER[0]
                && content[9] == WEBP_MARKER[1]
                && content[10] == WEBP_MARKER[2]
                && content[11] == WEBP_MARKER[3];
    }
}
