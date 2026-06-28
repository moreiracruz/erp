package br.com.moreiracruz.erp.modules.pricing.application.usecase;

import br.com.moreiracruz.erp.modules.pricing.domain.model.Cupom;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.ConfirmCouponUsageUseCase;
import br.com.moreiracruz.erp.modules.pricing.domain.port.out.CupomRepository;
import br.com.moreiracruz.erp.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConfirmCouponUsageUseCaseImpl implements ConfirmCouponUsageUseCase {

    private final CupomRepository cupomRepository;

    public ConfirmCouponUsageUseCaseImpl(CupomRepository cupomRepository) {
        this.cupomRepository = cupomRepository;
    }

    @Override
    public void confirm(String couponCode) {
        Cupom cupom = cupomRepository.findByCodeIgnoreCase(couponCode)
                .orElseThrow(() -> new NotFoundException("Cupom não encontrado: " + couponCode));

        cupom.incrementUsage();
        cupomRepository.save(cupom);
    }
}
