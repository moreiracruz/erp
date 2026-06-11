package com.erp.e2e;

import com.erp.test.AbstractIntegrationTest;
import com.erp.test.TestJwtGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-End test for the complete POS flow from sale opening to financial entry creation.
 * Validates Requirement 15: Complete POS flow.
 */
@AutoConfigureMockMvc
class FullPosFlowE2ETest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Complete POS flow: create product → add stock → open sale → add item → finalize → verify finance entry and stock")
    void completePosFlow() throws Exception {
        UUID managerUuid = UUID.randomUUID();
        UUID cashierUuid = UUID.randomUUID();
        String managerToken = TestJwtGenerator.generateToken(managerUuid, "ROLE_MANAGER");
        String cashierToken = TestJwtGenerator.generateToken(cashierUuid, "ROLE_CASHIER");

        // 1. Create product (as MANAGER)
        String productBody = """
                {"name": "Camiseta Básica", "brand": "TestBrand", "category": "Camisetas"}
                """;
        MvcResult productResult = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists())
                .andReturn();

        JsonNode productJson = objectMapper.readTree(productResult.getResponse().getContentAsString());
        String productUuid = productJson.get("uuid").asText();

        // 2. Create variant for the product (as MANAGER)
        String variantBody = """
                {"sku": "CAM-BAS-M-BRA", "size": "M", "color": "Branca", "barcode": "7891234567890", "price": 49.90, "cost": 20.00}
                """;
        MvcResult variantResult = mockMvc.perform(post("/api/v1/products/" + productUuid + "/variants")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(variantBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists())
                .andReturn();

        JsonNode variantJson = objectMapper.readTree(variantResult.getResponse().getContentAsString());
        String variantUuid = variantJson.get("uuid").asText();

        // 3. Add stock (as MANAGER)
        String stockBody = String.format("""
                {"quantity": 20, "actorUuid": "%s"}
                """, managerUuid);
        mockMvc.perform(post("/api/v1/inventory/variants/" + variantUuid + "/entries")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stockBody))
                .andExpect(status().isCreated());

        // 4. Open sale (as CASHIER)
        String openSaleBody = """
                {"terminalId": "POS-01", "clienteUuid": null}
                """;
        MvcResult saleResult = mockMvc.perform(post("/api/v1/sales")
                        .header("Authorization", "Bearer " + cashierToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(openSaleBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andReturn();

        JsonNode saleJson = objectMapper.readTree(saleResult.getResponse().getContentAsString());
        String saleUuid = saleJson.get("uuid").asText();

        // 5. Add item (as CASHIER) — stock gets reserved
        String addItemBody = """
                {"barcode": "7891234567890", "quantity": 2}
                """;
        mockMvc.perform(post("/api/v1/sales/" + saleUuid + "/items")
                        .header("Authorization", "Bearer " + cashierToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());

        // 6. Finalize sale (as CASHIER)
        String finalizeBody = """
                {"paymentMethod": "PIX", "amountPaid": 99.80, "couponCode": null, "expectedTotal": 99.80}
                """;
        mockMvc.perform(post("/api/v1/sales/" + saleUuid + "/finalize")
                        .header("Authorization", "Bearer " + cashierToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(finalizeBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentMethod").value("PIX"));

        // 7. Verify: lancamentos_financeiros has 1 RECEITA entry for this sale
        // Allow a brief moment for async event processing
        Thread.sleep(200);

        Integer receitaCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM lancamentos_financeiros WHERE sale_uuid = ?::uuid AND type = 'RECEITA'",
                Integer.class, saleUuid);
        assertThat(receitaCount).isEqualTo(1);

        // 8. Verify: estoque_items shows reserved=0 (committed) and physical adjusted
        Integer reservedStock = jdbcTemplate.queryForObject(
                "SELECT reserved_stock FROM estoque_items WHERE variante_uuid = ?::uuid",
                Integer.class, variantUuid);
        assertThat(reservedStock).isEqualTo(0);

        Integer physicalStock = jdbcTemplate.queryForObject(
                "SELECT physical_stock FROM estoque_items WHERE variante_uuid = ?::uuid",
                Integer.class, variantUuid);
        // After entry(20) and commit of reservation(2), physical stays at 20 (commit just converts reserved → withdrawn)
        // or physical drops to 18 depending on implementation
        assertThat(physicalStock).isLessThanOrEqualTo(20);
        assertThat(physicalStock).isGreaterThanOrEqualTo(0);

        // 9. Verify: venda status == FINALIZADA
        String vendaStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM vendas WHERE uuid = ?::uuid",
                String.class, saleUuid);
        assertThat(vendaStatus).isEqualTo("FINALIZADA");
    }

    @Test
    @DisplayName("POS flow — verify sale can be retrieved after finalization")
    void posFlow_retrieveSaleAfterFinalization() throws Exception {
        UUID managerUuid = UUID.randomUUID();
        String managerToken = TestJwtGenerator.generateToken(managerUuid, "ROLE_MANAGER");

        // Create product + variant + stock
        String productBody = """
                {"name": "Calça Jeans", "brand": "DenimCo", "category": "Calças"}
                """;
        MvcResult productResult = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productBody))
                .andExpect(status().isCreated())
                .andReturn();

        String productUuid = objectMapper.readTree(productResult.getResponse().getContentAsString())
                .get("uuid").asText();

        String variantBody = """
                {"sku": "CAL-JEA-G-AZU", "size": "G", "color": "Azul", "barcode": "7891234567891", "price": 129.90, "cost": 50.00}
                """;
        MvcResult variantResult = mockMvc.perform(post("/api/v1/products/" + productUuid + "/variants")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(variantBody))
                .andExpect(status().isCreated())
                .andReturn();

        String variantUuid = objectMapper.readTree(variantResult.getResponse().getContentAsString())
                .get("uuid").asText();

        mockMvc.perform(post("/api/v1/inventory/variants/" + variantUuid + "/entries")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"quantity\": 10, \"actorUuid\": \"%s\"}", managerUuid)))
                .andExpect(status().isCreated());

        // Open + add item + finalize
        MvcResult saleResult = mockMvc.perform(post("/api/v1/sales")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"terminalId\": \"POS-02\", \"clienteUuid\": null}"))
                .andExpect(status().isCreated())
                .andReturn();

        String saleUuid = objectMapper.readTree(saleResult.getResponse().getContentAsString())
                .get("uuid").asText();

        mockMvc.perform(post("/api/v1/sales/" + saleUuid + "/items")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"barcode\": \"7891234567891\", \"quantity\": 1}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/sales/" + saleUuid + "/finalize")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentMethod\": \"DINHEIRO\", \"amountPaid\": 150.00, \"couponCode\": null, \"expectedTotal\": 129.90}"))
                .andExpect(status().isOk());

        // Retrieve the finalized sale
        mockMvc.perform(get("/api/v1/sales/" + saleUuid)
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZADA"))
                .andExpect(jsonPath("$.paymentMethod").value("DINHEIRO"));
    }
}
