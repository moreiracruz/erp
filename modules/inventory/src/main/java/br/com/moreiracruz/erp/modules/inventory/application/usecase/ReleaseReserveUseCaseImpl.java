package br.com.moreiracruz.erp.modules.inventory.application.usecase;

import br.com.moreiracruz.erp.modules.inventory.domain.model.MovimentoEstoque;
import br.com.moreiracruz.erp.modules.inventory.domain.model.OperationType;
import br.com.moreiracruz.erp.modules.inventory.domain.port.in.ReleaseReserveUseCase;
import br.com.moreiracruz.erp.modules.inventory.domain.port.out.EstoqueItemRepository;
import br.com.moreiracruz.erp.modules.inventory.domain.port.out.MovimentoEstoqueRepository;
import br.com.moreiracruz.erp.modules.inventory.domain.port.out.ReservaEstoqueRepository;
import br.com.moreiracruz.erp.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ReleaseReserveUseCaseImpl implements ReleaseReserveUseCase {

    private final ReservaEstoqueRepository reservaRepo;
    private final EstoqueItemRepository estoqueItemRepo;
    private final MovimentoEstoqueRepository movimentoRepo;

    public ReleaseReserveUseCaseImpl(ReservaEstoqueRepository reservaRepo,
                                     EstoqueItemRepository estoqueItemRepo,
                                     MovimentoEstoqueRepository movimentoRepo) {
        this.reservaRepo = reservaRepo;
        this.estoqueItemRepo = estoqueItemRepo;
        this.movimentoRepo = movimentoRepo;
    }

    @Override
    public void release(UUID reservaUuid) {
        var reserva = reservaRepo.findByUuid(reservaUuid)
                .filter(r -> r.isActive())
                .orElseThrow(() -> new NotFoundException(
                        "Reserva não encontrada ou não está ativa: " + reservaUuid));

        var item = estoqueItemRepo.findByVarianteUuidForUpdate(reserva.getVarianteUuid());
        item.decrementReserved(reserva.getQuantity());
        estoqueItemRepo.save(item);

        reserva.markReleased();
        reservaRepo.save(reserva);

        movimentoRepo.save(MovimentoEstoque.of(
                reserva.getVarianteUuid(), OperationType.LIBERACAO_RESERVA,
                reserva.getQuantity(), null, reserva.getSaleUuid()));
    }
}
