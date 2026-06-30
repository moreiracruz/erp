package br.com.moreiracruz.erp.modules.inventory.domain.port.out;

import br.com.moreiracruz.erp.modules.inventory.domain.model.ReservaEstoque;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaEstoqueRepository {

    Optional<ReservaEstoque> findByUuid(UUID uuid);

    List<ReservaEstoque> findBySaleUuid(UUID saleUuid);

    List<ReservaEstoque> findExpiredActive(Instant before);

    ReservaEstoque save(ReservaEstoque reserva);
}
