package br.com.moreiracruz.erp.modules.consignment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface AcertoItemJpaRepository extends JpaRepository<AcertoItemJpaEntity, Long> {
    List<AcertoItemJpaEntity> findByAcertoUuid(UUID acertoUuid);
}
