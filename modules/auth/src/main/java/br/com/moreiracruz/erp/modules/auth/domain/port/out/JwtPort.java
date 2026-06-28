package br.com.moreiracruz.erp.modules.auth.domain.port.out;

import java.util.UUID;

/**
 * Outbound port: issue JWT access tokens.
 */
public interface JwtPort {

    /**
     * Generates a signed HS256 JWT.
     *
     * @param userUuid UUID of the authenticated user (becomes the {@code sub} claim)
     * @param role     role string (e.g., {@code "ROLE_MANAGER"})
     * @return compact JWT string
     */
    String generateToken(UUID userUuid, String role);
}
