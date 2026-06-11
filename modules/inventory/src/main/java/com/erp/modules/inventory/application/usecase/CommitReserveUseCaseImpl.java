package com.erp.modules.inventory.application.usecase;

import com.erp.modules.inventory.domain.port.in.CommitReserveUseCase;
import com.erp.modules.inventory.domain.port.out.ReservaEstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CommitReserveUseCaseImpl implements CommitReserveUseCase {

    private final ReservaEstoqueRepository reservaRepo;

    public CommitReserveUseCaseImpl(ReservaEstoqueRepository reservaRepo) {
        this.reservaRepo = reservaRepo;
    }

    @Override
    public void commit(UUID saleUuid) {
        reservaRepo.findBySaleUuid(saleUuid).stream()
                .filter(r -> r.isActive())
                .forEach(reserva -> {
                    reserva.markCommitted();
                    reservaRepo.save(reserva);
                });
    }
}
