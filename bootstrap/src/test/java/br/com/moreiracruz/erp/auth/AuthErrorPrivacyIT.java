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
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * Integration test verifying auth errors don't reveal username existence.
 * Property 13: Auth errors don't expose whether a username exists.
 */
class AuthErrorPrivacyIT extends AbstractIntegrationTest {

    @Autowired
    private LoginUseCase loginUseCase;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final String EXISTING_USERNAME = "existing-user@example.com";
    private static final String NON_EXISTENT_USERNAME = "nobody@example.com";
    private static final String CORRECT_PASSWORD = "CorrectPass123!";
    private static final String WRONG_PASSWORD = "WrongPassword!";

    @BeforeEach
    void setUp() {
        Usuario user = Usuario.create(EXISTING_USERNAME, passwordEncoder.encode(CORRECT_PASSWORD), Role.ROLE_CASHIER);
        usuarioRepository.save(user);
    }

    @Test
    @DisplayName("Login with non-existent username returns generic 'Credenciais inválidas' message")
    void nonExistentUsername_returnsGenericMessage() {
        AuthenticationException ex = catchThrowableOfType(
                () -> loginUseCase.login(new Credentials(NON_EXISTENT_USERNAME, WRONG_PASSWORD)),
                AuthenticationException.class);

        assertThat(ex.getMessage()).isEqualTo("Credenciais inválidas");
    }

    @Test
    @DisplayName("Login with existing username and wrong password returns same generic message")
    void existingUsername_wrongPassword_returnsSameMessage() {
        AuthenticationException ex = catchThrowableOfType(
                () -> loginUseCase.login(new Credentials(EXISTING_USERNAME, WRONG_PASSWORD)),
                AuthenticationException.class);

        assertThat(ex.getMessage()).isEqualTo("Credenciais inválidas");
    }

    @Test
    @DisplayName("Both error responses are identical — neither reveals which field is wrong")
    void bothErrors_areIdentical() {
        AuthenticationException nonExistentEx = catchThrowableOfType(
                () -> loginUseCase.login(new Credentials(NON_EXISTENT_USERNAME, WRONG_PASSWORD)),
                AuthenticationException.class);

        AuthenticationException wrongPasswordEx = catchThrowableOfType(
                () -> loginUseCase.login(new Credentials(EXISTING_USERNAME, WRONG_PASSWORD)),
                AuthenticationException.class);

        // Both messages MUST be identical
        assertThat(nonExistentEx.getMessage()).isEqualTo(wrongPasswordEx.getMessage());

        // Neither should mention 'username', 'password', 'não encontrado', 'incorreta'
        String message = nonExistentEx.getMessage();
        assertThat(message).doesNotContainIgnoringCase("username");
        assertThat(message).doesNotContainIgnoringCase("password");
        assertThat(message).doesNotContainIgnoringCase("não encontrado");
        assertThat(message).doesNotContainIgnoringCase("incorreta");
    }
}
