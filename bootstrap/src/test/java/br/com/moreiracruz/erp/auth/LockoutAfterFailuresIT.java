package br.com.moreiracruz.erp.auth;

import br.com.moreiracruz.erp.modules.auth.domain.model.Credentials;
import br.com.moreiracruz.erp.modules.auth.domain.model.Role;
import br.com.moreiracruz.erp.modules.auth.domain.model.Usuario;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.LoginUseCase;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.UsuarioRepository;
import br.com.moreiracruz.erp.shared.exceptions.AuthenticationException;
import br.com.moreiracruz.erp.test.AbstractIntegrationTest;
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
        // 1. Submit 6 wrong passwords (threshold is 5, locks on 5th failure)
        for (int i = 0; i < 6; i++) {
            try {
                loginUseCase.login(new Credentials(USERNAME, WRONG_PASSWORD));
            } catch (AuthenticationException e) {
                // expected
            }
        }

        // 2. Verify DB state: account should be locked
        Usuario lockedUser = usuarioRepository.findByUsername(USERNAME).orElseThrow();
        assertThat(lockedUser.getFailedAttempts()).isGreaterThanOrEqualTo(5);
        assertThat(lockedUser.getLockedUntil()).isNotNull();
        assertThat(lockedUser.isLocked()).isTrue();

        // 3. Submit correct password → still rejected because account is locked
        assertThatThrownBy(() -> loginUseCase.login(new Credentials(USERNAME, CORRECT_PASSWORD)))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("Account is locked after 5 failed attempts — lockout engaged")
    void accountLockedAfterExactlyFiveFailures() {
        // Submit 5 wrong passwords (threshold)
        for (int i = 0; i < 5; i++) {
            try {
                loginUseCase.login(new Credentials(USERNAME, WRONG_PASSWORD));
            } catch (AuthenticationException e) {
                // expected
            }
        }

        // Verify account is now locked
        Usuario lockedUser = usuarioRepository.findByUsername(USERNAME).orElseThrow();
        assertThat(lockedUser.getFailedAttempts()).isGreaterThanOrEqualTo(5);
        assertThat(lockedUser.getLockedUntil()).isNotNull();
        assertThat(lockedUser.isLocked()).isTrue();
    }
}
