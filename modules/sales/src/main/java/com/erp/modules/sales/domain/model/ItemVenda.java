package com.erp.modules.sales.domain.model;

import com.erp.shared.utils.MoneyUtils;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a single line item within a sale.
 */
public class ItemVenda {

    private Long id;
    private Long vendaId;
    private UUID varianteUuid;
    private String sku;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    protected ItemVenda() {
        // JPA / framework constructor
    }

    private ItemVenda(Long vendaId, UUID varianteUuid, String sku, int quantity,
                      BigDecimal unitPrice, BigDecimal lineTotal) {
        this.vendaId = vendaId;
        this.varianteUuid = varianteUuid;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    /**
     * Factory method that creates an ItemVenda and computes the line total.
     *
     * @param vendaId       the owning sale's surrogate id
     * @param varianteUuid  the product variant UUID
     * @param sku           the SKU code
     * @param quantity      number of units
     * @param unitPrice     price per unit
     * @return a new ItemVenda with computed lineTotal
     */
    public static ItemVenda create(Long vendaId, UUID varianteUuid, String sku,
                                   int quantity, BigDecimal unitPrice) {
        BigDecimal lineTotal = MoneyUtils.round(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        return new ItemVenda(vendaId, varianteUuid, sku, quantity, unitPrice, lineTotal);
    }

    /**
     * Restores an ItemVenda from persistent storage.
     */
    public static ItemVenda restore(Long id, Long vendaId, UUID varianteUuid, String sku,
                                    int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
        ItemVenda item = new ItemVenda();
        item.id = id;
        item.vendaId = vendaId;
        item.varianteUuid = varianteUuid;
        item.sku = sku;
        item.quantity = quantity;
        item.unitPrice = unitPrice;
        item.lineTotal = lineTotal;
        return item;
    }

    // --- Getters ---

    public Long getId() {
        return id;
    }

    public Long getVendaId() {
        return vendaId;
    }

    public UUID getVarianteUuid() {
        return varianteUuid;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
