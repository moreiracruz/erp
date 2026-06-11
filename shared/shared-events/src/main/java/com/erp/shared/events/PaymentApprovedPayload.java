package com.erp.shared.events;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payload for the PaymentApproved domain event.
 * Published by the finance module when a payment is successfully approved.
 */
public record PaymentApprovedPayload(
        UUID saleUuid,
        BigDecimal approvedAmount,
        String paymentMethod
) {}
