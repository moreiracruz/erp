package br.com.moreiracruz.erp.modules.auth.domain.port.out;

import br.com.moreiracruz.erp.modules.auth.domain.model.RefreshToken;

import java.util.Optional;

/**
 * Outbound port: persistence operations for {@link RefreshToken}.
 */
public interface RefreshTokenRepositoryPort {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    RefreshToken save(RefreshToken refreshToken);
}
