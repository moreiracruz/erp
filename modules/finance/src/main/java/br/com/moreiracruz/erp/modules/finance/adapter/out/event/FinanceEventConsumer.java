package br.com.moreiracruz.erp.modules.finance.adapter.out.event;

import br.com.moreiracruz.erp.modules.finance.domain.model.EntryType;
import br.com.moreiracruz.erp.modules.finance.domain.model.LancamentoFinanceiro;
import br.com.moreiracruz.erp.modules.finance.domain.port.out.LancamentoRepository;
import br.com.moreiracruz.erp.shared.events.EventEnvelope;
import br.com.moreiracruz.erp.shared.events.SaleCompletedPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Listens for SaleCompletedEvent and creates a RECEITA entry.
 * Uses saleUuid as idempotency key to prevent duplicate entries.
 */
@Component
public class FinanceEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(FinanceEventConsumer.class);

    private final LancamentoRepository lancamentoRepository;

    public FinanceEventConsumer(LancamentoRepository lancamentoRepository) {
        this.lancamentoRepository = lancamentoRepository;
    }

    @EventListener
    public void onSaleCompleted(EventEnvelope<?> event) {
        if (!"SaleCompleted".equals(event.eventType()) || !(event.payload() instanceof SaleCompletedPayload payload)) {
            return;
        }

        // Idempotency check: skip if already processed.
        if (lancamentoRepository.existsBySaleUuid(payload.saleUuid())) {
            log.info("Sale {} already processed, skipping finance entry creation", payload.saleUuid());
            return;
        }

        LancamentoFinanceiro lancamento = LancamentoFinanceiro.create(
                EntryType.RECEITA,
                payload.total(),
                payload.paymentMethod(),
                "Venda " + payload.saleUuid().toString().substring(0, 8),
                null,
                LocalDate.now(),
                payload.operatorUuid(),
                payload.saleUuid()
        );

        lancamentoRepository.save(lancamento);
        log.info("Created RECEITA entry for sale {}", payload.saleUuid());
    }
}
