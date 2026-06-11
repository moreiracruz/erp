package com.erp.modules.product.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ProdutoJpaEntity}.
 *
 * <p>Internal to the persistence adapter — do not expose outside the adapter layer.
 */
interface ProdutoJpaRepository extends JpaRepository<ProdutoJpaEntity, Long> {

    Optional<ProdutoJpaEntity> findByUuid(UUID uuid);

    boolean existsByNameIgnoreCaseAndActiveTrue(String name);
}
