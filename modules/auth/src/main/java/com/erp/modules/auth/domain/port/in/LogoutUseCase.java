package com.erp.modules.auth.domain.port.in;

/**
 * Inbound port: invalidate an active refresh token (user-initiated logout).
 */
public interface LogoutUseCase {

    /**
     * Revokes the given refresh token if it is still valid.
     *
     * <p>Silently ignores unknown or already-revoked tokens — the operation is
     * always idempotent from the caller's perspective.
     *
     * @param rawRefreshToken the opaque refresh token string received from the client
     */
    void logout(String rawRefreshToken);
}
