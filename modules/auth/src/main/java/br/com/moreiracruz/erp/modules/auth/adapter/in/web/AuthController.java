package br.com.moreiracruz.erp.modules.auth.adapter.in.web;

import br.com.moreiracruz.erp.modules.auth.domain.model.Credentials;
import br.com.moreiracruz.erp.modules.auth.domain.model.TokenPair;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.LoginUseCase;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.LogoutUseCase;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.RegisterUserCommand;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.RegisterUserUseCase;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.RefreshTokenUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter exposing the auth use cases over HTTP.
 *
 * <p>All endpoints are under {@code /api/v1/auth}.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RegisterUserUseCase registerUserUseCase;

    public AuthController(
            LoginUseCase loginUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            LogoutUseCase logoutUseCase,
            RegisterUserUseCase registerUserUseCase) {
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.registerUserUseCase = registerUserUseCase;
    }

    /**
     * Authenticates a user with username + password and returns a new token pair.
     *
     * @param request login credentials
     * @return 200 OK with {@link TokenPairResponse}
     */
    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> login(@RequestBody LoginRequest request) {
        TokenPair tokenPair = loginUseCase.login(
                new Credentials(request.username(), request.password()));
        return ResponseEntity.ok(toResponse(tokenPair));
    }

    @PostMapping("/register")
    public ResponseEntity<TokenPairResponse> register(@RequestBody RegisterRequest request) {
        TokenPair tokenPair = registerUserUseCase.register(new RegisterUserCommand(
                request.fullName(),
                request.email(),
                request.password(),
                request.phone(),
                request.cpf()));
        return ResponseEntity.ok(toResponse(tokenPair));
    }

    /**
     * Exchanges a valid refresh token for a new token pair.
     *
     * @param request body containing the refresh token
     * @return 200 OK with {@link TokenPairResponse}
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@RequestBody RefreshRequest request) {
        TokenPair tokenPair = refreshTokenUseCase.refresh(request.refreshToken());
        return ResponseEntity.ok(toResponse(tokenPair));
    }

    /**
     * Revokes the provided refresh token, effectively logging the user out.
     *
     * @param request body containing the refresh token to revoke
     * @return 204 No Content
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        logoutUseCase.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    private static TokenPairResponse toResponse(TokenPair tokenPair) {
        return new TokenPairResponse(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.expiresIn());
    }
}
