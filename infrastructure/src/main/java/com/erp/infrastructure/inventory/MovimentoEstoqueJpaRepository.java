package com.erp.infrastructure.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link MovimentoEstoqueJpaEntity}.
 */
public interface MovimentoEstoqueJpaRepository extends JpaRepository<MovimentoEstoqueJpaEntity, Long> {

    List<MovimentoEstoqueJpaEntity> findByVarianteUuid(UUID varianteUuid);
}
