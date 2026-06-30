package br.com.moreiracruz.erp.consignment;

import br.com.moreiracruz.erp.test.AbstractIntegrationTest;
import br.com.moreiracruz.erp.test.TestJwtGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ConsignmentFlowIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void receivedConsignmentCanBeSoldAndSettledAsSupplierExpense() throws Exception {
        UUID managerUuid = UUID.randomUUID();
        String managerToken = TestJwtGenerator.generateToken(managerUuid, "ROLE_MANAGER");

        String variantUuid = createVariant(managerToken);

        MvcResult consignorResult = mockMvc.perform(post("/api/v1/consignments/consignors")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Maria Consignante","document":"123","email":null,"phone":null}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String consignorUuid = objectMapper.readTree(consignorResult.getResponse().getContentAsString()).get("uuid").asText();

        MvcResult contractResult = mockMvc.perform(post("/api/v1/consignments/contracts")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"consignorUuid":"%s","code":"CG-001"}
                                """.formatted(consignorUuid)))
                .andExpect(status().isCreated())
                .andReturn();
        String contractUuid = objectMapper.readTree(contractResult.getResponse().getContentAsString()).get("uuid").asText();

        MvcResult receiveResult = mockMvc.perform(post("/api/v1/consignments/contracts/" + contractUuid + "/items")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"varianteUuid":"%s","quantity":2}]}
                                """.formatted(variantUuid)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode receivedContract = objectMapper.readTree(receiveResult.getResponse().getContentAsString());
        String itemUuid = receivedContract.get("items").get(0).get("uuid").asText();

        Integer stockAfterReceive = jdbcTemplate.queryForObject(
                "SELECT physical_stock FROM estoque_items WHERE variante_uuid = ?::uuid",
                Integer.class, variantUuid);
        assertThat(stockAfterReceive).isEqualTo(2);

        String saleUuid = sellOneUnit(managerToken);

        Integer soldQuantity = jdbcTemplate.queryForObject(
                "SELECT sold_quantity FROM itens_consignados WHERE uuid = ?::uuid",
                Integer.class, itemUuid);
        assertThat(soldQuantity).isEqualTo(1);

        mockMvc.perform(post("/api/v1/consignments/contracts/" + contractUuid + "/settlements")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"notes":"Acerto semanal","items":[{"itemUuid":"%s","quantity":1,"manualAmount":35.00}]}
                                """.formatted(itemUuid)))
                .andExpect(status().isOk());

        Integer expenseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM lancamentos_financeiros WHERE type = 'DESPESA' AND category = 'FORNECEDORES' AND amount = 35.00",
                Integer.class);
        assertThat(expenseCount).isEqualTo(1);

        Integer settledQuantity = jdbcTemplate.queryForObject(
                "SELECT settled_quantity FROM itens_consignados WHERE uuid = ?::uuid",
                Integer.class, itemUuid);
        assertThat(settledQuantity).isEqualTo(1);
        assertThat(saleUuid).isNotBlank();
    }

    @Test
    void cashierCannotAccessConsignmentOperations() throws Exception {
        String cashierToken = TestJwtGenerator.generateToken(UUID.randomUUID(), "ROLE_CASHIER");

        mockMvc.perform(get("/api/v1/consignments/contracts")
                        .header("Authorization", "Bearer " + cashierToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void sentConsignmentCanBeSoldReturnedAndSettledAsRevenue() throws Exception {
        UUID managerUuid = UUID.randomUUID();
        String managerToken = TestJwtGenerator.generateToken(managerUuid, "ROLE_MANAGER");

        String variantUuid = createVariant(managerToken, "CONSIG-ENV-001", "7791234567891");
        mockMvc.perform(post("/api/v1/inventory/variants/" + variantUuid + "/entries")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":3,"actorUuid":"%s"}
                                """.formatted(managerUuid)))
                .andExpect(status().isCreated());

        MvcResult consigneeResult = mockMvc.perform(post("/api/v1/consignments/sent/consignees")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Boutique Parceira","document":"999","email":null,"phone":null}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String consigneeUuid = objectMapper.readTree(consigneeResult.getResponse().getContentAsString()).get("uuid").asText();

        MvcResult contractResult = mockMvc.perform(post("/api/v1/consignments/sent/contracts")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"consigneeUuid":"%s","code":"CGE-001"}
                                """.formatted(consigneeUuid)))
                .andExpect(status().isCreated())
                .andReturn();
        String contractUuid = objectMapper.readTree(contractResult.getResponse().getContentAsString()).get("uuid").asText();

        MvcResult sendResult = mockMvc.perform(post("/api/v1/consignments/sent/contracts/" + contractUuid + "/items")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"varianteUuid":"%s","quantity":3}]}
                                """.formatted(variantUuid)))
                .andExpect(status().isOk())
                .andReturn();
        String itemUuid = objectMapper.readTree(sendResult.getResponse().getContentAsString()).get("items").get(0).get("uuid").asText();

        Integer stockAfterSend = jdbcTemplate.queryForObject(
                "SELECT physical_stock FROM estoque_items WHERE variante_uuid = ?::uuid",
                Integer.class, variantUuid);
        assertThat(stockAfterSend).isZero();

        mockMvc.perform(post("/api/v1/consignments/sent/contracts/" + contractUuid + "/sales")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"itemUuid":"%s","quantity":2}]}
                                """.formatted(itemUuid)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/consignments/sent/contracts/" + contractUuid + "/returns")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"itemUuid":"%s","quantity":1}]}
                                """.formatted(itemUuid)))
                .andExpect(status().isOk());

        Integer stockAfterReturn = jdbcTemplate.queryForObject(
                "SELECT physical_stock FROM estoque_items WHERE variante_uuid = ?::uuid",
                Integer.class, variantUuid);
        assertThat(stockAfterReturn).isEqualTo(1);

        mockMvc.perform(post("/api/v1/consignments/sent/contracts/" + contractUuid + "/settlements")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"notes":"Repasse parceiro","items":[{"itemUuid":"%s","quantity":2,"manualAmount":180.00}]}
                                """.formatted(itemUuid)))
                .andExpect(status().isOk());

        Integer revenueCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM lancamentos_financeiros WHERE type = 'RECEITA' AND category = 'CONSIGNACAO' AND amount = 180.00",
                Integer.class);
        assertThat(revenueCount).isEqualTo(1);

        mockMvc.perform(post("/api/v1/consignments/sent/contracts/" + contractUuid + "/close")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());
    }

    private String createVariant(String managerToken) throws Exception {
        return createVariant(managerToken, "CONSIG-001", "7791234567890");
    }

    private String createVariant(String managerToken, String sku, String barcode) throws Exception {
        MvcResult productResult = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Vestido Consignado","brand":"Parceira","category":"Vestidos"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String productUuid = objectMapper.readTree(productResult.getResponse().getContentAsString()).get("uuid").asText();

        MvcResult variantResult = mockMvc.perform(post("/api/v1/products/" + productUuid + "/variants")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"%s","size":"M","color":"Azul","barcode":"%s","price":99.90,"cost":30.00}
                                """.formatted(sku, barcode)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(variantResult.getResponse().getContentAsString()).get("uuid").asText();
    }

    private String sellOneUnit(String managerToken) throws Exception {
        MvcResult saleResult = mockMvc.perform(post("/api/v1/sales")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"terminalId":"POS-CONSIG","clienteUuid":null}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String saleUuid = objectMapper.readTree(saleResult.getResponse().getContentAsString()).get("uuid").asText();

        mockMvc.perform(post("/api/v1/sales/" + saleUuid + "/items")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"barcode":"7791234567890","quantity":1}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/sales/" + saleUuid + "/finalize")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"paymentMethod":"PIX","amountPaid":99.90,"couponCode":null,"expectedTotal":99.90}
                                """))
                .andExpect(status().isOk());
        return saleUuid;
    }
}
