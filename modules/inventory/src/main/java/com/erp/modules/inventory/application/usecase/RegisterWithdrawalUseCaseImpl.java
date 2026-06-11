package com.erp.modules.inventory.application.usecase;

import com.erp.modules.inventory.domain.model.MovimentoEstoque;
import com.erp.modules.inventory.domain.model.OperationType;
import com.erp.modules.inventory.domain.port.in.RegisterWithdrawalUseCase;
import com.erp.modules.inventory.domain.port.in.StockWithdrawalCommand;
import com.erp.modules.inventory.domain.port.out.EstoqueItemRepository;
import com.erp.modules.inventory.domain.port.out.MovimentoEstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterWithdrawalUseCaseImpl implements RegisterWithdrawalUseCase {

    private final EstoqueItemRepository estoqueItemRepo;
    private final MovimentoEstoqueRepository movimentoRepo;

    public RegisterWithdrawalUseCaseImpl(EstoqueItemRepository estoqueItemRepo,
                                         MovimentoEstoqueRepository movimentoRepo) {
        this.estoqueItemRepo = estoqueItemRepo;
        this.movimentoRepo = movimentoRepo;
    }

    @Override
    public void registerWithdrawal(StockWithdrawalCommand cmd) {
        var item = estoqueItemRepo.findOrCreateByVarianteUuid(cmd.varianteUuid());
        item.decrementPhysical(cmd.quantity());
        estoqueItemRepo.save(item);
        movimentoRepo.save(MovimentoEstoque.of(
                cmd.varianteUuid(), OperationType.SAIDA, cmd.quantity(), cmd.actorUuid(), null));
    }
}
