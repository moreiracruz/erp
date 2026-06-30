package br.com.moreiracruz.erp.bootstrap;

import br.com.moreiracruz.erp.modules.auth.application.usecase.RandomTokenGenerator;
import br.com.moreiracruz.erp.modules.auth.application.usecase.TokenHasher;
import br.com.moreiracruz.erp.modules.auth.domain.model.ActivationToken;
import br.com.moreiracruz.erp.modules.auth.domain.model.ActivationTokenPurpose;
import br.com.moreiracruz.erp.modules.auth.domain.model.Role;
import br.com.moreiracruz.erp.modules.auth.domain.model.Usuario;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.ActivationTokenRepository;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Profile("!test")
public class SuperAdminBootstrapper implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminBootstrapper.class);
    private static final String PENDING_ACTIVATION_CREDENTIAL_MARKER = "{bootstrap-pending-activation}";

    private final UsuarioRepository usuarioRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final String bootstrapEmail;
    private final long tokenTtlMinutes;
    private final String activationBaseUrl;
    private final boolean printActivationLink;

    public SuperAdminBootstrapper(
            UsuarioRepository usuarioRepository,
            ActivationTokenRepository activationTokenRepository,
            @Value("${bootstrap.super-admin.email:admin@empresa.com}") String bootstrapEmail,
            @Value("${bootstrap.super-admin.activation-token-ttl-minutes:30}") long tokenTtlMinutes,
            @Value("${bootstrap.super-admin.activation-base-url:http://localhost:4200/auth/activate}") String activationBaseUrl,
            @Value("${bootstrap.super-admin.print-activation-link:true}") boolean printActivationLink) {
        this.usuarioRepository = usuarioRepository;
        this.activationTokenRepository = activationTokenRepository;
        this.bootstrapEmail = bootstrapEmail;
        this.tokenTtlMinutes = tokenTtlMinutes;
        this.activationBaseUrl = activationBaseUrl;
        this.printActivationLink = printActivationLink;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByRoleIn(List.of(Role.ROLE_SUPER_ADMIN, Role.ROLE_MANAGER))) {
            return;
        }

        Usuario superAdmin = Usuario.createPendingActivation(
                bootstrapEmail,
                PENDING_ACTIVATION_CREDENTIAL_MARKER,
                Role.ROLE_SUPER_ADMIN);
        superAdmin = usuarioRepository.save(superAdmin);

        String rawToken = RandomTokenGenerator.generate();
        ActivationToken activationToken = ActivationToken.create(
                superAdmin.getUuid(),
                TokenHasher.sha256hex(rawToken),
                ActivationTokenPurpose.BOOTSTRAP_SUPER_ADMIN,
                Instant.now().plus(tokenTtlMinutes, ChronoUnit.MINUTES));
        activationTokenRepository.save(activationToken);

        if (printActivationLink) {
            log.warn("Bootstrap SUPER_ADMIN criado em estado pendente. Use uma unica vez: {}?token={}",
                    activationBaseUrl, rawToken);
        } else {
            log.warn("Bootstrap SUPER_ADMIN criado em estado pendente. Configure canal seguro para entregar o token de ativacao.");
        }
    }
}
