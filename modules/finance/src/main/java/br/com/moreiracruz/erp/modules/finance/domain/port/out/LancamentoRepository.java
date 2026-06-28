package br.com.moreiracruz.erp.modules.finance.domain.port.out;

import br.com.moreiracruz.erp.modules.finance.domain.model.LancamentoFinanceiro;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for financial entry persistence.
 */
public interface LancamentoRepository {

    Optional<LancamentoFinanceiro> findByUuid(UUID uuid);

    LancamentoFinanceiro save(LancamentoFinanceiro lancamento);

    boolean existsBySaleUuid(UUID saleUuid);

    List<LancamentoFinanceiro> findByCompetenceDateBetween(LocalDate from, LocalDate to);
}
