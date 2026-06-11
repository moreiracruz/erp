package com.erp.modules.inventory.application.usecase;

import com.erp.modules.inventory.domain.model.MovimentoEstoque;
import com.erp.modules.inventory.domain.model.OperationType;
import com.erp.modules.inventory.domain.model.ReservaEstoque;
import com.erp.modules.inventory.domain.port.in.ReservaResponse;
import com.erp.modules.inventory.domain.port.in.ReserveStockUseCase;
import com.erp.modules.inventory.domain.port.in.StockReserveCommand;
import com.erp.modules.inventory.domain.port.out.EstoqueItemRepository;
import com.erp.modules.inventory.domain.port.out.MovimentoEstoqueRepository;
import com.erp.modules.inventory.domain.port.out.ReservaEstoqueRepository;
import com.erp.shared.events.EventEnvelope;
import com.erp.shared.events.StockReservedPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class ReserveStockUseCaseImpl implements ReserveStockUseCase {

    private final EstoqueItemRepository estoqueItemRepo;
    private final ReservaEstoqueRepository reservaRepo;
    private final MovimentoEstoqueRepository movimentoRepo;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${inventory.expiry.reservation-ttl-minutes:30}")
    private long ttlMinutes;

    public ReserveStockUseCaseImpl(EstoqueItemRepository estoqueItemRepo,
                                   ReservaEstoqueRepository reservaRepo,
                                   MovimentoEstoqueRepository movimentoRepo,
                                   ApplicationEventPublisher eventPublisher) {
        this.estoqueItemRepo = estoqueItemRepo;
        this.reservaRepo = reservaRepo;
        this.movimentoRepo = movimentoRepo;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ReservaResponse reserve(StockReserveCommand cmd) {
        var item = estoqueItemRepo.findByVarianteUuidForUpdate(cmd.varianteUuid());
        item.incrementReserved(cmd.quantity());
        estoqueItemRepo.save(item);

        var reserva = ReservaEstoque.create(cmd.varianteUuid(), cmd.saleUuid(), cmd.quantity(), ttlMinutes);
        reservaRepo.save(reserva);

        movimentoRepo.save(MovimentoEstoque.of(
                cmd.varianteUuid(), OperationType.RESERVA, cmd.quantity(), null, cmd.saleUuid()));

        var payload = new StockReservedPayload(cmd.varianteUuid(), cmd.quantity(), cmd.saleUuid());
        var envelope = new EventEnvelope<>(UUID.randomUUID(), "StockReserved", Instant.now(), payload);
        eventPublisher.publishEvent(envelope);

        return new ReservaResponse(
                reserva.getUuid(),
                reserva.getVarianteUuid(),
                reserva.getSaleUuid(),
                reserva.getQuantity(),
                reserva.getExpiresAt());
    }
}
