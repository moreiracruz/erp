package com.erp.modules.pricing.domain.port.out;

import com.erp.modules.pricing.domain.model.Cupom;

import java.util.Optional;

public interface CupomRepository {

    Optional<Cupom> findByCodeIgnoreCase(String code);

    Cupom save(Cupom cupom);
}
