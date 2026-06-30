package br.com.moreiracruz.erp.modules.pricing.domain.port.out;

import br.com.moreiracruz.erp.modules.pricing.domain.model.Cupom;

import java.util.Optional;

public interface CupomRepository {

    Optional<Cupom> findByCodeIgnoreCase(String code);

    Optional<Cupom> findByCodeIgnoreCaseForUpdate(String code);

    Cupom save(Cupom cupom);
}
