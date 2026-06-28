package br.com.moreiracruz.erp.modules.pricing.domain.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CupomResponse(
        UUID uuid,
        String code,
        String type,
        BigDecimal discountValue,
        Instant startsAt,
        Instant endsAt,
        int maxUsages,
        int usageCount,
        boolean active
) {}
