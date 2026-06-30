package br.com.moreiracruz.erp.modules.pricing.application.usecase;

import br.com.moreiracruz.erp.modules.pricing.domain.model.CampaignType;
import br.com.moreiracruz.erp.modules.pricing.domain.model.Cupom;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CreateCouponCommand;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CreateCouponUseCase;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CupomResponse;
import br.com.moreiracruz.erp.modules.pricing.domain.port.out.CupomRepository;
import br.com.moreiracruz.erp.shared.exceptions.ConflictException;
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
