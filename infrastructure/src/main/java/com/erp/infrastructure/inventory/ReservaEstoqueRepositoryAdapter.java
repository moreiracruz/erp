package com.erp.infrastructure.inventory;

import com.erp.modules.inventory.domain.model.ReservaEstoque;
import com.erp.modules.inventory.domain.model.ReservaStatus;
import com.erp.modules.inventory.domain.port.out.ReservaEstoqueRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementing the domain's {@link ReservaEstoqueRepository} outbound port
 * using Spring Data JPA.
 *
 * <p>Performs manual mapping between {@link ReservaEstoqueJpaEntity} (persistence concern)
 * and {@link ReservaEstoque} (domain entity).
 */
@Repository
public class ReservaEstoqueRepositoryAdapter implements ReservaEstoqueRepository {

    private final ReservaEstoqueJpaRepository jpaRepository;

    public ReservaEstoqueRepositoryAdapter(ReservaEstoqueJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ReservaEstoque> findByUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid).map(this::toDomain);
    }

    @Override
    public List<ReservaEstoque> findBySaleUuid(UUID saleUuid) {
        return jpaRepository.findBySaleUuid(saleUuid).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservaEstoque> findExpiredActive(Instant before) {
        return jpaRepository.findExpiredActive(before).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public ReservaEstoque save(ReservaEstoque reserva) {
        ReservaEstoqueJpaEntity entity = toEntity(reserva);
        ReservaEstoqueJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    // -------------------------------------------------------------------------
    // Mapping helpers
    // -------------------------------------------------------------------------

    private ReservaEstoque toDomain(ReservaEstoqueJpaEntity e) {
        return ReservaEstoque.restore(
                e.getId(),
                e.getUuid(),
                e.getVarianteUuid(),
                e.getSaleUuid(),
                e.getQuantity(),
                ReservaStatus.valueOf(e.getStatus()),
                e.getCreatedAt(),
                e.getExpiresAt());
    }

    private ReservaEstoqueJpaEntity toEntity(ReservaEstoque r) {
        ReservaEstoqueJpaEntity e = new ReservaEstoqueJpaEntity();
        e.setId(r.getId());
        e.setUuid(r.getUuid());
        e.setVarianteUuid(r.getVarianteUuid());
        e.setSaleUuid(r.getSaleUuid());
        e.setQuantity(r.getQuantity());
        e.setStatus(r.getStatus().name());
        e.setCreatedAt(r.getCreatedAt());
        e.setExpiresAt(r.getExpiresAt());
        return e;
    }
}
