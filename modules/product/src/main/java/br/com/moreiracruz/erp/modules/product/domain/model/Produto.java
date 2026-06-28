package br.com.moreiracruz.erp.modules.product.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import br.com.moreiracruz.erp.shared.kernel.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root representing a product in the catalog.
 *
 * <p>A product groups one or more {@link VarianteProduto variants} (e.g. different
 * sizes/colors). Use the {@link #create(String, String, String)} factory method to
 * obtain a valid instance.
 */
public class Produto extends AggregateRoot {

    private String name;

    private String brand;

    private String category;

    private boolean active;

    private Instant createdAt;

    private Instant updatedAt;

    protected Produto() {}

    private Produto(String name, String brand, String category) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.active = true;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Restores a {@code Produto} from its persisted state, bypassing validation.
     *
     * <p>Intended for use by persistence adapters only — all values are trusted to
     * be already validated at creation time.
     *
     * @param id        surrogate database id
     * @param uuid      public UUID
     * @param name      product name
     * @param brand     brand name
     * @param category  product category
     * @param active    whether the product is active
     * @param createdAt creation timestamp
     * @param updatedAt last-update timestamp
     * @return a fully initialised {@code Produto} without validation
     */
    public static Produto restore(Long id, UUID uuid, String name, String brand, String category,
                                  boolean active, Instant createdAt, Instant updatedAt) {
        Produto p = new Produto(name, brand, category);
        p.id = id;
        p.uuid = uuid;
        p.active = active;
        p.createdAt = createdAt;
        p.updatedAt = updatedAt;
        return p;
    }

    /**
     * Creates and validates a new {@code Produto}.
     *
     * @param name     product name, 1–255 characters
     * @param brand    brand name, 1–100 characters
     * @param category product category, 1–100 characters
     * @throws ValidationException if any field fails its constraint
     */
    public static Produto create(String name, String brand, String category) {
        if (name == null || name.isBlank() || name.length() > 255) {
            throw new ValidationException("name", "deve ter entre 1 e 255 caracteres");
        }
        if (brand == null || brand.isBlank() || brand.length() > 100) {
            throw new ValidationException("brand", "deve ter entre 1 e 100 caracteres");
        }
        if (category == null || category.isBlank() || category.length() > 100) {
            throw new ValidationException("category", "deve ter entre 1 e 100 caracteres");
        }
        return new Produto(name, brand, category);
    }

    /**
     * Deactivates this product, setting {@code active = false} and updating the
     * {@code updatedAt} timestamp.
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the mutable attributes of this product.
     *
     * @param name     new product name (1–255 characters)
     * @param brand    new brand name (1–100 characters)
     * @param category new category (1–100 characters)
     * @throws br.com.moreiracruz.erp.shared.exceptions.ValidationException if any field fails its constraint
     */
    public void update(String name, String brand, String category) {
        if (name == null || name.isBlank() || name.length() > 255) {
            throw new ValidationException("name", "deve ter entre 1 e 255 caracteres");
        }
        if (brand == null || brand.isBlank() || brand.length() > 100) {
            throw new ValidationException("brand", "deve ter entre 1 e 100 caracteres");
        }
        if (category == null || category.isBlank() || category.length() > 100) {
            throw new ValidationException("category", "deve ter entre 1 e 100 caracteres");
        }
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.updatedAt = Instant.now();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getCategory() {
        return category;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
