package com.erp.modules.customer.domain.port.in;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for customer operations.
 */
public record ClienteResponse(
        UUID uuid,
        String fullName,
        String cpf,
        String email,
        String phone,
        LocalDate birthDate,
        boolean active
) {}
