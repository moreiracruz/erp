package com.erp.shared.exceptions;

/**
 * Thrown when a date range is invalid (e.g. start date is after end date).
 * Maps to HTTP 400 Bad Request.
 */
public class DateRangeException extends BusinessException {

    public DateRangeException(String message) {
        super(message);
    }
}
