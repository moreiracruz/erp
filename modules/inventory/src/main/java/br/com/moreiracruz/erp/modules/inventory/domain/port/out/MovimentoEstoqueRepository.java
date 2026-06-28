package br.com.moreiracruz.erp.modules.inventory.domain.port.out;

import br.com.moreiracruz.erp.modules.inventory.domain.model.MovimentoEstoque;

import java.util.List;
import java.util.UUID;

public interface MovimentoEstoqueRepository {

    void save(MovimentoEstoque movimento);

    List<MovimentoEstoque> findByVarianteUuid(UUID varianteUuid);
}
