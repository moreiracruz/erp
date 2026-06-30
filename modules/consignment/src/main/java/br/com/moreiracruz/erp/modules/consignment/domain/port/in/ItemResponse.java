package br.com.moreiracruz.erp.modules.consignment.domain.port.in;

import java.time.Instant;
import java.util.UUID;

public record ItemResponse(
        UUID uuid,
        UUID contratoUuid,
        UUID varianteUuid,
        int quantity,
        int remainingQuantity,
        int soldQuantity,
        int settledQuantity,
        int returnedQuantity,
        String status,
        Instant receivedAt,
        UUID soldSaleUuid
) {}
