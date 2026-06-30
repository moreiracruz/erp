package br.com.moreiracruz.erp.modules.product.domain.port.out;

import br.com.moreiracruz.erp.modules.product.domain.model.Produto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for persisting and querying {@link Produto} aggregates.
 */
public interface ProdutoRepository {

    List<Produto> findAllActive();

    Optional<Produto> findByUuid(UUID uuid);

    boolean existsByNameIgnoreCaseAndActiveTrue(String name);

    Produto save(Produto produto);
}
