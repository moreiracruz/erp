package br.com.moreiracruz.erp.modules.product.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a specific variant of a {@link Produto} (e.g. size + colour combination).
 *
 * <p>Owned by a {@link Produto} aggregate; persistence is via its own JPA table.
 * Use the {@link #create(Long, String, String, String, String, BigDecimal, BigDecimal)}
 * factory method to obtain a valid instance.
 */
public class VarianteProduto {

    private Long id;

    private UUID uuid;

    private Long produtoId;

    private UUID produtoUuid;

    private Sku sku;

    private String size;

    private String color;

    private Barcode barcode;

    private Money price;

    private Money cost;

    private boolean active;

    private Instant createdAt;

    private Instant updatedAt;

    protected VarianteProduto() {}

    private VarianteProduto(Long produtoId, UUID produtoUuid, Sku sku, String size, String color,
                            Barcode barcode, Money price, Money cost) {
        this.uuid = UUID.randomUUID();
        this.produtoId = produtoId;
        this.produtoUuid = produtoUuid;
        this.sku = sku;
        this.size = size;
        this.color = color;
        this.barcode = barcode;
        this.price = price;
        this.cost = cost;
        this.active = true;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Restores a {@code VarianteProduto} from its persisted state, bypassing validation.
     *
     * <p>Intended for use by persistence adapters only — all values are trusted to
     * be already validated at creation time.
     *
     * @param id         surrogate database id
     * @param uuid       public UUID
     * @param produtoId  owning product's surrogate id
     * @param produtoUuid owning product's public UUID
     * @param sku        raw SKU string
     * @param size       size label
     * @param color      colour label
     * @param barcode    raw barcode string
     * @param price      selling price
     * @param cost       cost price
     * @param active     whether the variant is active
     * @param createdAt  creation timestamp
     * @param updatedAt  last-update timestamp
     * @return a fully initialised {@code VarianteProduto} without extra validation
     */
    public static VarianteProduto restore(Long id, UUID uuid, Long produtoId, UUID produtoUuid,
                                          String sku, String size, String color, String barcode,
                                          BigDecimal price, BigDecimal cost, boolean active,
                                          Instant createdAt, Instant updatedAt) {
        VarianteProduto v = new VarianteProduto(
                produtoId, produtoUuid,
                new Sku(sku), size, color,
                new Barcode(barcode), new Money(price), new Money(cost));
        v.id = id;
        v.uuid = uuid;
        v.active = active;
        v.createdAt = createdAt;
        v.updatedAt = updatedAt;
        return v;
    }

    /**
     * Creates and validates a new {@code VarianteProduto}.
     *
     * <p>Value objects ({@link Sku}, {@link Barcode}, {@link Money}) perform their own
     * validation and throw {@link ValidationException} if the raw values are invalid.
     *
     * @param produtoId   the owning product's surrogate id
     * @param produtoUuid the owning product's public UUID
     * @param sku         raw SKU string (1–50 chars)
     * @param size        size label (e.g. "M", "42")
     * @param color       colour label
     * @param barcode     raw barcode string (8–14 digits)
     * @param price       selling price (0.01–999999.99)
     * @param cost        cost price (0.01–999999.99)
     * @throws ValidationException if any value object fails validation
     */
    public static VarianteProduto create(Long produtoId, UUID produtoUuid, String sku, String size,
                                         String color, String barcode,
                                         BigDecimal price, BigDecimal cost) {
        Sku skuVo       = new Sku(sku);
        Barcode bcVo    = new Barcode(barcode);
        Money priceVo   = new Money(price);
        Money costVo    = new Money(cost);
        return new VarianteProduto(produtoId, produtoUuid, skuVo, size, color, bcVo, priceVo, costVo);
    }

    /**
     * Creates and validates a new {@code VarianteProduto} (legacy overload without produtoUuid).
     *
     * @deprecated Prefer {@link #create(Long, UUID, String, String, String, String, BigDecimal, BigDecimal)}.
     */
    @Deprecated
    public static VarianteProduto create(Long produtoId, String sku, String size, String color,
                                         String barcode, BigDecimal price, BigDecimal cost) {
        return create(produtoId, null, sku, size, color, barcode, price, cost);
    }

    /**
     * Deactivates this variant, setting {@code active = false} and updating the
     * {@code updatedAt} timestamp.
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public UUID getProdutoUuid() {
        return produtoUuid;
    }

    public Sku getSku() {
        return sku;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public Barcode getBarcode() {
        return barcode;
    }

    public Money getPrice() {
        return price;
    }

    public Money getCost() {
        return cost;
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
