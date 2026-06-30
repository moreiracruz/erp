package br.com.moreiracruz.erp.modules.auth.application.usecase;

import br.com.moreiracruz.erp.modules.auth.domain.port.in.ActivateUserCommand;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.ActivateUserUseCase;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.ActivationTokenRepository;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.UsuarioRepository;
import br.com.moreiracruz.erp.shared.exceptions.AuthenticationException;
import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ActivateUserUseCaseImpl implements ActivateUserUseCase {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final ActivationTokenRepository activationTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ActivateUserUseCaseImpl(ActivationTokenRepository activationTokenRepository,
                                   UsuarioRepository usuarioRepository,
                                   BCryptPasswordEncoder passwordEncoder) {
        this.activationTokenRepository = activationTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void activate(ActivateUserCommand command) {
        validate(command);
        String tokenHash = TokenHasher.sha256hex(command.token());
        var activationToken = activationTokenRepository.findByTokenHash(tokenHash)
                .filter(token -> token.isValid())
                .orElseThrow(() -> new AuthenticationException("Token de ativação inválido"));

        var usuario = usuarioRepository.findByUuid(activationToken.getUsuarioUuid())
                .orElseThrow(() -> new AuthenticationException("Token de ativação inválido"));

        usuario.activateWithPasswordHash(passwordEncoder.encode(command.password()));
        activationToken.markUsed();
        usuarioRepository.save(usuario);
        activationTokenRepository.save(activationToken);
        activationTokenRepository.markActiveTokensUsed(usuario.getUuid(), activationToken.getPurpose());
    }

    private void validate(ActivateUserCommand command) {
        if (command == null || command.token() == null || command.token().isBlank()) {
            throw new ValidationException("Token de ativação é obrigatório");
        }
        if (command.password() == null || command.password().length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Senha deve ter pelo menos 8 caracteres");
        }
    }
}
