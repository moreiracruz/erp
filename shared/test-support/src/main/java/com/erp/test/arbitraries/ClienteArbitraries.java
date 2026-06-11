package com.erp.test.arbitraries;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

/**
 * Jqwik Arbitrary providers for the Customer module domain objects.
 * Generates valid CPFs using the official check-digit algorithm.
 */
public class ClienteArbitraries {

    /**
     * Generates valid Brazilian CPFs (11-digit strings with valid check digits).
     * Uses the official CPF validation algorithm to compute the two check digits.
     */
    public static Arbitrary<String> validCpf() {
        return Arbitraries.integers().between(100000000, 999999999)
                .filter(n -> !allDigitsEqual(n))
                .map(ClienteArbitraries::computeFullCpf);
    }

    private static boolean allDigitsEqual(int nineDigits) {
        String s = String.valueOf(nineDigits);
        char first = s.charAt(0);
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != first) return false;
        }
        return false; // 9-digit numbers rarely have all equal digits
    }

    private static String computeFullCpf(int nineDigits) {
        String base = String.format("%09d", nineDigits);

        // Compute first check digit
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (base.charAt(i) - '0') * (10 - i);
        }
        int firstDigit = 11 - (sum % 11);
        if (firstDigit >= 10) firstDigit = 0;

        // Compute second check digit
        sum = 0;
        String withFirst = base + firstDigit;
        for (int i = 0; i < 10; i++) {
            sum += (withFirst.charAt(i) - '0') * (11 - i);
        }
        int secondDigit = 11 - (sum % 11);
        if (secondDigit >= 10) secondDigit = 0;

        return base + firstDigit + secondDigit;
    }
}
