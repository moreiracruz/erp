package br.com.moreiracruz.erp.e2e;

import br.com.moreiracruz.erp.test.AbstractIntegrationTest;
import br.com.moreiracruz.erp.test.RbacTestCase;
import br.com.moreiracruz.erp.test.TestJwtGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-End test verifying RBAC enforcement across all protected endpoints.
 * Property 5: RBAC matrix — authorized roles get 2xx, unauthorized get 403, unauthenticated get 401.
 */
@AutoConfigureMockMvc
class RbacEnforcementE2ETest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest(name = "{0}")
    @MethodSource("rbacTestCases")
    @DisplayName("RBAC enforcement")
    void rbacEnforcement(RbacTestCase testCase) throws Exception {
        MockHttpServletRequestBuilder request = buildRequest(testCase);

        if (testCase.role() == null) {
            // No token — expect 401
            mockMvc.perform(request
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        } else {
            String token = TestJwtGenerator.generateToken(UUID.randomUUID(), testCase.role());
            int expectedStatus = testCase.expectedStatus();

            var result = mockMvc.perform(request
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();

            if (expectedStatus == 200) {
                assertThat(result.getResponse().getStatus())
                        .isNotIn(401, 403)
                        .isLessThan(500);
            } else {
                assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
            }
        }
    }

    static Stream<RbacTestCase> rbacTestCases() {
        return Stream.of(
                // Products - ROLE_MANAGER allowed
                RbacTestCase.allowed("POST", "/api/v1/products", "ROLE_MANAGER"),
                RbacTestCase.forbidden("POST", "/api/v1/products", "ROLE_USER"),
                RbacTestCase.forbidden("POST", "/api/v1/products", "ROLE_CASHIER"),
                RbacTestCase.forbidden("POST", "/api/v1/products", "ROLE_STOCK"),
                RbacTestCase.forbidden("POST", "/api/v1/products", "ROLE_FINANCE"),

                // Inventory - ROLE_MANAGER and ROLE_STOCK allowed
                RbacTestCase.allowed("GET", "/api/v1/inventory/variants/" + UUID.randomUUID() + "/stock", "ROLE_MANAGER"),
                RbacTestCase.allowed("GET", "/api/v1/inventory/variants/" + UUID.randomUUID() + "/stock", "ROLE_STOCK"),
                RbacTestCase.forbidden("GET", "/api/v1/inventory/variants/" + UUID.randomUUID() + "/stock", "ROLE_USER"),
                RbacTestCase.forbidden("GET", "/api/v1/inventory/variants/" + UUID.randomUUID() + "/stock", "ROLE_CASHIER"),
                RbacTestCase.forbidden("GET", "/api/v1/inventory/variants/" + UUID.randomUUID() + "/stock", "ROLE_FINANCE"),

                // Sales - ROLE_MANAGER and ROLE_CASHIER allowed
                RbacTestCase.allowed("POST", "/api/v1/sales", "ROLE_MANAGER"),
                RbacTestCase.allowed("POST", "/api/v1/sales", "ROLE_CASHIER"),
                RbacTestCase.forbidden("POST", "/api/v1/sales", "ROLE_USER"),
                RbacTestCase.forbidden("POST", "/api/v1/sales", "ROLE_STOCK"),
                RbacTestCase.forbidden("POST", "/api/v1/sales", "ROLE_FINANCE"),

                // Customers - ROLE_MANAGER and ROLE_CASHIER allowed
                RbacTestCase.allowed("POST", "/api/v1/customers", "ROLE_MANAGER"),
                RbacTestCase.allowed("POST", "/api/v1/customers", "ROLE_CASHIER"),
                RbacTestCase.forbidden("POST", "/api/v1/customers", "ROLE_USER"),
                RbacTestCase.forbidden("POST", "/api/v1/customers", "ROLE_STOCK"),
                RbacTestCase.forbidden("POST", "/api/v1/customers", "ROLE_FINANCE"),

                // Consignments - ROLE_MANAGER and ROLE_STOCK can consult; manager controls consignors
                RbacTestCase.allowed("GET", "/api/v1/consignments/contracts", "ROLE_MANAGER"),
                RbacTestCase.allowed("GET", "/api/v1/consignments/contracts", "ROLE_STOCK"),
                RbacTestCase.forbidden("GET", "/api/v1/consignments/contracts", "ROLE_USER"),
                RbacTestCase.forbidden("GET", "/api/v1/consignments/contracts", "ROLE_CASHIER"),
                RbacTestCase.forbidden("GET", "/api/v1/consignments/contracts", "ROLE_FINANCE"),
                RbacTestCase.allowed("POST", "/api/v1/consignments/consignors", "ROLE_MANAGER"),
                RbacTestCase.forbidden("POST", "/api/v1/consignments/consignors", "ROLE_USER"),
                RbacTestCase.forbidden("POST", "/api/v1/consignments/consignors", "ROLE_STOCK"),
                RbacTestCase.forbidden("POST", "/api/v1/consignments/consignors", "ROLE_CASHIER"),
                RbacTestCase.forbidden("POST", "/api/v1/consignments/consignors", "ROLE_FINANCE"),
                RbacTestCase.allowed("GET", "/api/v1/consignments/sent/contracts", "ROLE_MANAGER"),
                RbacTestCase.allowed("GET", "/api/v1/consignments/sent/contracts", "ROLE_STOCK"),
                RbacTestCase.forbidden("GET", "/api/v1/consignments/sent/contracts", "ROLE_USER"),
                RbacTestCase.forbidden("GET", "/api/v1/consignments/sent/contracts", "ROLE_CASHIER"),
                RbacTestCase.forbidden("GET", "/api/v1/consignments/sent/contracts", "ROLE_FINANCE"),
                RbacTestCase.allowed("POST", "/api/v1/consignments/sent/consignees", "ROLE_MANAGER"),
                RbacTestCase.forbidden("POST", "/api/v1/consignments/sent/consignees", "ROLE_USER"),
                RbacTestCase.forbidden("POST", "/api/v1/consignments/sent/consignees", "ROLE_STOCK"),
                RbacTestCase.forbidden("POST", "/api/v1/consignments/sent/consignees", "ROLE_CASHIER"),
                RbacTestCase.forbidden("POST", "/api/v1/consignments/sent/consignees", "ROLE_FINANCE"),

                // System administration - ROLE_MANAGER only
                RbacTestCase.allowed("GET", "/api/v1/system/users", "ROLE_MANAGER"),
                RbacTestCase.forbidden("GET", "/api/v1/system/users", "ROLE_USER"),
                RbacTestCase.forbidden("GET", "/api/v1/system/users", "ROLE_STOCK"),
                RbacTestCase.forbidden("GET", "/api/v1/system/users", "ROLE_CASHIER"),
                RbacTestCase.forbidden("GET", "/api/v1/system/users", "ROLE_FINANCE"),
                RbacTestCase.allowed("POST", "/api/v1/system/users", "ROLE_MANAGER"),
                RbacTestCase.forbidden("POST", "/api/v1/system/users", "ROLE_USER"),
                RbacTestCase.forbidden("POST", "/api/v1/system/users", "ROLE_STOCK"),
                RbacTestCase.forbidden("POST", "/api/v1/system/users", "ROLE_CASHIER"),
                RbacTestCase.forbidden("POST", "/api/v1/system/users", "ROLE_FINANCE"),

                // Unauthenticated access to various endpoints → 401
                new RbacTestCase("POST", "/api/v1/products", null, 401, "Unauthenticated → POST /products"),
                new RbacTestCase("POST", "/api/v1/sales", null, 401, "Unauthenticated → POST /sales"),
                new RbacTestCase("POST", "/api/v1/customers", null, 401, "Unauthenticated → POST /customers"),
                new RbacTestCase("GET", "/api/v1/consignments/contracts", null, 401,
                        "Unauthenticated → GET /consignments/contracts"),
                new RbacTestCase("GET", "/api/v1/consignments/sent/contracts", null, 401,
                        "Unauthenticated → GET /consignments/sent/contracts"),
                new RbacTestCase("GET", "/api/v1/system/users", null, 401,
                        "Unauthenticated → GET /system/users"),
                new RbacTestCase("GET", "/api/v1/inventory/variants/" + UUID.randomUUID() + "/stock",
                        null, 401, "Unauthenticated → GET /inventory")
        );
    }

    private MockHttpServletRequestBuilder buildRequest(RbacTestCase testCase) {
        String path = testCase.path();
        return switch (testCase.httpMethod()) {
            case "GET" -> get(path);
            case "POST" -> post(path).content("{}");
            case "PUT" -> put(path).content("{}");
            case "DELETE" -> delete(path);
            default -> throw new IllegalArgumentException("Unsupported method: " + testCase.httpMethod());
        };
    }

    @Override
    public String toString() {
        return "RbacEnforcementE2ETest";
    }
}
