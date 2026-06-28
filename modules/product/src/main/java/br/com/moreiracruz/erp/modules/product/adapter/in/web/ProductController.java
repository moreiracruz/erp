package br.com.moreiracruz.erp.modules.product.adapter.in.web;

import br.com.moreiracruz.erp.modules.product.domain.port.in.DeactivateProductUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.in.CatalogProductResponse;
import br.com.moreiracruz.erp.modules.product.domain.port.in.GetProductUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.in.ProdutoResponse;
import br.com.moreiracruz.erp.modules.product.domain.port.in.RegisterProductCommand;
import br.com.moreiracruz.erp.modules.product.domain.port.in.RegisterProductUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.in.RegisterVariantCommand;
import br.com.moreiracruz.erp.modules.product.domain.port.in.RegisterVariantUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.in.SearchVariantUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.in.UpdateProductCommand;
import br.com.moreiracruz.erp.modules.product.domain.port.in.UpdateProductUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.in.VarianteResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST adapter exposing product catalog operations under {@code /api/v1/products}.
 *
 * <p>Endpoints are secured with method-level {@code @PreAuthorize} annotations.
 * Managers may perform all operations; cashiers and stock personnel can search variants.
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final RegisterProductUseCase registerProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeactivateProductUseCase deactivateProductUseCase;
    private final RegisterVariantUseCase registerVariantUseCase;
    private final SearchVariantUseCase searchVariantUseCase;

    public ProductController(RegisterProductUseCase registerProductUseCase,
                             GetProductUseCase getProductUseCase,
                             UpdateProductUseCase updateProductUseCase,
                             DeactivateProductUseCase deactivateProductUseCase,
                             RegisterVariantUseCase registerVariantUseCase,
                             SearchVariantUseCase searchVariantUseCase) {
        this.registerProductUseCase = registerProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deactivateProductUseCase = deactivateProductUseCase;
        this.registerVariantUseCase = registerVariantUseCase;
        this.searchVariantUseCase = searchVariantUseCase;
    }

    /**
     * Creates a new product in the catalog.
     *
     * @param cmd name, brand and category of the product
     * @return the created product
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ProdutoResponse create(@RequestBody RegisterProductCommand cmd) {
        return registerProductUseCase.register(cmd);
    }

    /**
     * Lists all active products (public endpoint for storefront).
     */
    @GetMapping
    public java.util.List<ProdutoResponse> findAll() {
        return getProductUseCase.findAll();
    }

    /**
     * Lists active products with active variants and price range for the storefront.
     */
    @GetMapping("/catalog")
    public java.util.List<CatalogProductResponse> findCatalog() {
        return getProductUseCase.findCatalog();
    }

    /**
     * Retrieves a storefront product detail with active variants.
     *
     * @param uuid the product's UUID
     * @return product detail for the public catalog
     */
    @GetMapping("/catalog/{uuid}")
    public CatalogProductResponse findCatalogByUuid(@PathVariable UUID uuid) {
        return getProductUseCase.findCatalogByUuid(uuid);
    }

    /**
     * Retrieves a product by its public UUID (public endpoint).
     *
     * @param uuid the product's UUID
     * @return the product, or 404 if not found
     */
    @GetMapping("/{uuid}")
    public ProdutoResponse findByUuid(@PathVariable UUID uuid) {
        return getProductUseCase.findByUuid(uuid);
    }

    /**
     * Updates the mutable attributes of an existing product.
     *
     * @param uuid the product's UUID
     * @param cmd  new name, brand and category
     * @return the updated product
     */
    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ProdutoResponse update(@PathVariable UUID uuid, @RequestBody UpdateProductCommand cmd) {
        return updateProductUseCase.update(uuid, cmd);
    }

    /**
     * Deactivates a product and all its variants.
     *
     * @param uuid the product's UUID
     */
    @DeleteMapping("/{uuid}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public void deactivate(@PathVariable UUID uuid) {
        deactivateProductUseCase.deactivate(uuid);
    }

    /**
     * Registers a new variant under an existing product.
     *
     * @param uuid the owning product's UUID
     * @param cmd  variant attributes (SKU, size, colour, barcode, price, cost)
     * @return the created variant
     */
    @PostMapping("/{uuid}/variants")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public VarianteResponse createVariant(@PathVariable UUID uuid,
                                          @RequestBody RegisterVariantCommand cmd) {
        return registerVariantUseCase.register(uuid, cmd);
    }

    /**
     * Looks up a variant by its SKU.
     *
     * @param sku the variant's SKU
     * @return the matching variant, or 404 if not found
     */
    @GetMapping("/variants/by-sku/{sku}")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_CASHIER') or hasRole('ROLE_STOCK')")
    public VarianteResponse findBySku(@PathVariable String sku) {
        return searchVariantUseCase.findBySku(sku);
    }

    /**
     * Looks up a variant by its barcode.
     *
     * @param barcode the variant's barcode
     * @return the matching variant, or 404 if not found
     */
    @GetMapping("/variants/by-barcode/{barcode}")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_CASHIER') or hasRole('ROLE_STOCK')")
    public VarianteResponse findByBarcode(@PathVariable String barcode) {
        return searchVariantUseCase.findByBarcode(barcode);
    }
}
