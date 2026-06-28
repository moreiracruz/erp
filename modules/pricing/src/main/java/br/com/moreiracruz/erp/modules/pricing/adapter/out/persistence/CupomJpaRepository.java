package br.com.moreiracruz.erp.modules.pricing.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface CupomJpaRepository extends JpaRepository<CupomJpaEntity, Long> {

    Optional<CupomJpaEntity> findByCodeIgnoreCase(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CupomJpaEntity c where lower(c.code) = lower(:code)")
    Optional<CupomJpaEntity> findWithLockByCodeIgnoreCase(@Param("code") String code);
}
