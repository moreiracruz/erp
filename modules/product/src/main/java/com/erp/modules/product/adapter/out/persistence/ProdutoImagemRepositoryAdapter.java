package com.erp.modules.product.adapter.out.persistence;

import com.erp.modules.product.domain.model.ProdutoImagem;
import com.erp.modules.product.domain.port.out.ProdutoImagemRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter implementing the domain's {@link ProdutoImagemRepository} outbound port
 * using Spring Data JPA.
 *
 * <p>Performs manual mapping between {@link ProdutoImagemJpaEntity} (persistence concern)
 * and {@link ProdutoImagem} (domain model).
 */
@Repository
public class ProdutoImagemRepositoryAdapter implements ProdutoImagemRepository {

    private final ProdutoImagemJpaRepository jpaRepo;

    public ProdutoImagemRepositoryAdapter(ProdutoImagemJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public List<ProdutoImagem> findByProdutoUuidOrderBySortOrder(UUID produtoUuid) {
        return jpaRepo.findByProdutoUuidOrderBySortOrder(produtoUuid)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<ProdutoImagem> findByIdAndProdutoUuid(Long id, UUID produtoUuid) {
        return jpaRepo.findByIdAndProdutoUuid(id, produtoUuid).map(this::toDomain);
    }

    @Override
    public int countByProdutoUuid(UUID produtoUuid) {
        return jpaRepo.countByProdutoUuid(produtoUuid);
    }

    @Override
    public long sumFileSizeByProdutoUuid(UUID produtoUuid) {
        return jpaRepo.sumFileSizeByProdutoUuid(produtoUuid);
    }

    @Override
    public int findMaxSortOrderByProdutoUuid(UUID produtoUuid) {
        return jpaRepo.findMaxSortOrderByProdutoUuid(produtoUuid);
    }

    @Override
    public ProdutoImagem save(ProdutoImagem imagem) {
        ProdutoImagemJpaEntity entity = toEntity(imagem);
        ProdutoImagemJpaEntity saved = jpaRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<ProdutoImagem> saveAll(List<ProdutoImagem> imagens) {
        List<ProdutoImagemJpaEntity> entities = imagens.stream()
                .map(this::toEntity)
                .toList();
        List<ProdutoImagemJpaEntity> saved = jpaRepo.saveAll(entities);
        return saved.stream().map(this::toDomain).toList();
    }

    @Override
    public void delete(ProdutoImagem imagem) {
        ProdutoImagemJpaEntity entity = toEntity(imagem);
        jpaRepo.delete(entity);
    }

    @Override
    @Transactional
    public void clearMainByProdutoUuid(UUID produtoUuid) {
        jpaRepo.clearMainByProdutoUuid(produtoUuid);
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private ProdutoImagem toDomain(ProdutoImagemJpaEntity e) {
        return ProdutoImagem.restore(
                e.getId(),
                e.getProdutoUuid(),
                e.getFilename(),
                e.getOriginalName(),
                e.getContentType(),
                e.getFileSize(),
                e.getSortOrder(),
                e.isMain(),
                e.getCreatedAt());
    }

    private ProdutoImagemJpaEntity toEntity(ProdutoImagem p) {
        ProdutoImagemJpaEntity e = new ProdutoImagemJpaEntity();
        if (p.getId() != null) {
            e.setId(p.getId());
        }
        e.setProdutoUuid(p.getProdutoUuid());
        e.setFilename(p.getFilename());
        e.setOriginalName(p.getOriginalName());
        e.setContentType(p.getContentType());
        e.setFileSize(p.getFileSize());
        e.setSortOrder(p.getSortOrder());
        e.setMain(p.isMain());
        e.setCreatedAt(p.getCreatedAt());
        return e;
    }
}
