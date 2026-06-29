package br.com.moreiracruz.erp.modules.consignment.domain.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SentSettlementResponse(
        UUID uuid,
        UUID contratoUuid,
        BigDecimal totalAmount,
        String notes,
        Instant createdAt
) {}
