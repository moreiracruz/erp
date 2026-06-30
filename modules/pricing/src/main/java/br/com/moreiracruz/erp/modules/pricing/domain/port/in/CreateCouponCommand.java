package br.com.moreiracruz.erp.modules.pricing.domain.port.in;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateCouponCommand(
        String code,
        String type,
        BigDecimal discountValue,
        Instant startsAt,
        Instant endsAt,
        int maxUsages
) {}
