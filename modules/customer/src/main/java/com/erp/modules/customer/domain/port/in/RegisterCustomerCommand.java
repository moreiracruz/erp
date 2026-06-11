package com.erp.modules.customer.domain.port.in;

import java.time.LocalDate;

/**
 * Command for registering a new customer.
 */
public record RegisterCustomerCommand(
        String fullName,
        String cpf,
        String email,
        String phone,
        LocalDate birthDate
) {}
