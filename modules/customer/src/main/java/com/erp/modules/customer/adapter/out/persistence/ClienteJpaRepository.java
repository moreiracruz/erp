package com.erp.modules.customer.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteJpaRepository extends JpaRepository<ClienteJpaEntity, Long> {

    Optional<ClienteJpaEntity> findByUuid(UUID uuid);

    Optional<ClienteJpaEntity> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    @Query("SELECT c FROM ClienteJpaEntity c WHERE LOWER(c.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<ClienteJpaEntity> searchByName(@Param("name") String name, Pageable pageable);
}
