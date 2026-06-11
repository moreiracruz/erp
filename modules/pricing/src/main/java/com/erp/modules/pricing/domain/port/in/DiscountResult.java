package com.erp.modules.pricing.domain.port.in;

import java.math.BigDecimal;
import java.util.List;

public record DiscountResult(
        BigDecimal discountAmount,
        BigDecimal cashbackAmount,
        List<AppliedRule> appliedRules
) {}
