package br.com.moreiracruz.erp.infrastructure.adapter.port;

import br.com.moreiracruz.erp.modules.finance.domain.model.EntryType;
import br.com.moreiracruz.erp.modules.finance.domain.model.LancamentoFinanceiro;
import br.com.moreiracruz.erp.modules.finance.domain.port.in.RegisterExpenseCommand;
import br.com.moreiracruz.erp.modules.finance.domain.port.in.RegisterExpenseUseCase;
import br.com.moreiracruz.erp.modules.finance.domain.port.out.LancamentoRepository;
import br.com.moreiracruz.erp.shared.kernel.FinancePort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class FinancePortAdapter implements FinancePort {

    private final RegisterExpenseUseCase registerExpenseUseCase;
    private final LancamentoRepository lancamentoRepository;

    public FinancePortAdapter(RegisterExpenseUseCase registerExpenseUseCase,
                              LancamentoRepository lancamentoRepository) {
        this.registerExpenseUseCase = registerExpenseUseCase;
        this.lancamentoRepository = lancamentoRepository;
    }

    @Override
    public UUID registerSupplierExpense(BigDecimal amount, String description, UUID responsibleUuid, UUID referenceUuid) {
        return registerExpenseUseCase.register(new RegisterExpenseCommand(
                amount, description, "FORNECEDORES", LocalDate.now(), responsibleUuid)).uuid();
    }

    @Override
    public UUID registerConsignmentRevenue(BigDecimal amount, String description, UUID responsibleUuid, UUID referenceUuid) {
        LancamentoFinanceiro lancamento = LancamentoFinanceiro.create(
                EntryType.RECEITA,
                amount,
                null,
                description,
                "CONSIGNACAO",
                LocalDate.now(),
                responsibleUuid,
                null);
        return lancamentoRepository.save(lancamento).getUuid();
    }
}
