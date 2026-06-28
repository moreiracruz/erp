package br.com.moreiracruz.erp.modules.pricing.application.usecase;

import br.com.moreiracruz.erp.modules.pricing.domain.model.CampaignType;
import br.com.moreiracruz.erp.modules.pricing.domain.model.Campanha;
import br.com.moreiracruz.erp.modules.pricing.domain.model.TargetType;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CampanhaResponse;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CreateCampaignCommand;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CreateCampaignUseCase;
import br.com.moreiracruz.erp.modules.pricing.domain.port.out.CampanhaRepository;
import br.com.moreiracruz.erp.shared.exceptions.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CreateCampaignUseCaseImpl implements CreateCampaignUseCase {

    private final CampanhaRepository campanhaRepository;

    public CreateCampaignUseCaseImpl(CampanhaRepository campanhaRepository) {
        this.campanhaRepository = campanhaRepository;
    }

    @Override
    public CampanhaResponse create(CreateCampaignCommand cmd) {
        CampaignType type = CampaignType.valueOf(cmd.type());
        TargetType targetType = TargetType.valueOf(cmd.targetType());

        Campanha campanha = Campanha.create(
                cmd.name(), type, targetType, cmd.targetUuid(), cmd.targetCategory(),
                cmd.discountValue(), cmd.minQuantity(), cmd.cashbackPct(),
                cmd.startsAt(), cmd.endsAt());

        List<Campanha> overlapping = campanhaRepository.findActiveOverlapping(
                type, targetType, cmd.targetUuid(), cmd.targetCategory(),
                cmd.startsAt(), cmd.endsAt());

        if (!overlapping.isEmpty()) {
            throw new ConflictException("Conflito de campanha");
        }

        Campanha saved = campanhaRepository.save(campanha);
        return toResponse(saved);
    }

    private CampanhaResponse toResponse(Campanha c) {
        return new CampanhaResponse(
                c.getUuid(), c.getName(), c.getType().name(), c.getTargetType().name(),
                c.getTargetUuid(), c.getTargetCategory(), c.getDiscountValue(),
                c.getCashbackPct(), c.getStartsAt(), c.getEndsAt(), c.isActive());
    }
}
