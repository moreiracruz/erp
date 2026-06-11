package com.erp.modules.auth.application.usecase;

import com.erp.modules.auth.domain.model.Credentials;
import com.erp.modules.auth.domain.model.RefreshToken;
import com.erp.modules.auth.domain.model.TokenPair;
import com.erp.modules.auth.domain.port.in.LoginUseCase;
import com.erp.modules.auth.domain.port.out.JwtPort;
import com.erp.modules.auth.domain.port.out.RefreshTokenRepositoryPort;
import com.erp.modules.auth.domain.port.out.UsuarioRepository;
import com.erp.shared.exceptions.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that authenticates a user with username/password credentials
 * and issues a new {@link TokenPair}.
 *
 * <p>Lockout logic is delegated entirely to the {@link com.erp.modules.auth.domain.model.Usuario}
 * domain aggregate — this class only orchestrates ports and factories.
 */
@Service
@Transactional
public class LoginUseCaseImpl implements LoginUseCase {

    private static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 900L;

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final JwtPort jwtPort;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-expiration-days:7}")
    private int refreshExpirationDays;

    public LoginUseCaseImpl(UsuarioRepository usuarioRepository,
                            RefreshTokenRepositoryPort refreshTokenRepository,
                            JwtPort jwtPort,
                            BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtPort = jwtPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public TokenPair login(Credentials credentials) {
        // 1. Load usuario — treat absence the same as wrong password to avoid username enumeration
        var usuario = usuarioRepository.findByUsername(credentials.username())
                .orElseThrow(() -> new AuthenticationException("Credenciais inválidas"));

        // 2. Locked account — return generic message
        if (usuario.isLocked()) {
            throw new AuthenticationException("Credenciais inválidas");
        }

        // 3. Password verification
        if (!passwordEncoder.matches(credentials.password(), usuario.getPasswordHash())) {
            usuario.recordFailedAttempt();
            usuarioRepository.save(usuario);
            throw new AuthenticationException("Credenciais inválidas");
        }

        // 4. Successful authentication — clear lockout state
        usuario.resetAttempts();
        usuarioRepository.save(usuario);

        // 5. Issue JWT
        String jwt = jwtPort.generateToken(usuario.getUuid(), usuario.getRole().name());

        // 6. Generate refresh token (raw stays with client; only hash persisted)
        String rawRefreshToken = RandomTokenGenerator.generate();
        String tokenHash = TokenHasher.sha256hex(rawRefreshToken);

        // 7. Persist refresh token
        RefreshToken refreshToken = RefreshToken.create(usuario.getUuid(), tokenHash, refreshExpirationDays);
        refreshTokenRepository.save(refreshToken);

        // 8. Return token pair
        return new TokenPair(jwt, rawRefreshToken, ACCESS_TOKEN_EXPIRES_IN_SECONDS);
    }
}
