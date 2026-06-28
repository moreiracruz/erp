package br.com.moreiracruz.erp.modules.consignment.domain.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ContractResponse(
        UUID uuid,
        UUID consignorUuid,
        String code,
        String status,
        Instant openedAt,
        Instant closedAt,
        List<ItemResponse> items
) {}
