package br.com.moreiracruz.erp.modules.product.adapter.out.storage;

/**
 * Thrown when image processing (resize/format conversion) fails due to an I/O or library error.
 * Maps to HTTP 500 in the global exception handler.
 */
public class ImageProcessingException extends RuntimeException {

    public ImageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
