package br.com.moreiracruz.erp.modules.product.adapter.out.persistence;

import br.com.moreiracruz.erp.modules.product.domain.model.Produto;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ProdutoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter implementing the domain's {@link ProdutoRepository} outbound port
 * using Spring Data JPA.
 *
 * <p>Performs manual mapping between {@link ProdutoJpaEntity} (persistence concern)
 * and {@link Produto} (domain aggregate root).
 */
@Repository
public class ProdutoRepositoryAdapter implements ProdutoRepository {

    private final ProdutoJpaRepository jpaRepo;

    public ProdutoRepositoryAdapter(ProdutoJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public List<Produto> findAllActive() {
        return jpaRepo.findByActiveTrue().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Produto> findByUuid(UUID uuid) {
        return jpaRepo.findByUuid(uuid).map(this::toDomain);
    }

    @Override
    public boolean existsByNameIgnoreCaseAndActiveTrue(String name) {
        return jpaRepo.existsByNameIgnoreCaseAndActiveTrue(name);
    }

    @Override
    public Produto save(Produto produto) {
        ProdutoJpaEntity entity = toEntity(produto);
        ProdutoJpaEntity saved = jpaRepo.save(entity);
        return toDomain(saved);
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private Produto toDomain(ProdutoJpaEntity e) {
        return Produto.restore(
                e.getId(),
                e.getUuid(),
                e.getName(),
                e.getBrand(),
                e.getCategory(),
                e.isActive(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }

    private ProdutoJpaEntity toEntity(Produto p) {
        ProdutoJpaEntity e = new ProdutoJpaEntity();
        if (p.getId() != null) {
            e.setId(p.getId());
        }
        e.setUuid(p.getUuid() != null ? p.getUuid() : UUID.randomUUID());
        e.setName(p.getName());
        e.setBrand(p.getBrand());
        e.setCategory(p.getCategory());
        e.setActive(p.isActive());
        e.setCreatedAt(p.getCreatedAt());
        e.setUpdatedAt(p.getUpdatedAt());
        return e;
    }
}
