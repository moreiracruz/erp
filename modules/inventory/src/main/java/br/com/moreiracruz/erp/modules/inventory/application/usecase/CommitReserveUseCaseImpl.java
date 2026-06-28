package br.com.moreiracruz.erp.modules.inventory.application.usecase;

import br.com.moreiracruz.erp.modules.inventory.domain.port.in.CommitReserveUseCase;
import br.com.moreiracruz.erp.modules.inventory.domain.model.MovimentoEstoque;
import br.com.moreiracruz.erp.modules.inventory.domain.model.OperationType;
import br.com.moreiracruz.erp.modules.inventory.domain.port.out.EstoqueItemRepository;
import br.com.moreiracruz.erp.modules.inventory.domain.port.out.MovimentoEstoqueRepository;
import br.com.moreiracruz.erp.modules.inventory.domain.port.out.ReservaEstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CommitReserveUseCaseImpl implements CommitReserveUseCase {

    private final ReservaEstoqueRepository reservaRepo;
    private final EstoqueItemRepository estoqueItemRepo;
    private final MovimentoEstoqueRepository movimentoRepo;

    public CommitReserveUseCaseImpl(ReservaEstoqueRepository reservaRepo,
                                    EstoqueItemRepository estoqueItemRepo,
                                    MovimentoEstoqueRepository movimentoRepo) {
        this.reservaRepo = reservaRepo;
        this.estoqueItemRepo = estoqueItemRepo;
        this.movimentoRepo = movimentoRepo;
    }

    @Override
    public void commit(UUID saleUuid) {
        reservaRepo.findBySaleUuid(saleUuid).stream()
                .filter(r -> r.isActive())
                .forEach(reserva -> {
                    var item = estoqueItemRepo.findByVarianteUuidForUpdate(reserva.getVarianteUuid());
                    item.decrementReserved(reserva.getQuantity());
                    item.decrementPhysical(reserva.getQuantity());
                    estoqueItemRepo.save(item);

                    reserva.markCommitted();
                    reservaRepo.save(reserva);

                    movimentoRepo.save(MovimentoEstoque.of(
                            reserva.getVarianteUuid(), OperationType.SAIDA,
                            reserva.getQuantity(), null, reserva.getSaleUuid()));
                });
    }
}
