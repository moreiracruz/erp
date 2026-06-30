package br.com.moreiracruz.erp.modules.product.application.usecase;

import br.com.moreiracruz.erp.modules.product.domain.model.Produto;
import br.com.moreiracruz.erp.modules.product.domain.model.VarianteProduto;
import br.com.moreiracruz.erp.modules.product.domain.port.in.CatalogProductResponse;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ProdutoRepository;
import br.com.moreiracruz.erp.modules.product.domain.port.out.VarianteRepository;
import br.com.moreiracruz.erp.shared.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetProductUseCaseImplTest {

    private final InMemoryProdutoRepository produtoRepository = new InMemoryProdutoRepository();
    private final InMemoryVarianteRepository varianteRepository = new InMemoryVarianteRepository();
    private final GetProductUseCaseImpl useCase = new GetProductUseCaseImpl(produtoRepository, varianteRepository);

    @Test
    void findCatalogReturnsActiveVariantsAndPriceRange() {
        Produto produto = produto(1L, UUID.randomUUID(), "Vestido Flora");
        produtoRepository.products.add(produto);

        VarianteProduto lowPrice = variante(10L, produto, "SKU-LOW", "7891000000001", "99.90");
        VarianteProduto highPrice = variante(11L, produto, "SKU-HIGH", "7891000000002", "129.90");
        VarianteProduto inactive = variante(12L, produto, "SKU-OFF", "7891000000003", "59.90");
        inactive.deactivate();
        varianteRepository.variants.addAll(List.of(lowPrice, highPrice, inactive));

        List<CatalogProductResponse> catalog = useCase.findCatalog();

        assertThat(catalog).hasSize(1);
        CatalogProductResponse response = catalog.getFirst();
        assertThat(response.uuid()).isEqualTo(produto.getUuid());
        assertThat(response.minPrice()).isEqualByComparingTo("99.90");
        assertThat(response.maxPrice()).isEqualByComparingTo("129.90");
        assertThat(response.variants())
                .extracting("sku")
                .containsExactly("SKU-LOW", "SKU-HIGH");
    }

    @Test
    void findCatalogByUuidRejectsInactiveProduct() {
        Produto produto = produto(1L, UUID.randomUUID(), "Blusa Inativa");
        produto.deactivate();
        produtoRepository.products.add(produto);

        assertThatThrownBy(() -> useCase.findCatalogByUuid(produto.getUuid()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Produto não encontrado");
    }

    private static Produto produto(Long id, UUID uuid, String name) {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        return Produto.restore(id, uuid, name, "Reino & Flor", "Vestidos", true, now, now);
    }

    private static VarianteProduto variante(Long id, Produto produto, String sku, String barcode, String price) {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        return VarianteProduto.restore(
                id,
                UUID.randomUUID(),
                produto.getId(),
                produto.getUuid(),
                sku,
                "M",
                "Rosa",
                barcode,
                new BigDecimal(price),
                new BigDecimal("40.00"),
                true,
                now,
                now
        );
    }

    private static class InMemoryProdutoRepository implements ProdutoRepository {
        private final List<Produto> products = new ArrayList<>();

        @Override
        public List<Produto> findAllActive() {
            return products.stream().filter(Produto::isActive).toList();
        }

        @Override
        public Optional<Produto> findByUuid(UUID uuid) {
            return products.stream().filter(product -> product.getUuid().equals(uuid)).findFirst();
        }

        @Override
        public boolean existsByNameIgnoreCaseAndActiveTrue(String name) {
            return products.stream()
                    .anyMatch(product -> product.isActive() && product.getName().equalsIgnoreCase(name));
        }

        @Override
        public Produto save(Produto produto) {
            products.add(produto);
            return produto;
        }
    }

    private static class InMemoryVarianteRepository implements VarianteRepository {
        private final List<VarianteProduto> variants = new ArrayList<>();

        @Override
        public Optional<VarianteProduto> findByUuid(UUID uuid) {
            return variants.stream().filter(variant -> variant.getUuid().equals(uuid)).findFirst();
        }

        @Override
        public Optional<VarianteProduto> findBySku(String sku) {
            return variants.stream().filter(variant -> variant.getSku().value().equals(sku)).findFirst();
        }

        @Override
        public Optional<VarianteProduto> findByBarcode(String barcode) {
            return variants.stream().filter(variant -> variant.getBarcode().value().equals(barcode)).findFirst();
        }

        @Override
        public boolean existsBySku(String sku) {
            return findBySku(sku).isPresent();
        }

        @Override
        public boolean existsByBarcode(String barcode) {
            return findByBarcode(barcode).isPresent();
        }

        @Override
        public List<VarianteProduto> findByProdutoId(Long produtoId) {
            return variants.stream().filter(variant -> variant.getProdutoId().equals(produtoId)).toList();
        }

        @Override
        public VarianteProduto save(VarianteProduto variante) {
            variants.add(variante);
            return variante;
        }
    }
}
