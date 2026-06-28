package br.com.moreiracruz.erp.modules.finance.adapter.in.web;

import br.com.moreiracruz.erp.modules.finance.domain.port.in.CashFlowReport;
import br.com.moreiracruz.erp.modules.finance.domain.port.in.GetCashFlowReportUseCase;
import br.com.moreiracruz.erp.modules.finance.domain.port.in.LancamentoResponse;
import br.com.moreiracruz.erp.modules.finance.domain.port.in.RegisterExpenseCommand;
import br.com.moreiracruz.erp.modules.finance.domain.port.in.RegisterExpenseUseCase;
import br.com.moreiracruz.erp.modules.finance.domain.port.out.LancamentoRepository;
import br.com.moreiracruz.erp.shared.exceptions.NotFoundException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST adapter exposing finance operations under {@code /api/v1/finance}.
 */
@RestController
@RequestMapping("/api/v1/finance")
public class FinanceController {

    private final RegisterExpenseUseCase registerExpenseUseCase;
    private final GetCashFlowReportUseCase getCashFlowReportUseCase;
    private final LancamentoRepository lancamentoRepository;

    public FinanceController(RegisterExpenseUseCase registerExpenseUseCase,
                              GetCashFlowReportUseCase getCashFlowReportUseCase,
                              LancamentoRepository lancamentoRepository) {
        this.registerExpenseUseCase = registerExpenseUseCase;
        this.getCashFlowReportUseCase = getCashFlowReportUseCase;
        this.lancamentoRepository = lancamentoRepository;
    }

    @PostMapping("/expenses")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_FINANCE')")
    public ResponseEntity<LancamentoResponse> registerExpense(@RequestBody RegisterExpenseCommand cmd) {
        LancamentoResponse response = registerExpenseUseCase.register(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/cash-flow")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_FINANCE')")
    public ResponseEntity<CashFlowReport> getCashFlow(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(getCashFlowReportUseCase.getCashFlow(from, to));
    }

    @GetMapping("/entries/{uuid}")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_FINANCE')")
    public ResponseEntity<LancamentoResponse> findByUuid(@PathVariable UUID uuid) {
        var lancamento = lancamentoRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Lançamento não encontrado"));
        return ResponseEntity.ok(new LancamentoResponse(
                lancamento.getUuid(),
                lancamento.getType().name(),
                lancamento.getAmount(),
                lancamento.getPaymentMethod(),
                lancamento.getDescription(),
                lancamento.getCategory(),
                lancamento.getCompetenceDate()
        ));
    }
}
