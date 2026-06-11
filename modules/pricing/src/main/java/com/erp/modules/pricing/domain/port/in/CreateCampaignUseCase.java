package com.erp.modules.pricing.domain.port.in;

public interface CreateCampaignUseCase {
    CampanhaResponse create(CreateCampaignCommand cmd);
}
