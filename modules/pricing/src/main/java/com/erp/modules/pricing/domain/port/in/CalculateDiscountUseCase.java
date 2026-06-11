package com.erp.modules.pricing.domain.port.in;

public interface CalculateDiscountUseCase {
    DiscountResult calculate(DiscountQuery query);
}
