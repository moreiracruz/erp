package com.erp.modules.product.domain.port.out;

import com.erp.modules.product.domain.model.ProdutoImagem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for persisting and querying {@link ProdutoImagem} entities.
 */
public interface ProdutoImagemRepository {

    List<ProdutoImagem> findByProdutoUuidOrderBySortOrder(UUID produtoUuid);

    Optional<ProdutoImagem> findByIdAndProdutoUuid(Long id, UUID produtoUuid);

    int countByProdutoUuid(UUID produtoUuid);

    long sumFileSizeByProdutoUuid(UUID produtoUuid);

    int findMaxSortOrderByProdutoUuid(UUID produtoUuid);

    ProdutoImagem save(ProdutoImagem imagem);

    List<ProdutoImagem> saveAll(List<ProdutoImagem> imagens);

    void delete(ProdutoImagem imagem);

    void clearMainByProdutoUuid(UUID produtoUuid);
}
