package br.com.moreiracruz.erp.shared.exceptions;

/**
 * Thrown when authentication fails (invalid credentials, expired token, etc.).
 * Maps to HTTP 401 Unauthorized.
 */
public class AuthenticationException extends BusinessException {

    public AuthenticationException(String message) {
        super(message);
    }
}
