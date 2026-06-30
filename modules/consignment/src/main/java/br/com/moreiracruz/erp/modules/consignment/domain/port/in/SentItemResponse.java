package br.com.moreiracruz.erp.modules.consignment.domain.port.in;

import java.time.Instant;
import java.util.UUID;

public record SentItemResponse(
        UUID uuid,
        UUID contratoUuid,
        UUID varianteUuid,
        int quantity,
        int availableQuantity,
        int soldQuantity,
        int settledQuantity,
        int returnedQuantity,
        String status,
        Instant sentAt
) {}
