package com.erp.modules.product.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ProdutoImagemJpaEntity}.
 *
 * <p>Internal to the persistence adapter — do not expose outside the adapter layer.
 */
interface ProdutoImagemJpaRepository extends JpaRepository<ProdutoImagemJpaEntity, Long> {

    List<ProdutoImagemJpaEntity> findByProdutoUuidOrderBySortOrder(UUID produtoUuid);

    Optional<ProdutoImagemJpaEntity> findByIdAndProdutoUuid(Long id, UUID produtoUuid);

    int countByProdutoUuid(UUID produtoUuid);

    @Query("SELECT COALESCE(SUM(e.fileSize), 0) FROM ProdutoImagemJpaEntity e WHERE e.produtoUuid = :produtoUuid")
    long sumFileSizeByProdutoUuid(@Param("produtoUuid") UUID produtoUuid);

    @Query("SELECT COALESCE(MAX(e.sortOrder), -1) FROM ProdutoImagemJpaEntity e WHERE e.produtoUuid = :produtoUuid")
    int findMaxSortOrderByProdutoUuid(@Param("produtoUuid") UUID produtoUuid);

    @Modifying
    @Query("UPDATE ProdutoImagemJpaEntity e SET e.main = false WHERE e.produtoUuid = :produtoUuid")
    void clearMainByProdutoUuid(@Param("produtoUuid") UUID produtoUuid);
}
