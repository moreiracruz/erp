package com.erp.modules.sales.adapter.in.web;

import java.math.BigDecimal;

public record FinalizeSaleRequest(String paymentMethod, BigDecimal amountPaid,
                                  String couponCode, BigDecimal expectedTotal) {}
