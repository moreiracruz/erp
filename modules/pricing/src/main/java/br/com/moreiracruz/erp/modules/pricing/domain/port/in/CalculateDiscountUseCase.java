package br.com.moreiracruz.erp.modules.pricing.domain.port.in;

public interface CalculateDiscountUseCase {
    DiscountResult calculate(DiscountQuery query);
}
