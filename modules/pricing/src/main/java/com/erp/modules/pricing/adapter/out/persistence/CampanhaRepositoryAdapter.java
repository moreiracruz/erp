package com.erp.modules.pricing.adapter.out.persistence;

import com.erp.modules.pricing.domain.model.CampaignType;
import com.erp.modules.pricing.domain.model.Campanha;
import com.erp.modules.pricing.domain.model.TargetType;
import com.erp.modules.pricing.domain.port.out.CampanhaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CampanhaRepositoryAdapter implements CampanhaRepository {

    private final CampanhaJpaRepository jpaRepository;

    public CampanhaRepositoryAdapter(CampanhaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Campanha> findByUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid).map(this::toDomain);
    }

    @Override
    public List<Campanha> findActiveOverlapping(CampaignType type, TargetType targetType,
                                                 UUID targetUuid, String targetCategory,
                                                 Instant from, Instant to) {
        return jpaRepository.findActiveOverlapping(
                type.name(), targetType.name(), targetUuid, targetCategory, from, to)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Campanha> findAllActive() {
        return jpaRepository.findByActiveTrue()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Campanha save(Campanha campanha) {
        CampanhaJpaEntity entity = toEntity(campanha);
        CampanhaJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    // -------------------------------------------------------------------------
    // Mapping helpers
    // -------------------------------------------------------------------------

    private Campanha toDomain(CampanhaJpaEntity e) {
        return Campanha.restore(
                e.getId(), e.getUuid(), e.getName(),
                CampaignType.valueOf(e.getType()),
                TargetType.valueOf(e.getTargetType()),
                e.getTargetUuid(), e.getTargetCategory(),
                e.getDiscountValue(), e.getMinQuantity(), e.getCashbackPct(),
                e.getStartsAt(), e.getEndsAt(), e.isActive());
    }

    private CampanhaJpaEntity toEntity(Campanha c) {
        CampanhaJpaEntity e = new CampanhaJpaEntity();
        e.setId(c.getId());
        e.setUuid(c.getUuid());
        e.setName(c.getName());
        e.setType(c.getType().name());
        e.setTargetType(c.getTargetType().name());
        e.setTargetUuid(c.getTargetUuid());
        e.setTargetCategory(c.getTargetCategory());
        e.setDiscountValue(c.getDiscountValue());
        e.setMinQuantity(c.getMinQuantity());
        e.setCashbackPct(c.getCashbackPct());
        e.setStartsAt(c.getStartsAt());
        e.setEndsAt(c.getEndsAt());
        e.setActive(c.isActive());
        return e;
    }
}
