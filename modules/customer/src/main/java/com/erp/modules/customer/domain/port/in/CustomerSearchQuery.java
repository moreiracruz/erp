package com.erp.modules.customer.domain.port.in;

import java.util.UUID;

/**
 * Query parameters for searching customers.
 */
public record CustomerSearchQuery(
        String cpf,
        String name,
        UUID uuid,
        int page,
        int size
) {}
