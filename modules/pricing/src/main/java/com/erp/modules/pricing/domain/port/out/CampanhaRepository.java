package com.erp.modules.pricing.domain.port.out;

import com.erp.modules.pricing.domain.model.CampaignType;
import com.erp.modules.pricing.domain.model.Campanha;
import com.erp.modules.pricing.domain.model.TargetType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampanhaRepository {

    Optional<Campanha> findByUuid(UUID uuid);

    List<Campanha> findActiveOverlapping(CampaignType type, TargetType targetType,
                                         UUID targetUuid, String targetCategory,
                                         Instant from, Instant to);

    List<Campanha> findAllActive();

    Campanha save(Campanha campanha);
}
