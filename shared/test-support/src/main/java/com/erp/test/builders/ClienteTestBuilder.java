package com.erp.test.builders;

import com.erp.modules.customer.domain.model.Cliente;

import java.time.LocalDate;

/**
 * Fluent builder for creating {@link Cliente} domain objects in tests.
 */
public class ClienteTestBuilder {

    private String fullName = "Maria da Silva";
    private String cpf = "52998224725"; // Valid CPF
    private String email = "maria@example.com";
    private String phone = "11999887766";
    private LocalDate birthDate = LocalDate.of(1990, 5, 15);

    private ClienteTestBuilder() {}

    public static ClienteTestBuilder aCliente() {
        return new ClienteTestBuilder();
    }

    public ClienteTestBuilder withFullName(String fullName) { this.fullName = fullName; return this; }
    public ClienteTestBuilder withCpf(String cpf) { this.cpf = cpf; return this; }
    public ClienteTestBuilder withEmail(String email) { this.email = email; return this; }
    public ClienteTestBuilder withPhone(String phone) { this.phone = phone; return this; }
    public ClienteTestBuilder withBirthDate(LocalDate birthDate) { this.birthDate = birthDate; return this; }

    /** Build domain object using the create factory method. */
    public Cliente build() {
        return Cliente.create(fullName, cpf, email, phone, birthDate);
    }
}
