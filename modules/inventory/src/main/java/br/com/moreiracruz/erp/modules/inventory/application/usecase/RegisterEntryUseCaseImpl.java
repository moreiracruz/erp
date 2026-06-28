package br.com.moreiracruz.erp.modules.inventory.application.usecase;

import br.com.moreiracruz.erp.modules.inventory.domain.model.MovimentoEstoque;
import br.com.moreiracruz.erp.modules.inventory.domain.model.OperationType;
import br.com.moreiracruz.erp.modules.inventory.domain.port.in.RegisterEntryUseCase;
import br.com.moreiracruz.erp.modules.inventory.domain.port.in.StockEntryCommand;
import br.com.moreiracruz.erp.modules.inventory.domain.port.out.EstoqueItemRepository;
import br.com.moreiracruz.erp.modules.inventory.domain.port.out.MovimentoEstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterEntryUseCaseImpl implements RegisterEntryUseCase {

    private final EstoqueItemRepository estoqueItemRepo;
    private final MovimentoEstoqueRepository movimentoRepo;

    public RegisterEntryUseCaseImpl(EstoqueItemRepository estoqueItemRepo,
                                    MovimentoEstoqueRepository movimentoRepo) {
        this.estoqueItemRepo = estoqueItemRepo;
        this.movimentoRepo = movimentoRepo;
    }

    @Override
    public void registerEntry(StockEntryCommand cmd) {
        var item = estoqueItemRepo.findOrCreateByVarianteUuid(cmd.varianteUuid());
        item.incrementPhysical(cmd.quantity());
        estoqueItemRepo.save(item);
        movimentoRepo.save(MovimentoEstoque.of(
                cmd.varianteUuid(), OperationType.ENTRADA, cmd.quantity(), cmd.actorUuid(), cmd.referenceUuid()));
    }
}
