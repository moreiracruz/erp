package br.com.moreiracruz.erp.modules.pricing.domain.port.in;

import java.math.BigDecimal;

public record AppliedRule(String type, String code, BigDecimal discount) {}
