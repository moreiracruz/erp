package br.com.moreiracruz.erp.e2e;

import br.com.moreiracruz.erp.test.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@AutoConfigureMockMvc
class PublicStorefrontAccessIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Anonymous user can browse storefront product catalog")
    void anonymousUserCanBrowseCatalog() throws Exception {
        assertPublic(get("/api/v1/products/catalog"));
        assertPublic(get("/api/v1/products"));
        assertPublic(get("/api/v1/products/catalog/" + UUID.randomUUID()));
        assertPublic(get("/api/v1/products/" + UUID.randomUUID()));
        assertPublic(get("/api/v1/products/" + UUID.randomUUID() + "/images"));
    }

    private void assertPublic(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request)
            throws Exception {
        int status = mockMvc.perform(request).andReturn().getResponse().getStatus();
        assertThat(status).isNotIn(401, 403);
    }
}
