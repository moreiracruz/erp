package br.com.moreiracruz.erp.auth;

import br.com.moreiracruz.erp.modules.auth.domain.model.Credentials;
import br.com.moreiracruz.erp.modules.auth.domain.model.Role;
import br.com.moreiracruz.erp.modules.auth.domain.model.Usuario;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.LoginUseCase;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.UsuarioRepository;
import br.com.moreiracruz.erp.shared.exceptions.AuthenticationException;
import br.com.moreiracruz.erp.test.AbstractIntegrationTest;
import br.com.moreiracruz.erp.test.TestJwtGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class SystemUserAdminIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoginUseCase loginUseCase;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Manager can create and list system users without exposing password hashes")
    void managerCreatesAndListsSystemUsers() throws Exception {
        String managerToken = TestJwtGenerator.generateToken(UUID.randomUUID(), "ROLE_MANAGER");

        mockMvc.perform(post("/api/v1/system/users")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "new-cashier@example.com",
                                  "password": "CorrectPass123!",
                                  "role": "ROLE_CASHIER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").isNotEmpty())
                .andExpect(jsonPath("$.username").value("new-cashier@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_CASHIER"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(get("/api/v1/system/users")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username", hasItem("new-cashier@example.com")))
                .andExpect(jsonPath("$[*].passwordHash", not(hasItem(org.hamcrest.Matchers.anything()))));
    }

    @Test
    @DisplayName("Deactivated user cannot login with correct credentials")
    void deactivatedUserCannotLogin() throws Exception {
        Usuario user = Usuario.create(
                "inactive-user@example.com",
                passwordEncoder.encode("CorrectPass123!"),
                Role.ROLE_CASHIER);
        user = usuarioRepository.save(user);

        UUID managerUuid = UUID.randomUUID();
        String managerToken = TestJwtGenerator.generateToken(managerUuid, "ROLE_MANAGER");

        mockMvc.perform(post("/api/v1/system/users/{uuid}/deactivate", user.getUuid())
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        assertThatThrownBy(() -> loginUseCase.login(new Credentials(
                        "inactive-user@example.com",
                        "CorrectPass123!")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    @DisplayName("Manager cannot deactivate their own user")
    void managerCannotDeactivateSelf() throws Exception {
        Usuario manager = Usuario.create(
                "self-manager@example.com",
                passwordEncoder.encode("CorrectPass123!"),
                Role.ROLE_MANAGER);
        manager = usuarioRepository.save(manager);

        String managerToken = TestJwtGenerator.generateToken(manager.getUuid(), "ROLE_MANAGER");

        mockMvc.perform(post("/api/v1/system/users/{uuid}/deactivate", manager.getUuid())
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().is4xxClientError());

        Usuario persisted = usuarioRepository.findByUuid(manager.getUuid()).orElseThrow();
        assertThat(persisted.isActive()).isTrue();
    }
}
