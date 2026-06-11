package com.erp.modules.finance.adapter.out.persistence;

import com.erp.modules.finance.domain.model.EntryType;
import com.erp.modules.finance.domain.model.LancamentoFinanceiro;
import com.erp.modules.finance.domain.port.out.LancamentoRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LancamentoRepositoryAdapter implements LancamentoRepository {

    private final LancamentoJpaRepository jpaRepository;

    public LancamentoRepositoryAdapter(LancamentoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<LancamentoFinanceiro> findByUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid).map(this::toDomain);
    }

    @Override
    public LancamentoFinanceiro save(LancamentoFinanceiro lancamento) {
        LancamentoFinanceiroJpaEntity entity = toEntity(lancamento);
        LancamentoFinanceiroJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsBySaleUuid(UUID saleUuid) {
        return jpaRepository.existsBySaleUuid(saleUuid);
    }

    @Override
    public List<LancamentoFinanceiro> findByCompetenceDateBetween(LocalDate from, LocalDate to) {
        return jpaRepository.findByCompetenceDateBetween(from, to).stream()
                .map(this::toDomain)
                .toList();
    }

    private LancamentoFinanceiro toDomain(LancamentoFinanceiroJpaEntity entity) {
        return LancamentoFinanceiro.restore(
                entity.getUuid(),
                EntryType.valueOf(entity.getType()),
                entity.getAmount(),
                entity.getPaymentMethod(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getCompetenceDate(),
                entity.getResponsibleUuid(),
                entity.getSaleUuid(),
                entity.getCreatedAt()
        );
    }

    private LancamentoFinanceiroJpaEntity toEntity(LancamentoFinanceiro l) {
        LancamentoFinanceiroJpaEntity entity = new LancamentoFinanceiroJpaEntity();
        entity.setUuid(l.getUuid());
        entity.setType(l.getType().name());
        entity.setAmount(l.getAmount());
        entity.setPaymentMethod(l.getPaymentMethod());
        entity.setDescription(l.getDescription());
        entity.setCategory(l.getCategory());
        entity.setCompetenceDate(l.getCompetenceDate());
        entity.setResponsibleUuid(l.getResponsibleUuid());
        entity.setSaleUuid(l.getSaleUuid());
        entity.setCreatedAt(l.getCreatedAt());
        return entity;
    }
}
