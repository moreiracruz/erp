package br.com.moreiracruz.erp.test.builders;

import br.com.moreiracruz.erp.modules.product.domain.model.VarianteProduto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Fluent builder for creating {@link VarianteProduto} domain objects in tests.
 */
public class VarianteTestBuilder {

    private Long produtoId = 1L;
    private UUID produtoUuid = UUID.randomUUID();
    private String sku = "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    private String size = "M";
    private String color = "Preto";
    private String barcode = "78901234" + String.format("%05d", (int)(Math.random() * 99999));
    private BigDecimal price = new BigDecimal("99.90");
    private BigDecimal cost = new BigDecimal("45.00");

    private VarianteTestBuilder() {}

    public static VarianteTestBuilder aVariante() {
        return new VarianteTestBuilder();
    }

    public VarianteTestBuilder withProdutoId(Long produtoId) { this.produtoId = produtoId; return this; }
    public VarianteTestBuilder withProdutoUuid(UUID produtoUuid) { this.produtoUuid = produtoUuid; return this; }
    public VarianteTestBuilder withSku(String sku) { this.sku = sku; return this; }
    public VarianteTestBuilder withSize(String size) { this.size = size; return this; }
    public VarianteTestBuilder withColor(String color) { this.color = color; return this; }
    public VarianteTestBuilder withBarcode(String barcode) { this.barcode = barcode; return this; }
    public VarianteTestBuilder withPrice(BigDecimal price) { this.price = price; return this; }
    public VarianteTestBuilder withCost(BigDecimal cost) { this.cost = cost; return this; }

    /** Build domain object using the create factory method. */
    public VarianteProduto build() {
        return VarianteProduto.create(produtoId, produtoUuid, sku, size, color, barcode, price, cost);
    }
}
