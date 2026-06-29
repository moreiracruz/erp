package br.com.moreiracruz.erp.modules.consignment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConsignatarioEnvioJpaRepository extends JpaRepository<ConsignatarioEnvioJpaEntity, Long> {
    Optional<ConsignatarioEnvioJpaEntity> findByUuid(UUID uuid);
}
