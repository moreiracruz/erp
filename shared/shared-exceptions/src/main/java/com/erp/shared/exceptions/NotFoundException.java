package com.erp.shared.exceptions;

/**
 * Thrown when a requested resource cannot be found. Maps to HTTP 404 Not Found.
 */
public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(message);
    }
}
