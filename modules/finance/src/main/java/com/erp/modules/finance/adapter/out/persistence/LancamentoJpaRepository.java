package com.erp.modules.finance.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LancamentoJpaRepository extends JpaRepository<LancamentoFinanceiroJpaEntity, Long> {

    Optional<LancamentoFinanceiroJpaEntity> findByUuid(UUID uuid);

    boolean existsBySaleUuid(UUID saleUuid);

    List<LancamentoFinanceiroJpaEntity> findByCompetenceDateBetween(LocalDate from, LocalDate to);
}
