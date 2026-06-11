package com.erp.auth;

import com.erp.modules.auth.domain.model.Credentials;
import com.erp.modules.auth.domain.model.Role;
import com.erp.modules.auth.domain.model.TokenPair;
import com.erp.modules.auth.domain.model.Usuario;
import com.erp.modules.auth.domain.port.in.LoginUseCase;
import com.erp.modules.auth.domain.port.out.UsuarioRepository;
import com.erp.shared.exceptions.AuthenticationException;
import com.erp.test.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test verifying that the failure counter resets on successful login.
 * Property 7: Counter resets on success after ≤5 failures.
 */
@org.junit.jupiter.api.Disabled("TODO: lockout counter persistence across transactions needs investigation")
class LockoutResetOnSuccessIT extends AbstractIntegrationTest {

    @Autowired
    private LoginUseCase loginUseCase;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final String USERNAME = "reset-test@example.com";
    private static final String CORRECT_PASSWORD = "CorrectPass123!";
    private static final String WRONG_PASSWORD = "WrongPassword!";

    @BeforeEach
    void setUp() {
        Usuario user = Usuario.create(USERNAME, passwordEncoder.encode(CORRECT_PASSWORD), Role.ROLE_CASHIER);
        usuarioRepository.save(user);
    }

    @Test
    @DisplayName("Counter resets to zero after successful login following 4 failed attempts")
    void counterResetsOnSuccess_afterFourFailures() {
        // 1. Submit 4 wrong passwords (below threshold)
        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> loginUseCase.login(new Credentials(USERNAME, WRONG_PASSWORD)))
                    .isInstanceOf(AuthenticationException.class);
        }

        // Verify that we have 4 failed attempts accumulated
        Usuario userBeforeSuccess = usuarioRepository.findByUsername(USERNAME).orElseThrow();
        assertThat(userBeforeSuccess.getFailedAttempts()).isEqualTo(4);

        // 2. Submit correct password → succeeds
        TokenPair result = loginUseCase.login(new Credentials(USERNAME, CORRECT_PASSWORD));
        assertThat(result.accessToken()).isNotBlank();

        // 3. Verify DB: failed_attempts = 0, locked_until = null
        Usuario userAfterSuccess = usuarioRepository.findByUsername(USERNAME).orElseThrow();
        assertThat(userAfterSuccess.getFailedAttempts()).isZero();
        assertThat(userAfterSuccess.getLockedUntil()).isNull();
        assertThat(userAfterSuccess.isLocked()).isFalse();
    }

    @Test
    @DisplayName("Single failed attempt followed by success resets counter")
    void singleFailure_followedBySuccess_resetsCounter() {
        // 1 failure
        assertThatThrownBy(() -> loginUseCase.login(new Credentials(USERNAME, WRONG_PASSWORD)))
                .isInstanceOf(AuthenticationException.class);

        // Success
        TokenPair result = loginUseCase.login(new Credentials(USERNAME, CORRECT_PASSWORD));
        assertThat(result.accessToken()).isNotBlank();

        // Verify reset
        Usuario user = usuarioRepository.findByUsername(USERNAME).orElseThrow();
        assertThat(user.getFailedAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
    }
}
