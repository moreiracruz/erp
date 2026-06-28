package br.com.moreiracruz.erp.modules.consignment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ContratoConsignacaoJpaRepository extends JpaRepository<ContratoConsignacaoJpaEntity, Long> {
    Optional<ContratoConsignacaoJpaEntity> findByUuid(UUID uuid);
    List<ContratoConsignacaoJpaEntity> findByStatus(String status);
    List<ContratoConsignacaoJpaEntity> findByConsignanteUuid(UUID consignanteUuid);
    List<ContratoConsignacaoJpaEntity> findByStatusAndConsignanteUuid(String status, UUID consignanteUuid);
}
