package br.com.moreiracruz.erp.modules.consignment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemConsignacaoEnvioJpaRepository extends JpaRepository<ItemConsignacaoEnvioJpaEntity, Long> {
    Optional<ItemConsignacaoEnvioJpaEntity> findByUuid(UUID uuid);
    List<ItemConsignacaoEnvioJpaEntity> findByContratoUuid(UUID contratoUuid);
}
