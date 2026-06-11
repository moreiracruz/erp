package com.erp.shared.exceptions;

/**
 * Base runtime exception for all domain/business rule violations in the ERP system.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
