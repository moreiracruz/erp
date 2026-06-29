package br.com.moreiracruz.erp.modules.auth.application.usecase;

import br.com.moreiracruz.erp.modules.auth.domain.port.in.LogoutUseCase;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.RefreshTokenRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that revokes a refresh token on user-initiated logout.
 *
 * <p>Intentionally idempotent: if the token is unknown or already revoked the
 * operation succeeds silently, making it safe to call multiple times.
 */
@Service
@Transactional
public class LogoutUseCaseImpl implements LogoutUseCase {

    private final RefreshTokenRepositoryPort refreshTokenRepository;

    public LogoutUseCaseImpl(RefreshTokenRepositoryPort refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        // 1. Hash the raw token to locate the stored record
        String hash = TokenHasher.sha256hex(rawRefreshToken);

        // 2. Revoke only if found and still valid; silently ignore otherwise
        refreshTokenRepository.findByTokenHash(hash)
                .filter(token -> token.isValid())
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                });
    }
}
