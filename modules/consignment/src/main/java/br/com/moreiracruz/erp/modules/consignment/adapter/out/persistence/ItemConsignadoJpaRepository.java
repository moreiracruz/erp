package br.com.moreiracruz.erp.modules.consignment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ItemConsignadoJpaRepository extends JpaRepository<ItemConsignadoJpaEntity, Long> {
    Optional<ItemConsignadoJpaEntity> findByUuid(UUID uuid);
    List<ItemConsignadoJpaEntity> findByContratoUuid(UUID contratoUuid);
    List<ItemConsignadoJpaEntity> findByVarianteUuidAndRemainingQuantityGreaterThanOrderByReceivedAtAsc(UUID varianteUuid, int remainingQuantity);
}
