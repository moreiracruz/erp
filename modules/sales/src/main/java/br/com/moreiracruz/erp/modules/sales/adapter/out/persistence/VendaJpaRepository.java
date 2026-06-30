package br.com.moreiracruz.erp.modules.sales.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VendaJpaRepository extends JpaRepository<VendaJpaEntity, Long> {

    Optional<VendaJpaEntity> findByUuid(UUID uuid);
}
