package com.erp.modules.product.adapter.out.persistence;

import com.erp.modules.product.domain.model.VarianteProduto;
import com.erp.modules.product.domain.port.out.VarianteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence adapter implementing the domain's {@link VarianteRepository} outbound port
 * using Spring Data JPA.
 *
 * <p>Performs manual mapping between {@link VarianteJpaEntity} (persistence concern)
 * and {@link VarianteProduto} (domain entity).
 */
@Repository
public class VarianteRepositoryAdapter implements VarianteRepository {

    private final VarianteJpaRepository jpaRepo;

    public VarianteRepositoryAdapter(VarianteJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Optional<VarianteProduto> findByUuid(UUID uuid) {
        return jpaRepo.findByUuid(uuid).map(this::toDomain);
    }

    @Override
    public Optional<VarianteProduto> findBySku(String sku) {
        return jpaRepo.findBySku(sku).map(this::toDomain);
    }

    @Override
    public Optional<VarianteProduto> findByBarcode(String barcode) {
        return jpaRepo.findByBarcode(barcode).map(this::toDomain);
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepo.existsBySku(sku);
    }

    @Override
    public boolean existsByBarcode(String barcode) {
        return jpaRepo.existsByBarcode(barcode);
    }

    @Override
    public List<VarianteProduto> findByProdutoId(Long produtoId) {
        return jpaRepo.findByProdutoId(produtoId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public VarianteProduto save(VarianteProduto variante) {
        VarianteJpaEntity entity = toEntity(variante);
        VarianteJpaEntity saved = jpaRepo.save(entity);
        return toDomain(saved);
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private VarianteProduto toDomain(VarianteJpaEntity e) {
        return VarianteProduto.restore(
                e.getId(),
                e.getUuid(),
                e.getProdutoId(),
                e.getProdutoUuid(),
                e.getSku(),
                e.getSize(),
                e.getColor(),
                e.getBarcode(),
                e.getPrice(),
                e.getCost(),
                e.isActive(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }

    private VarianteJpaEntity toEntity(VarianteProduto v) {
        VarianteJpaEntity e = new VarianteJpaEntity();
        if (v.getId() != null) {
            e.setId(v.getId());
        }
        e.setUuid(v.getUuid() != null ? v.getUuid() : UUID.randomUUID());
        e.setProdutoId(v.getProdutoId());
        e.setProdutoUuid(v.getProdutoUuid());
        e.setSku(v.getSku().value());
        e.setSize(v.getSize());
        e.setColor(v.getColor());
        e.setBarcode(v.getBarcode().value());
        e.setPrice(v.getPrice().amount());
        e.setCost(v.getCost().amount());
        e.setActive(v.isActive());
        e.setCreatedAt(v.getCreatedAt());
        e.setUpdatedAt(v.getUpdatedAt());
        return e;
    }
}
