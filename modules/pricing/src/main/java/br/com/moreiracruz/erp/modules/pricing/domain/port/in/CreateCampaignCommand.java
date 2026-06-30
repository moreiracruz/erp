package br.com.moreiracruz.erp.modules.pricing.domain.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateCampaignCommand(
        String name,
        String type,
        String targetType,
        UUID targetUuid,
        String targetCategory,
        BigDecimal discountValue,
        Integer minQuantity,
        BigDecimal cashbackPct,
        Instant startsAt,
        Instant endsAt
) {}
