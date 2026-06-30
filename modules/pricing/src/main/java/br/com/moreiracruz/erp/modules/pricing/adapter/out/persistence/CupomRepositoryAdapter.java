package br.com.moreiracruz.erp.modules.pricing.adapter.out.persistence;

import br.com.moreiracruz.erp.modules.pricing.domain.model.CampaignType;
import br.com.moreiracruz.erp.modules.pricing.domain.model.Cupom;
import br.com.moreiracruz.erp.modules.pricing.domain.port.out.CupomRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CupomRepositoryAdapter implements CupomRepository {

    private final CupomJpaRepository jpaRepository;

    public CupomRepositoryAdapter(CupomJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Cupom> findByCodeIgnoreCase(String code) {
        return jpaRepository.findByCodeIgnoreCase(code).map(this::toDomain);
    }

    @Override
    public Optional<Cupom> findByCodeIgnoreCaseForUpdate(String code) {
        return jpaRepository.findWithLockByCodeIgnoreCase(code).map(this::toDomain);
    }

    @Override
    public Cupom save(Cupom cupom) {
        CupomJpaEntity entity = toEntity(cupom);
        CupomJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    // -------------------------------------------------------------------------
    // Mapping helpers
    // -------------------------------------------------------------------------

    private Cupom toDomain(CupomJpaEntity e) {
        return Cupom.restore(
                e.getId(), e.getUuid(), e.getCode(),
                CampaignType.valueOf(e.getType()),
                e.getDiscountValue(), e.getStartsAt(), e.getEndsAt(),
                e.getMaxUsages(), e.getUsageCount(), e.isActive(), e.getVersion());
    }

    private CupomJpaEntity toEntity(Cupom c) {
        CupomJpaEntity e = new CupomJpaEntity();
        e.setId(c.getId());
        e.setUuid(c.getUuid());
        e.setCode(c.getCode());
        e.setType(c.getType().name());
        e.setDiscountValue(c.getDiscountValue());
        e.setStartsAt(c.getStartsAt());
        e.setEndsAt(c.getEndsAt());
        e.setMaxUsages(c.getMaxUsages());
        e.setUsageCount(c.getUsageCount());
        e.setActive(c.isActive());
        e.setVersion(c.getVersion());
        return e;
    }
}
