package br.com.moreiracruz.erp.modules.consignment.domain.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SentContractResponse(
        UUID uuid,
        UUID consigneeUuid,
        String code,
        String status,
        Instant openedAt,
        Instant closedAt,
        List<SentItemResponse> items
) {}
