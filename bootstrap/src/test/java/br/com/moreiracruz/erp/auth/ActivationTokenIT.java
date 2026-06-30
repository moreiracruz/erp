package br.com.moreiracruz.erp.auth;

import br.com.moreiracruz.erp.modules.auth.application.usecase.TokenHasher;
import br.com.moreiracruz.erp.modules.auth.domain.model.ActivationToken;
import br.com.moreiracruz.erp.modules.auth.domain.model.ActivationTokenPurpose;
import br.com.moreiracruz.erp.modules.auth.domain.model.Credentials;
import br.com.moreiracruz.erp.modules.auth.domain.model.Role;
import br.com.moreiracruz.erp.modules.auth.domain.model.Usuario;
import br.com.moreiracruz.erp.modules.auth.domain.model.UsuarioStatus;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.LoginUseCase;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.ActivationTokenRepository;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.UsuarioRepository;
import br.com.moreiracruz.erp.shared.exceptions.AuthenticationException;
import br.com.moreiracruz.erp.test.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ActivationTokenIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private LoginUseCase loginUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Activation token is stored hashed, activates pending user once, and enables login")
    void activationTokenActivatesPendingUserOnce() throws Exception {
        Usuario usuario = usuarioRepository.save(Usuario.createPendingActivation(
                "bootstrap-admin@example.com",
                "{pending}",
                Role.ROLE_SUPER_ADMIN));
        String rawToken = java.util.UUID.randomUUID() + "-" + java.util.UUID.randomUUID();
        activationTokenRepository.save(ActivationToken.create(
                usuario.getUuid(),
                TokenHasher.sha256hex(rawToken),
                ActivationTokenPurpose.BOOTSTRAP_SUPER_ADMIN,
                Instant.now().plus(30, ChronoUnit.MINUTES)));

        assertThatThrownBy(() -> loginUseCase.login(new Credentials("bootstrap-admin@example.com", "CorrectPass123!")))
                .isInstanceOf(AuthenticationException.class);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from user_activation_tokens where token_hash = ?",
                Integer.class,
                rawToken)).isZero();

        mockMvc.perform(post("/api/v1/auth/activation")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s",
                                  "password": "CorrectPass123!"
                                }
                                """.formatted(rawToken)))
                .andExpect(status().isNoContent());

        Usuario activated = usuarioRepository.findByUsername("bootstrap-admin@example.com").orElseThrow();
        assertThat(activated.getStatus()).isEqualTo(UsuarioStatus.ACTIVE);
        assertThat(activated.isActive()).isTrue();
        assertThat(loginUseCase.login(new Credentials("bootstrap-admin@example.com", "CorrectPass123!")))
                .isNotNull();

        mockMvc.perform(post("/api/v1/auth/activation")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s",
                                  "password": "AnotherPass123!"
                                }
                                """.formatted(rawToken)))
                .andExpect(status().isUnauthorized());
    }
}
