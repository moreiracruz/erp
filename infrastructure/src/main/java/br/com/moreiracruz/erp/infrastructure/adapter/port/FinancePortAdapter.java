package br.com.moreiracruz.erp.infrastructure.adapter.port;

import br.com.moreiracruz.erp.modules.finance.domain.port.in.RegisterExpenseCommand;
import br.com.moreiracruz.erp.modules.finance.domain.port.in.RegisterExpenseUseCase;
import br.com.moreiracruz.erp.shared.kernel.FinancePort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class FinancePortAdapter implements FinancePort {

    private final RegisterExpenseUseCase registerExpenseUseCase;

    public FinancePortAdapter(RegisterExpenseUseCase registerExpenseUseCase) {
        this.registerExpenseUseCase = registerExpenseUseCase;
    }

    @Override
    public UUID registerSupplierExpense(BigDecimal amount, String description, UUID responsibleUuid, UUID referenceUuid) {
        return registerExpenseUseCase.register(new RegisterExpenseCommand(
                amount, description, "FORNECEDORES", LocalDate.now(), responsibleUuid)).uuid();
    }
}
