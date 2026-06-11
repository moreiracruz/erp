package com.erp.modules.product.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link VarianteJpaEntity}.
 *
 * <p>Internal to the persistence adapter — do not expose outside the adapter layer.
 */
interface VarianteJpaRepository extends JpaRepository<VarianteJpaEntity, Long> {

    Optional<VarianteJpaEntity> findByUuid(UUID uuid);

    Optional<VarianteJpaEntity> findBySku(String sku);

    Optional<VarianteJpaEntity> findByBarcode(String barcode);

    boolean existsBySku(String sku);

    boolean existsByBarcode(String barcode);

    List<VarianteJpaEntity> findByProdutoId(Long produtoId);
}
