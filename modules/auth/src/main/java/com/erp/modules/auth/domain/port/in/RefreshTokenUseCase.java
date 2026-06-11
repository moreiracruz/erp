package com.erp.modules.auth.domain.port.in;

import com.erp.modules.auth.domain.model.TokenPair;

/**
 * Inbound port: exchange a valid refresh token for a new token pair.
 */
public interface RefreshTokenUseCase {

    /**
     * Validates the presented refresh token, revokes it, and issues a new token pair.
     *
     * @param rawRefreshToken the opaque refresh token string received from the client
     * @return a new {@link TokenPair}
     * @throws com.erp.shared.exceptions.AuthenticationException if the token is unknown, already revoked, or expired
     */
    TokenPair refresh(String rawRefreshToken);
}
