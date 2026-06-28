package br.com.moreiracruz.erp.modules.pricing.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CupomJpaRepository extends JpaRepository<CupomJpaEntity, Long> {

    Optional<CupomJpaEntity> findByCodeIgnoreCase(String code);
}
