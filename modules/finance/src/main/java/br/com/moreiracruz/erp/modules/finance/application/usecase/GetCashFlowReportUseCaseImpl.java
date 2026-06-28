package br.com.moreiracruz.erp.modules.finance.application.usecase;

import br.com.moreiracruz.erp.modules.finance.domain.model.EntryType;
import br.com.moreiracruz.erp.modules.finance.domain.model.LancamentoFinanceiro;
import br.com.moreiracruz.erp.modules.finance.domain.port.in.CashFlowReport;
import br.com.moreiracruz.erp.modules.finance.domain.port.in.DailyEntry;
import br.com.moreiracruz.erp.modules.finance.domain.port.in.GetCashFlowReportUseCase;
import br.com.moreiracruz.erp.modules.finance.domain.port.in.LancamentoResponse;
import br.com.moreiracruz.erp.modules.finance.domain.port.out.LancamentoRepository;
import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetCashFlowReportUseCaseImpl implements GetCashFlowReportUseCase {

    private final LancamentoRepository lancamentoRepository;

    public GetCashFlowReportUseCaseImpl(LancamentoRepository lancamentoRepository) {
        this.lancamentoRepository = lancamentoRepository;
    }

    @Override
    public CashFlowReport getCashFlow(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new ValidationException("Datas de início e fim são obrigatórias");
        }
        if (from.isAfter(to)) {
            throw new ValidationException("Data de início deve ser anterior ou igual à data de fim");
        }
        if (ChronoUnit.DAYS.between(from, to) > 366) {
            throw new ValidationException("Intervalo máximo é de 366 dias");
        }

        List<LancamentoFinanceiro> entries = lancamentoRepository.findByCompetenceDateBetween(from, to);

        // Group by competence date
        Map<LocalDate, List<LancamentoFinanceiro>> byDate = entries.stream()
                .collect(Collectors.groupingBy(LancamentoFinanceiro::getCompetenceDate));

        BigDecimal totalReceita = BigDecimal.ZERO;
        BigDecimal totalDespesa = BigDecimal.ZERO;
        List<DailyEntry> dailyEntries = new ArrayList<>();

        // Iterate over each day in the range
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            List<LancamentoFinanceiro> dayEntries = byDate.getOrDefault(date, List.of());
            List<LancamentoResponse> receitas = new ArrayList<>();
            List<LancamentoResponse> despesas = new ArrayList<>();
            BigDecimal dayReceita = BigDecimal.ZERO;
            BigDecimal dayDespesa = BigDecimal.ZERO;

            for (LancamentoFinanceiro l : dayEntries) {
                LancamentoResponse r = toResponse(l);
                if (l.getType() == EntryType.RECEITA) {
                    receitas.add(r);
                    dayReceita = dayReceita.add(l.getAmount());
                } else {
                    despesas.add(r);
                    dayDespesa = dayDespesa.add(l.getAmount());
                }
            }

            totalReceita = totalReceita.add(dayReceita);
            totalDespesa = totalDespesa.add(dayDespesa);

            if (!dayEntries.isEmpty()) {
                dailyEntries.add(new DailyEntry(date, receitas, despesas, dayReceita.subtract(dayDespesa)));
            }
        }

        return new CashFlowReport(from, to, totalReceita, totalDespesa,
                totalReceita.subtract(totalDespesa), dailyEntries);
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
