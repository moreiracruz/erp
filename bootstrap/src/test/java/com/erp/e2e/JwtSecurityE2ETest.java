package com.erp.e2e;

import com.erp.test.AbstractIntegrationTest;
import com.erp.test.TestJwtGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-End test verifying JWT security controls.
 * Requirement 7: JWT token validation, expiry, and tampering checks.
 */
@AutoConfigureMockMvc
class JwtSecurityE2ETest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("No token → 401 Unauthorized")
    void noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/products/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Expired token → 401 Unauthorized")
    void expiredToken_returns401() throws Exception {
        UUID userUuid = UUID.randomUUID();
        String expiredToken = TestJwtGenerator.generateExpired(userUuid, "ROLE_MANAGER");

        mockMvc.perform(get("/api/v1/products/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + expiredToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Tampered token → 401 Unauthorized")
    void tamperedToken_returns401() throws Exception {
        UUID userUuid = UUID.randomUUID();
        String tamperedToken = TestJwtGenerator.generateTampered(userUuid, "ROLE_MANAGER");

        mockMvc.perform(get("/api/v1/products/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + tamperedToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Invalid role in token → 403 Forbidden on protected endpoint")
    void invalidRole_returns403() throws Exception {
        String tokenWithInvalidRole = TestJwtGenerator.generateWithInvalidRole("ROLE_INVALID_XYZ");

        // Endpoint that requires ROLE_MANAGER (POST /products)
        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + tokenWithInvalidRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Valid token + correct role → not 401/403")
    void validToken_correctRole_accessGranted() throws Exception {
        UUID userUuid = UUID.randomUUID();
        String validToken = TestJwtGenerator.generateToken(userUuid, "ROLE_MANAGER");

        // GET /products/{uuid} is allowed for ROLE_MANAGER
        // It might return 404 if product doesn't exist, but it should NOT be 401 or 403
        int responseStatus = mockMvc.perform(get("/api/v1/products/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getStatus();

        assertThat(responseStatus).isNotIn(401, 403);
    }
}
