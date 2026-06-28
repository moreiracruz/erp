package br.com.moreiracruz.erp.modules.consignment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface AcertoConsignacaoJpaRepository extends JpaRepository<AcertoConsignacaoJpaEntity, Long> {
    Optional<AcertoConsignacaoJpaEntity> findByUuid(UUID uuid);
}
