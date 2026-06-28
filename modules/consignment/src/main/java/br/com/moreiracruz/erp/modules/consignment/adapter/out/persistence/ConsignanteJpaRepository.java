package br.com.moreiracruz.erp.modules.consignment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface ConsignanteJpaRepository extends JpaRepository<ConsignanteJpaEntity, Long> {
    Optional<ConsignanteJpaEntity> findByUuid(UUID uuid);
}
