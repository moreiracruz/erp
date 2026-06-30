package br.com.moreiracruz.erp.modules.pricing.domain.port.in;

public interface CreateCampaignUseCase {
    CampanhaResponse create(CreateCampaignCommand cmd);
}
