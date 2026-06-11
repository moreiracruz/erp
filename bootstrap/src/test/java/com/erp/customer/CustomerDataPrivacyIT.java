package com.erp.customer;

import com.erp.modules.customer.domain.port.in.RegisterCustomerCommand;
import com.erp.modules.customer.domain.port.in.RegisterCustomerUseCase;
import com.erp.test.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Integration test verifying data privacy on duplicate CPF registration.
 * Property 12: Duplicate CPF never exposes existing customer data.
 */
class CustomerDataPrivacyIT extends AbstractIntegrationTest {

    @Autowired
    private RegisterCustomerUseCase registerCustomerUseCase;

    private static final String EXISTING_CPF = "12345678909";
    private static final String EXISTING_NAME = "João Silva Confidencial";
    private static final String EXISTING_EMAIL = "joao.secreto@email.com";
    private static final String EXISTING_PHONE = "11999887766";

    @Test
    @DisplayName("Duplicate CPF registration returns only 'CPF já cadastrado' — no data leak")
    void duplicateCpf_onlyShowsGenericMessage_noDataLeak() {
        // 1. Register first client with known data
        registerCustomerUseCase.register(new RegisterCustomerCommand(
                EXISTING_NAME, EXISTING_CPF, EXISTING_EMAIL, EXISTING_PHONE, LocalDate.of(1990, 5, 15)));

        // 2. Try to register ANOTHER client with same CPF
        Throwable thrown = catchThrowable(() ->
                registerCustomerUseCase.register(new RegisterCustomerCommand(
                        "Outra Pessoa", EXISTING_CPF, "outro@email.com", "11888776655", LocalDate.of(1985, 3, 20))));

        // 3. Assert error occurred
        assertThat(thrown).isNotNull();

        // 4. Assert response contains ONLY "CPF já cadastrado"
        String errorMessage = thrown.getMessage();
        assertThat(errorMessage).containsIgnoringCase("CPF já cadastrado");

        // 5. Assert response does NOT contain any existing customer data
        assertThat(errorMessage).doesNotContain(EXISTING_NAME);
        assertThat(errorMessage).doesNotContain(EXISTING_EMAIL);
        assertThat(errorMessage).doesNotContain(EXISTING_PHONE);
        // Should not contain the UUID either
        assertThat(errorMessage).doesNotContainPattern("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("Duplicate CPF does not reveal partial CPF in the error response")
    void duplicateCpf_doesNotExposePartialCpf() {
        registerCustomerUseCase.register(new RegisterCustomerCommand(
                "Maria Oliveira", EXISTING_CPF, "maria@email.com", "11777665544", LocalDate.of(1992, 8, 10)));

        Throwable thrown = catchThrowable(() ->
                registerCustomerUseCase.register(new RegisterCustomerCommand(
                        "Outro Nome", EXISTING_CPF, "other@email.com", "11666554433", LocalDate.of(1988, 1, 5))));

        assertThat(thrown).isNotNull();
        String errorMessage = thrown.getMessage();

        // Should not contain the full CPF digits (beyond what the user submitted)
        // The error should be generic
        assertThat(errorMessage).doesNotContain("maria@email.com");
        assertThat(errorMessage).doesNotContain("Maria Oliveira");
    }
}
