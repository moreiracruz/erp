package br.com.moreiracruz.erp.modules.auth.application.usecase;

import br.com.moreiracruz.erp.modules.auth.domain.model.RefreshToken;
import br.com.moreiracruz.erp.modules.auth.domain.model.TokenPair;
import br.com.moreiracruz.erp.modules.auth.domain.model.Usuario;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.RefreshTokenUseCase;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.JwtPort;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.RefreshTokenRepositoryPort;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.UsuarioRepository;
import br.com.moreiracruz.erp.shared.exceptions.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that rotates a refresh token and issues a new {@link TokenPair}.
 *
 * <p>Refresh token rotation: the presented token is revoked immediately and a brand-new
 * token pair is returned. This limits the window in which a stolen refresh token can be
 * reused.
 */
@Service
@Transactional
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 900L;

    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtPort jwtPort;

    @Value("${jwt.refresh-expiration-days:7}")
    private int refreshExpirationDays;

    public RefreshTokenUseCaseImpl(RefreshTokenRepositoryPort refreshTokenRepository,
                                   UsuarioRepository usuarioRepository,
                                   JwtPort jwtPort) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.jwtPort = jwtPort;
    }

    @Override
    public TokenPair refresh(String rawRefreshToken) {
        // 1. Hash the raw token to look up in the database
        String hash = TokenHasher.sha256hex(rawRefreshToken);

        // 2. Load and validate refresh token
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .filter(RefreshToken::isValid)
                .orElseThrow(() -> new AuthenticationException("Credenciais inválidas"));

        // 3. Revoke the old token (refresh token rotation)
        token.revoke();
        refreshTokenRepository.save(token);

        // 4. Load the owning user
        Usuario usuario = usuarioRepository.findByUuid(token.getUsuarioUuid())
                .orElseThrow(() -> new AuthenticationException("Credenciais inválidas"));

        // 5. Issue new JWT
        String jwt = jwtPort.generateToken(usuario.getUuid(), usuario.getRole().name());

        // 6. Generate and persist new refresh token
        String newRawToken = RandomTokenGenerator.generate();
        String newHash = TokenHasher.sha256hex(newRawToken);
        RefreshToken newRefreshToken = RefreshToken.create(usuario.getUuid(), newHash, refreshExpirationDays);
        refreshTokenRepository.save(newRefreshToken);

        // 7. Return new token pair
        return new TokenPair(jwt, newRawToken, ACCESS_TOKEN_EXPIRES_IN_SECONDS);
    }
}
