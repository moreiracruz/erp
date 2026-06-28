package br.com.moreiracruz.erp.modules.product.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ImageValidator")
class ImageValidatorTest {

    // ── Magic bytes fixtures ─────────────────────────────────────────────────

    private static final byte[] VALID_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
    private static final byte[] VALID_PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] VALID_WEBP = {
            0x52, 0x49, 0x46, 0x46,   // RIFF
            0x00, 0x00, 0x00, 0x00,   // file size (placeholder)
            0x57, 0x45, 0x42, 0x50    // WEBP
    };
    private static final byte[] INVALID_GIF = {0x47, 0x49, 0x46, 0x38}; // GIF89

    @Nested
    @DisplayName("validateMagicBytes()")
    class MagicBytesValidation {

        @Test
        @DisplayName("accepts valid JPEG magic bytes")
        void acceptsJpeg() {
            assertThatCode(() -> ImageValidator.validateMagicBytes(VALID_JPEG))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("accepts valid PNG magic bytes")
        void acceptsPng() {
            assertThatCode(() -> ImageValidator.validateMagicBytes(VALID_PNG))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("accepts valid WebP magic bytes")
        void acceptsWebp() {
            assertThatCode(() -> ImageValidator.validateMagicBytes(VALID_WEBP))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rejects GIF magic bytes")
        void rejectsGif() {
            assertThatThrownBy(() -> ImageValidator.validateMagicBytes(INVALID_GIF))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("rejects null content")
        void rejectsNull() {
            assertThatThrownBy(() -> ImageValidator.validateMagicBytes(null))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("rejects content shorter than 4 bytes")
        void rejectsTooShort() {
            assertThatThrownBy(() -> ImageValidator.validateMagicBytes(new byte[]{0x00, 0x01}))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("rejects random bytes")
        void rejectsRandom() {
            byte[] random = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C};
            assertThatThrownBy(() -> ImageValidator.validateMagicBytes(random))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("detectContentType()")
    class ContentTypeDetection {

        @Test
        @DisplayName("detects JPEG")
        void detectsJpeg() {
            assertThat(ImageValidator.detectContentType(VALID_JPEG)).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("detects PNG")
        void detectsPng() {
            assertThat(ImageValidator.detectContentType(VALID_PNG)).isEqualTo("image/png");
        }

        @Test
        @DisplayName("detects WebP")
        void detectsWebp() {
            assertThat(ImageValidator.detectContentType(VALID_WEBP)).isEqualTo("image/webp");
        }

        @Test
        @DisplayName("returns null for unknown format")
        void returnsNullForUnknown() {
            assertThat(ImageValidator.detectContentType(INVALID_GIF)).isNull();
        }
    }

    @Nested
    @DisplayName("validateFileSize()")
    class FileSizeValidation {

        @Test
        @DisplayName("accepts size at exactly 5MB")
        void acceptsExactLimit() {
            assertThatCode(() -> ImageValidator.validateFileSize(5L * 1024 * 1024))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("accepts 1 byte file")
        void acceptsOneByteFile() {
            assertThatCode(() -> ImageValidator.validateFileSize(1L))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rejects file exceeding 5MB")
        void rejectsOverLimit() {
            assertThatThrownBy(() -> ImageValidator.validateFileSize(5L * 1024 * 1024 + 1))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("rejects zero-byte file")
        void rejectsZeroSize() {
            assertThatThrownBy(() -> ImageValidator.validateFileSize(0L))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("rejects negative size")
        void rejectsNegativeSize() {
            assertThatThrownBy(() -> ImageValidator.validateFileSize(-1L))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("validateFilename()")
    class FilenameValidation {

        @Test
        @DisplayName("accepts simple filename")
        void acceptsSimpleFilename() {
            assertThatCode(() -> ImageValidator.validateFilename("photo123"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rejects filename with forward slash")
        void rejectsForwardSlash() {
            assertThatThrownBy(() -> ImageValidator.validateFilename("path/file"))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("rejects filename with backslash")
        void rejectsBackslash() {
            assertThatThrownBy(() -> ImageValidator.validateFilename("path\\file"))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("rejects filename with path traversal")
        void rejectsPathTraversal() {
            assertThatThrownBy(() -> ImageValidator.validateFilename("../etc/passwd"))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("rejects filename with null byte")
        void rejectsNullByte() {
            assertThatThrownBy(() -> ImageValidator.validateFilename("file\0name"))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("rejects empty filename")
        void rejectsEmpty() {
            assertThatThrownBy(() -> ImageValidator.validateFilename(""))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("rejects null filename")
        void rejectsNull() {
            assertThatThrownBy(() -> ImageValidator.validateFilename(null))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("sanitizeFilename()")
    class FilenameSanitization {

        @Test
        @DisplayName("removes null bytes")
        void removesNullBytes() {
            String result = ImageValidator.sanitizeFilename("file\0name.jpg");
            assertThat(result).doesNotContain("\0");
        }

        @Test
        @DisplayName("removes path separators")
        void removesPathSeparators() {
            String result = ImageValidator.sanitizeFilename("../path/to\\file.jpg");
            assertThat(result).doesNotContain("/").doesNotContain("\\");
        }

        @Test
        @DisplayName("removes parent directory references")
        void removesParentDirRefs() {
            String result = ImageValidator.sanitizeFilename("../../file.jpg");
            assertThat(result).doesNotContain("..");
        }

        @Test
        @DisplayName("returns 'unnamed' for null input")
        void returnsUnnamedForNull() {
            assertThat(ImageValidator.sanitizeFilename(null)).isEqualTo("unnamed");
        }

        @Test
        @DisplayName("returns 'unnamed' for blank input")
        void returnsUnnamedForBlank() {
            assertThat(ImageValidator.sanitizeFilename("   ")).isEqualTo("unnamed");
        }

        @Test
        @DisplayName("preserves safe filename unchanged")
        void preservesSafeFilename() {
            assertThat(ImageValidator.sanitizeFilename("photo123.jpg")).isEqualTo("photo123.jpg");
        }
    }

    @Nested
    @DisplayName("isAllowedContentType()")
    class ContentTypeCheck {

        @Test
        @DisplayName("accepts image/jpeg")
        void acceptsJpeg() {
            assertThat(ImageValidator.isAllowedContentType("image/jpeg")).isTrue();
        }

        @Test
        @DisplayName("accepts image/png")
        void acceptsPng() {
            assertThat(ImageValidator.isAllowedContentType("image/png")).isTrue();
        }

        @Test
        @DisplayName("accepts image/webp")
        void acceptsWebp() {
            assertThat(ImageValidator.isAllowedContentType("image/webp")).isTrue();
        }

        @Test
        @DisplayName("rejects image/gif")
        void rejectsGif() {
            assertThat(ImageValidator.isAllowedContentType("image/gif")).isFalse();
        }

        @Test
        @DisplayName("rejects null")
        void rejectsNull() {
            assertThat(ImageValidator.isAllowedContentType(null)).isFalse();
        }
    }
}
