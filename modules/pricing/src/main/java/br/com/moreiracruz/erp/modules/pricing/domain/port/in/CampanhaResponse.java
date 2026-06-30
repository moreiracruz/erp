package br.com.moreiracruz.erp.modules.pricing.domain.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CampanhaResponse(
        UUID uuid,
        String name,
        String type,
        String targetType,
        UUID targetUuid,
        String targetCategory,
        BigDecimal discountValue,
        BigDecimal cashbackPct,
        Instant startsAt,
        Instant endsAt,
        boolean active
) {}
