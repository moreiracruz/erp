package com.erp.modules.pricing.application.usecase;

import com.erp.modules.pricing.domain.model.CampaignType;
import com.erp.modules.pricing.domain.model.Cupom;
import com.erp.modules.pricing.domain.port.in.CreateCouponCommand;
import com.erp.modules.pricing.domain.port.in.CreateCouponUseCase;
import com.erp.modules.pricing.domain.port.in.CupomResponse;
import com.erp.modules.pricing.domain.port.out.CupomRepository;
import com.erp.shared.exceptions.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateCouponUseCaseImpl implements CreateCouponUseCase {

    private final CupomRepository cupomRepository;

    public CreateCouponUseCaseImpl(CupomRepository cupomRepository) {
        this.cupomRepository = cupomRepository;
    }

    @Override
    public CupomResponse create(CreateCouponCommand cmd) {
        CampaignType type = CampaignType.valueOf(cmd.type());

        Cupom cupom = Cupom.create(cmd.code(), type, cmd.discountValue(),
                cmd.startsAt(), cmd.endsAt(), cmd.maxUsages());

        if (cupomRepository.findByCodeIgnoreCase(cmd.code()).isPresent()) {
            throw new ConflictException("Código de cupom já existe");
        }

        Cupom saved = cupomRepository.save(cupom);
        return toResponse(saved);
    }

    private CupomResponse toResponse(Cupom c) {
        return new CupomResponse(
                c.getUuid(), c.getCode(), c.getType().name(), c.getDiscountValue(),
                c.getStartsAt(), c.getEndsAt(), c.getMaxUsages(), c.getUsageCount(),
                c.isActive());
    }
}
