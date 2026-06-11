package com.erp.auth;

import com.erp.modules.auth.domain.model.Credentials;
import com.erp.modules.auth.domain.model.TokenPair;
import com.erp.modules.auth.domain.model.Role;
import com.erp.modules.auth.domain.model.Usuario;
import com.erp.modules.auth.domain.port.in.LoginUseCase;
import com.erp.modules.auth.domain.port.in.RefreshTokenUseCase;
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
 * Integration test verifying refresh token rotation correctness.
 * Property 4: After refresh, old token is invalid.
 */
class RefreshTokenRotationIT extends AbstractIntegrationTest {

    @Autowired
    private LoginUseCase loginUseCase;

    @Autowired
    private RefreshTokenUseCase refreshTokenUseCase;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final String USERNAME = "rotation-test@example.com";
    private static final String PASSWORD = "SecurePass123!";

    @BeforeEach
    void setUp() {
        Usuario user = Usuario.create(USERNAME, passwordEncoder.encode(PASSWORD), Role.ROLE_CASHIER);
        usuarioRepository.save(user);
    }

    @Test
    @DisplayName("After refresh, old refresh token is invalidated and cannot be reused")
    void afterRefresh_oldTokenIsInvalid() {
        // 1. Login to get initial token pair
        TokenPair initialPair = loginUseCase.login(new Credentials(USERNAME, PASSWORD));
        assertThat(initialPair.refreshToken()).isNotBlank();

        // 2. Refresh with the initial refresh token → get new pair
        TokenPair newPair = refreshTokenUseCase.refresh(initialPair.refreshToken());
        assertThat(newPair.accessToken()).isNotBlank();
        assertThat(newPair.refreshToken()).isNotBlank();
        assertThat(newPair.refreshToken()).isNotEqualTo(initialPair.refreshToken());

        // 3. Try to use the OLD refresh token again → assert 401
        assertThatThrownBy(() -> refreshTokenUseCase.refresh(initialPair.refreshToken()))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("New refresh token from rotation is valid for subsequent refresh")
    void newRefreshToken_isValidForSubsequentRefresh() {
        TokenPair initialPair = loginUseCase.login(new Credentials(USERNAME, PASSWORD));
        TokenPair rotatedPair = refreshTokenUseCase.refresh(initialPair.refreshToken());

        // The new refresh token should work
        TokenPair thirdPair = refreshTokenUseCase.refresh(rotatedPair.refreshToken());
        assertThat(thirdPair.accessToken()).isNotBlank();
        assertThat(thirdPair.refreshToken()).isNotBlank();
    }
}
