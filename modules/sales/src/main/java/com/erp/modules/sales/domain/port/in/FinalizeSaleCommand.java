package com.erp.modules.sales.domain.port.in;

import java.math.BigDecimal;

/**
 * Command to finalize an open sale.
 *
 * @param paymentMethod the payment method (DINHEIRO, DEBITO, CREDITO, PIX)
 * @param amountPaid    the amount tendered by the customer (relevant for DINHEIRO)
 * @param couponCode    an optional coupon code for discount
 * @param expectedTotal the total the client expects — used as a safety check
 */
public record FinalizeSaleCommand(String paymentMethod, BigDecimal amountPaid,
                                  String couponCode, BigDecimal expectedTotal) {}
