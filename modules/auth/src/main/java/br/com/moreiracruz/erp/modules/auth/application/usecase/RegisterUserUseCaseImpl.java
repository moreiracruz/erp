package br.com.moreiracruz.erp.modules.auth.application.usecase;

import br.com.moreiracruz.erp.modules.auth.domain.model.RefreshToken;
import br.com.moreiracruz.erp.modules.auth.domain.model.Role;
import br.com.moreiracruz.erp.modules.auth.domain.model.TokenPair;
import br.com.moreiracruz.erp.modules.auth.domain.model.Usuario;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.RegisterUserCommand;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.RegisterUserUseCase;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.JwtPort;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.RefreshTokenRepositoryPort;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.UsuarioRepository;
import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 900L;

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final JwtPort jwtPort;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-expiration-days:7}")
    private int refreshExpirationDays;

    public RegisterUserUseCaseImpl(UsuarioRepository usuarioRepository,
                                   RefreshTokenRepositoryPort refreshTokenRepository,
                                   JwtPort jwtPort,
                                   BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtPort = jwtPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public TokenPair register(RegisterUserCommand command) {
        String email = normalizeEmail(command.email());
        validatePassword(command.password());
        if (usuarioRepository.findByUsername(email).isPresent()) {
            throw new ValidationException("Usuário já cadastrado");
        }

        Usuario usuario = Usuario.create(email, passwordEncoder.encode(command.password()), Role.ROLE_USER);
        usuario = usuarioRepository.save(usuario);

        String jwt = jwtPort.generateToken(usuario.getUuid(), usuario.getRole().name());
        String rawRefreshToken = RandomTokenGenerator.generate();
        String tokenHash = TokenHasher.sha256hex(rawRefreshToken);
        RefreshToken refreshToken = RefreshToken.create(usuario.getUuid(), tokenHash, refreshExpirationDays);
        refreshTokenRepository.save(refreshToken);

        return new TokenPair(jwt, rawRefreshToken, ACCESS_TOKEN_EXPIRES_IN_SECONDS);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank() || email.length() > 255 || !email.contains("@")) {
            throw new ValidationException("E-mail inválido");
        }
        return email.trim().toLowerCase();
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Senha deve ter pelo menos 8 caracteres");
        }
    }
}
