package com.erp.modules.sales.application.usecase;

import com.erp.modules.sales.domain.model.PaymentMethod;
import com.erp.modules.sales.domain.model.Venda;
import com.erp.modules.sales.domain.port.in.FinalizationResponse;
import com.erp.modules.sales.domain.port.in.FinalizeSaleCommand;
import com.erp.modules.sales.domain.port.in.FinalizeSaleUseCase;
import com.erp.modules.sales.domain.port.out.VendaRepository;
import com.erp.shared.events.EventEnvelope;
import com.erp.shared.events.SaleCompletedPayload;
import com.erp.shared.exceptions.NotFoundException;
import com.erp.shared.exceptions.ValidationException;
import com.erp.shared.kernel.InventoryPort;
import com.erp.shared.kernel.PricingPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FinalizeSaleUseCaseImpl implements FinalizeSaleUseCase {

    private final VendaRepository vendaRepository;
    private final PricingPort pricingPort;
    private final InventoryPort inventoryPort;
    private final ApplicationEventPublisher eventPublisher;

    public FinalizeSaleUseCaseImpl(VendaRepository vendaRepository,
                                   PricingPort pricingPort,
                                   InventoryPort inventoryPort,
                                   ApplicationEventPublisher eventPublisher) {
        this.vendaRepository = vendaRepository;
        this.pricingPort = pricingPort;
        this.inventoryPort = inventoryPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public FinalizationResponse finalize(UUID vendaUuid, FinalizeSaleCommand cmd) {
        Venda venda = vendaRepository.findByUuid(vendaUuid)
                .orElseThrow(() -> new NotFoundException("Venda não encontrada: " + vendaUuid));

        PaymentMethod paymentMethod = PaymentMethod.valueOf(cmd.paymentMethod());

        // Calculate discount via pricing port
        List<PricingPort.ItemLine> itemLines = venda.getItems().stream()
                .map(item -> new PricingPort.ItemLine(item.getVarianteUuid(), item.getQuantity(), item.getUnitPrice()))
                .toList();

        BigDecimal discountAmount = pricingPort.calculateDiscount(
                venda.getUuid(), itemLines, venda.getSubtotal(), cmd.couponCode());

        // Compute total (tax is zero for MVP)
        venda.computeTotal(discountAmount, BigDecimal.ZERO);

        // Validate expected total
        if (cmd.expectedTotal() != null && venda.getTotal().compareTo(cmd.expectedTotal()) != 0) {
            throw new ValidationException("Valor de total inválido");
        }

        // Handle change for cash payments
        BigDecimal changeAmount = BigDecimal.ZERO;
        if (paymentMethod == PaymentMethod.DINHEIRO) {
            changeAmount = venda.computeChange(cmd.amountPaid());
        }

        // Confirm coupon usage if applicable
        if (cmd.couponCode() != null && !cmd.couponCode().isBlank()) {
            pricingPort.confirmCouponUsage(cmd.couponCode());
            venda.setCouponCode(cmd.couponCode());
        }

        // Finalize the sale
        venda.finalize(paymentMethod, changeAmount);

        // Commit inventory reservations
        inventoryPort.commitAll(venda.getUuid());

        // Save
        venda = vendaRepository.save(venda);

        // Publish domain event
        List<SaleCompletedPayload.SaleItem> saleItems = venda.getItems().stream()
                .map(item -> new SaleCompletedPayload.SaleItem(item.getSku(), item.getQuantity()))
                .toList();

        SaleCompletedPayload payload = new SaleCompletedPayload(
                venda.getUuid(), venda.getOperatorUuid(), saleItems,
                venda.getTotal(), paymentMethod.name());

        EventEnvelope<SaleCompletedPayload> event = new EventEnvelope<>(
                UUID.randomUUID(), "SaleCompleted", Instant.now(), payload);
        eventPublisher.publishEvent(event);

        return new FinalizationResponse(
                venda.getUuid(),
                venda.getSubtotal(),
                venda.getDiscountAmount(),
                venda.getTaxAmount(),
                venda.getTotal(),
                venda.getChangeAmount(),
                paymentMethod.name()
        );
    }
}
