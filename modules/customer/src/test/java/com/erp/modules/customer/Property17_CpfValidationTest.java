package com.erp.modules.customer;

import com.erp.modules.customer.domain.model.Cpf;
import com.erp.shared.exceptions.ValidationException;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates: Requirements 1.2
 */
class Property17_CpfValidationTest {

    @Property(tries = 1000)
    @Label("Feature: erp-loja-roupas, Property 17: CPF validation is deterministic and algorithm-correct")
    void cpfValidationIsDeterministic(@ForAll("elevenDigitStrings") String digits) {
        boolean result1 = isValidCpf(digits);
        boolean result2 = isValidCpf(digits);
        assertThat(result1).isEqualTo(result2); // pure function, same input → same output

        // Cross-check with reference implementation
        assertThat(result1).isEqualTo(referenceCpfCheck(digits));
    }

    private boolean isValidCpf(String digits) {
        try {
            new Cpf(digits);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    private boolean referenceCpfCheck(String cpf) {
        if (cpf.length() != 11) return false;
        if (cpf.chars().distinct().count() == 1) return false; // all same digit
        int[] d = cpf.chars().map(c -> c - '0').toArray();
        int sum1 = 0;
        for (int i = 0; i < 9; i++) sum1 += d[i] * (10 - i);
        int r1 = 11 - (sum1 % 11);
        if (r1 >= 10) r1 = 0;
        if (d[9] != r1) return false;
        int sum2 = 0;
        for (int i = 0; i < 10; i++) sum2 += d[i] * (11 - i);
        int r2 = 11 - (sum2 % 11);
        if (r2 >= 10) r2 = 0;
        return d[10] == r2;
    }

    @Provide
    Arbitrary<String> elevenDigitStrings() {
        return Arbitraries.strings().numeric().ofLength(11);
    }
}
