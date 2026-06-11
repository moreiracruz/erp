package com.erp.infrastructure.web;

import com.erp.shared.exceptions.AuthenticationException;
import com.erp.shared.exceptions.ConflictException;
import com.erp.shared.exceptions.DateRangeException;
import com.erp.shared.exceptions.InsufficientStockException;
import com.erp.shared.exceptions.NotFoundException;
import com.erp.shared.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralised exception → HTTP response mapping for all REST controllers.
 * Produces a uniform error body:
 * <pre>
 * {
 *   "timestamp" : "2024-01-01T00:00:00Z",
 *   "status"    : 422,
 *   "error"     : "Unprocessable Entity",
 *   "traceId"   : "abc123",
 *   "message"   : "...",
 *   "violations": [ "..." ]
 * }
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // -------------------------------------------------------------------------
    // 422 — Unprocessable Entity
    // -------------------------------------------------------------------------

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, Object> handleValidation(ValidationException ex) {
        return buildBody(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, Object> handleInsufficientStock(InsufficientStockException ex) {
        return buildBody(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, Object> handleConflict(ConflictException ex) {
        return buildBody(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // 404 — Not Found
    // -------------------------------------------------------------------------

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(NotFoundException ex) {
        return buildBody(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // 400 — Bad Request
    // -------------------------------------------------------------------------

    @ExceptionHandler(DateRangeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleDateRange(DateRangeException ex) {
        return buildBody(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // 401 — Unauthorized
    // -------------------------------------------------------------------------

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleAuthentication(AuthenticationException ex) {
        return buildBody(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // 403 — Forbidden
    // -------------------------------------------------------------------------

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleAccessDenied(AccessDeniedException ex) {
        return buildBody(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // 500 — Internal Server Error
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneric(Exception ex) {
        String traceId = traceId();
        log.error("Unhandled exception [traceId={}]", traceId, ex);
        return buildBody(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Map<String, Object> buildBody(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("traceId", traceId());
        body.put("message", message);
        body.put("violations", message != null ? List.of(message) : List.of());
        return body;
    }

    private String traceId() {
        String id = MDC.get("traceId");
        return id != null ? id : "";
    }
}
