package br.com.moreiracruz.erp.auth;

import br.com.moreiracruz.erp.modules.auth.domain.model.Role;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.UsuarioRepository;
import br.com.moreiracruz.erp.test.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class PublicRegistrationIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Public registration creates ROLE_USER account and returns token pair")
    void publicRegistrationCreatesRoleUser() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Cliente Loja",
                                  "email": "cliente@example.com",
                                  "password": "CorrectPass123!",
                                  "phone": "81999999999",
                                  "cpf": "12345678900"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(900));

        var usuario = usuarioRepository.findByUsername("cliente@example.com").orElseThrow();
        assertThat(usuario.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(usuario.isActive()).isTrue();
    }
}
