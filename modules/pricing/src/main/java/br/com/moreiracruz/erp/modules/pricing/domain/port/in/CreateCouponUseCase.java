package br.com.moreiracruz.erp.modules.pricing.domain.port.in;

public interface CreateCouponUseCase {
    CupomResponse create(CreateCouponCommand cmd);
}
