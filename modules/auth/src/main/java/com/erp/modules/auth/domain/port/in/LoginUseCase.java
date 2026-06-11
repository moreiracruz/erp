package com.erp.modules.auth.domain.port.in;

import com.erp.modules.auth.domain.model.Credentials;
import com.erp.modules.auth.domain.model.TokenPair;

/**
 * Inbound port: authenticate a user with username + password credentials.
 */
public interface LoginUseCase {

    /**
     * Authenticates the user and issues a new token pair.
     *
     * @param credentials raw login credentials
     * @return a {@link TokenPair} containing the JWT and refresh token
     * @throws com.erp.shared.exceptions.AuthenticationException if credentials are invalid or the account is locked
     */
    TokenPair login(Credentials credentials);
}
