package com.erp.auth;

import com.erp.modules.auth.domain.model.Credentials;
import com.erp.modules.auth.domain.model.Role;
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
 * Integration test verifying brute-force lockout after N>5 failures.
 * Property 6: Account locks after 5+ consecutive failed attempts.
 */
class LockoutAfterFailuresIT extends AbstractIntegrationTest {

    @Autowired
    private LoginUseCase loginUseCase;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final String USERNAME = "lockout-test@example.com";
    private static final String CORRECT_PASSWORD = "CorrectPass123!";
    private static final String WRONG_PASSWORD = "WrongPassword!";

    @BeforeEach
    void setUp() {
        Usuario user = Usuario.create(USERNAME, passwordEncoder.encode(CORRECT_PASSWORD), Role.ROLE_CASHIER);
        usuarioRepository.save(user);
    }

    @Test
    @DisplayName("Account is locked after 6 consecutive failed login attempts — correct password rejected")
    void accountLockedAfterSixFailures_correctPasswordStillRejected() {
        // 1. Submit 6 wrong passwords
        for (int i = 0; i < 6; i++) {
            assertThatThrownBy(() -> loginUseCase.login(new Credentials(USERNAME, WRONG_PASSWORD)))
                    .isInstanceOf(AuthenticationException.class);
        }

        // 2. Submit correct password → still rejected because account is locked
        assertThatThrownBy(() -> loginUseCase.login(new Credentials(USERNAME, CORRECT_PASSWORD)))
                .isInstanceOf(AuthenticationException.class);

        // 3. Verify DB state: failed_attempts >= 5, locked_until is future
        Usuario lockedUser = usuarioRepository.findByUsername(USERNAME).orElseThrow();
        assertThat(lockedUser.getFailedAttempts()).isGreaterThanOrEqualTo(5);
        assertThat(lockedUser.getLockedUntil()).isNotNull();
        assertThat(lockedUser.isLocked()).isTrue();
    }

    @Test
    @DisplayName("Account is locked after exactly 5 failed attempts")
    void accountLockedAfterExactlyFiveFailures() {
        // Submit 5 wrong passwords (threshold)
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> loginUseCase.login(new Credentials(USERNAME, WRONG_PASSWORD)))
                    .isInstanceOf(AuthenticationException.class);
        }

        // Verify account is now locked
        Usuario lockedUser = usuarioRepository.findByUsername(USERNAME).orElseThrow();
        assertThat(lockedUser.getFailedAttempts()).isEqualTo(5);
        assertThat(lockedUser.getLockedUntil()).isNotNull();
        assertThat(lockedUser.isLocked()).isTrue();
    }
}
