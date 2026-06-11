package com.erp.modules.product.domain.port.out;

import com.erp.modules.product.domain.model.VarianteProduto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for persisting and querying {@link VarianteProduto} entities.
 */
public interface VarianteRepository {

    Optional<VarianteProduto> findByUuid(UUID uuid);

    Optional<VarianteProduto> findBySku(String sku);

    Optional<VarianteProduto> findByBarcode(String barcode);

    boolean existsBySku(String sku);

    boolean existsByBarcode(String barcode);

    List<VarianteProduto> findByProdutoId(Long produtoId);

    VarianteProduto save(VarianteProduto variante);
}
