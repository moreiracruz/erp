package com.erp.shared.exceptions;

/**
 * Thrown when an operation conflicts with existing data (e.g. overlapping campaign dates).
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message);
    }
}
