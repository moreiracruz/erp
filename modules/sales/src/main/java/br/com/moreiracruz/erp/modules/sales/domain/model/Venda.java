package br.com.moreiracruz.erp.modules.sales.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import br.com.moreiracruz.erp.shared.kernel.AggregateRoot;
import br.com.moreiracruz.erp.shared.utils.MoneyUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root representing a point-of-sale transaction.
 */
public class Venda extends AggregateRoot {

    private UUID operatorUuid;
    private String terminalId;
    private UUID clienteUuid;
    private VendaStatus status;
    private PaymentMethod paymentMethod;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private BigDecimal changeAmount;
    private String couponCode;
    private String cancellationReason;
    private Instant createdAt;
    private Instant finalizedAt;
    private List<ItemVenda> items;

    protected Venda() {
        // JPA / framework constructor
    }

    private Venda(UUID operatorUuid, String terminalId, UUID clienteUuid) {
        this.operatorUuid = operatorUuid;
        this.terminalId = terminalId;
        this.clienteUuid = clienteUuid;
        this.status = VendaStatus.EM_ANDAMENTO;
        this.subtotal = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.items = new ArrayList<>();
    }

    /**
     * Creates a new sale in EM_ANDAMENTO status.
     *
     * @param operatorUuid the operator (cashier) performing the sale
     * @param terminalId   the POS terminal identifier
     * @param clienteUuid  the customer UUID (nullable)
     * @return a new Venda instance
     */
    public static Venda create(UUID operatorUuid, String terminalId, UUID clienteUuid) {
        return new Venda(operatorUuid, terminalId, clienteUuid);
    }

    /**
     * Restores a Venda from persistent storage.
     */
    public static Venda restore(Long id, UUID uuid, UUID operatorUuid, String terminalId,
                                UUID clienteUuid, VendaStatus status, PaymentMethod paymentMethod,
                                BigDecimal subtotal, BigDecimal discountAmount, BigDecimal taxAmount,
                                BigDecimal total, BigDecimal changeAmount, String couponCode,
                                String cancellationReason, Instant createdAt, Instant finalizedAt,
                                List<ItemVenda> items) {
        Venda venda = new Venda();
        venda.id = id;
        venda.uuid = uuid;
        venda.operatorUuid = operatorUuid;
        venda.terminalId = terminalId;
        venda.clienteUuid = clienteUuid;
        venda.status = status;
        venda.paymentMethod = paymentMethod;
        venda.subtotal = subtotal;
        venda.discountAmount = discountAmount;
        venda.taxAmount = taxAmount;
        venda.total = total;
        venda.changeAmount = changeAmount;
        venda.couponCode = couponCode;
        venda.cancellationReason = cancellationReason;
        venda.createdAt = createdAt;
        venda.finalizedAt = finalizedAt;
        venda.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        return venda;
    }

    /**
     * Adds an item to this sale and recomputes the subtotal.
     */
    public void addItem(ItemVenda item) {
        items.add(item);
        recomputeSubtotal();
    }

    /**
     * Removes items matching the given varianteUuid and recomputes subtotal.
     */
    public void removeItem(UUID varianteUuid) {
        items.removeIf(item -> item.getVarianteUuid().equals(varianteUuid));
        recomputeSubtotal();
    }

    /**
     * Computes total = subtotal - discountAmount + taxAmount, stores the values,
     * and returns the result.
     */
    public BigDecimal computeTotal(BigDecimal discountAmount, BigDecimal taxAmount) {
        BigDecimal result = MoneyUtils.round(subtotal.subtract(discountAmount).add(taxAmount));
        this.discountAmount = discountAmount;
        this.taxAmount = taxAmount;
        this.total = result;
        return result;
    }

    /**
     * Computes change (troco) for cash payments.
     *
     * @param amountPaid the amount tendered by the customer
     * @return the change amount
     * @throws ValidationException if payment is insufficient
     */
    public BigDecimal computeChange(BigDecimal amountPaid) {
        if (amountPaid.compareTo(total) < 0) {
            throw new ValidationException("Pagamento insuficiente");
        }
        BigDecimal result = MoneyUtils.round(amountPaid.subtract(total));
        this.changeAmount = result;
        return result;
    }

    /**
     * Finalizes the sale, transitioning it to FINALIZADA status.
     */
    public void finalize(PaymentMethod paymentMethod, BigDecimal changeAmount) {
        this.status = VendaStatus.FINALIZADA;
        this.paymentMethod = paymentMethod;
        this.changeAmount = changeAmount;
        this.finalizedAt = Instant.now();
    }

    /**
     * Cancels the sale with a mandatory reason.
     *
     * @param reason cancellation reason (1–255 chars)
     * @throws ValidationException if reason is invalid
     */
    public void cancel(String reason) {
        if (reason == null || reason.isBlank() || reason.length() > 255) {
            throw new ValidationException("Motivo de cancelamento deve ter entre 1 e 255 caracteres");
        }
        this.status = VendaStatus.CANCELADA;
        this.cancellationReason = reason;
    }

    /**
     * Sets the coupon code applied to this sale.
     */
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    // --- Private helpers ---

    private void recomputeSubtotal() {
        this.subtotal = items.stream()
                .map(ItemVenda::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // --- Getters ---

    public UUID getOperatorUuid() {
        return operatorUuid;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public UUID getClienteUuid() {
        return clienteUuid;
    }

    public VendaStatus getStatus() {
        return status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getFinalizedAt() {
        return finalizedAt;
    }

    public List<ItemVenda> getItems() {
        return items;
    }
}
