package br.com.moreiracruz.erp.modules.sales.domain.port.out;

import br.com.moreiracruz.erp.modules.sales.domain.model.Venda;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for persisting and retrieving sales.
 */
public interface VendaRepository {

    Optional<Venda> findByUuid(UUID uuid);

    Venda save(Venda venda);
}
