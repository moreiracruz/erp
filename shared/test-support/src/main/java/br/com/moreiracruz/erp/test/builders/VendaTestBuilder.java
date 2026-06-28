package br.com.moreiracruz.erp.test.builders;

import br.com.moreiracruz.erp.modules.sales.domain.model.ItemVenda;
import br.com.moreiracruz.erp.modules.sales.domain.model.PaymentMethod;
import br.com.moreiracruz.erp.modules.sales.domain.model.Venda;
import br.com.moreiracruz.erp.modules.sales.domain.model.VendaStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fluent builder for creating {@link Venda} domain objects in tests.
 */
public class VendaTestBuilder {

    private UUID operatorUuid = UUID.randomUUID();
    private String terminalId = "POS-01";
    private UUID clienteUuid = null;
    private VendaStatus status = VendaStatus.EM_ANDAMENTO;
    private PaymentMethod paymentMethod = null;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal changeAmount = BigDecimal.ZERO;
    private String couponCode = null;
    private String cancellationReason = null;
    private Instant createdAt = Instant.now();
    private Instant finalizedAt = null;
    private List<ItemVenda> items = new ArrayList<>();

    private VendaTestBuilder() {}

    public static VendaTestBuilder anOpenSale() {
        return new VendaTestBuilder();
    }

    public static VendaTestBuilder aFinalizedSale() {
        return new VendaTestBuilder()
                .withStatus(VendaStatus.FINALIZADA)
                .withPaymentMethod(PaymentMethod.PIX)
                .withSubtotal(new BigDecimal("199.90"))
                .withTotal(new BigDecimal("199.90"))
                .withFinalizedAt(Instant.now());
    }

    public VendaTestBuilder withOperatorUuid(UUID operatorUuid) { this.operatorUuid = operatorUuid; return this; }
    public VendaTestBuilder withTerminalId(String terminalId) { this.terminalId = terminalId; return this; }
    public VendaTestBuilder withClienteUuid(UUID clienteUuid) { this.clienteUuid = clienteUuid; return this; }
    public VendaTestBuilder withStatus(VendaStatus status) { this.status = status; return this; }
    public VendaTestBuilder withPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; return this; }
    public VendaTestBuilder withSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; return this; }
    public VendaTestBuilder withDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; return this; }
    public VendaTestBuilder withTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; return this; }
    public VendaTestBuilder withTotal(BigDecimal total) { this.total = total; return this; }
    public VendaTestBuilder withChangeAmount(BigDecimal changeAmount) { this.changeAmount = changeAmount; return this; }
    public VendaTestBuilder withCouponCode(String couponCode) { this.couponCode = couponCode; return this; }
    public VendaTestBuilder withCancellationReason(String reason) { this.cancellationReason = reason; return this; }
    public VendaTestBuilder withFinalizedAt(Instant finalizedAt) { this.finalizedAt = finalizedAt; return this; }
    public VendaTestBuilder withItems(List<ItemVenda> items) { this.items = items; return this; }

    /** Build domain object using restore factory method. */
    public Venda build() {
        return Venda.restore(null, UUID.randomUUID(), operatorUuid, terminalId,
                clienteUuid, status, paymentMethod, subtotal, discountAmount,
                taxAmount, total, changeAmount, couponCode, cancellationReason,
                createdAt, finalizedAt, items);
    }
}
