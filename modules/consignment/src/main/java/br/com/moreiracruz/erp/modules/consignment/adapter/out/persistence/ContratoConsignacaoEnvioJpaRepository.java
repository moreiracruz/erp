package br.com.moreiracruz.erp.modules.consignment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContratoConsignacaoEnvioJpaRepository extends JpaRepository<ContratoConsignacaoEnvioJpaEntity, Long> {
    Optional<ContratoConsignacaoEnvioJpaEntity> findByUuid(UUID uuid);
    List<ContratoConsignacaoEnvioJpaEntity> findByStatus(String status);
    List<ContratoConsignacaoEnvioJpaEntity> findByConsigneeUuid(UUID consigneeUuid);
    List<ContratoConsignacaoEnvioJpaEntity> findByStatusAndConsigneeUuid(String status, UUID consigneeUuid);
}
