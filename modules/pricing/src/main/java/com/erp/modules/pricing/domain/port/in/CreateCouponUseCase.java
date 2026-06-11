package com.erp.modules.pricing.domain.port.in;

public interface CreateCouponUseCase {
    CupomResponse create(CreateCouponCommand cmd);
}
