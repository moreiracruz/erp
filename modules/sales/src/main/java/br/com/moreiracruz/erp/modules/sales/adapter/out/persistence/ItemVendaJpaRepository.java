package br.com.moreiracruz.erp.modules.sales.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemVendaJpaRepository extends JpaRepository<ItemVendaJpaEntity, Long> {

    List<ItemVendaJpaEntity> findByVendaId(Long vendaId);
}
