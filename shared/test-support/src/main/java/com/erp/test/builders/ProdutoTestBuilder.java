package com.erp.test.builders;

import com.erp.modules.product.domain.model.Produto;

/**
 * Fluent builder for creating {@link Produto} domain objects in tests.
 */
public class ProdutoTestBuilder {

    private String name = "Camiseta Básica";
    private String brand = "Nike";
    private String category = "Camisetas";

    private ProdutoTestBuilder() {}

    public static ProdutoTestBuilder aProduct() {
        return new ProdutoTestBuilder();
    }

    public ProdutoTestBuilder withName(String name) { this.name = name; return this; }
    public ProdutoTestBuilder withBrand(String brand) { this.brand = brand; return this; }
    public ProdutoTestBuilder withCategory(String category) { this.category = category; return this; }

    /** Build domain object using the create factory method. */
    public Produto build() {
        return Produto.create(name, brand, category);
    }
}
