package com.erp.shared.exceptions;

/**
 * Thrown when input data fails validation rules. Maps to HTTP 422 Unprocessable Entity.
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(message);
    }

    /**
     * Field-level validation error.
     *
     * @param field   the name of the field that failed validation
     * @param message description of the validation failure
     */
    public ValidationException(String field, String message) {
        super("[" + field + "] " + message);
    }
}
