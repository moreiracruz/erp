package com.erp.modules.finance.application.usecase;

import com.erp.modules.finance.domain.model.EntryType;
import com.erp.modules.finance.domain.model.LancamentoFinanceiro;
import com.erp.modules.finance.domain.port.in.LancamentoResponse;
import com.erp.modules.finance.domain.port.in.RegisterExpenseCommand;
import com.erp.modules.finance.domain.port.in.RegisterExpenseUseCase;
import com.erp.modules.finance.domain.port.out.LancamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterExpenseUseCaseImpl implements RegisterExpenseUseCase {

    private final LancamentoRepository lancamentoRepository;

    public RegisterExpenseUseCaseImpl(LancamentoRepository lancamentoRepository) {
        this.lancamentoRepository = lancamentoRepository;
    }

    @Override
    public LancamentoResponse register(RegisterExpenseCommand cmd) {
        LancamentoFinanceiro lancamento = LancamentoFinanceiro.create(
                EntryType.DESPESA,
                cmd.amount(),
                null, // no payment method for expenses
                cmd.description(),
                cmd.category(),
                cmd.competenceDate(),
                cmd.responsibleUuid(),
                null  // no sale UUID for manual expenses
        );

        LancamentoFinanceiro saved = lancamentoRepository.save(lancamento);
        return toResponse(saved);
    }

    private LancamentoResponse toResponse(LancamentoFinanceiro l) {
        return new LancamentoResponse(
                l.getUuid(),
                l.getType().name(),
                l.getAmount(),
                l.getPaymentMethod(),
                l.getDescription(),
                l.getCategory(),
                l.getCompetenceDate()
        );
    }
}
