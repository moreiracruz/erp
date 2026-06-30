package br.com.moreiracruz.erp.test;

import java.util.List;

/**
 * Result of a concurrent test execution, aggregating success/failure counts and errors.
 */
public record ConcurrentTestResult(
        long successCount,
        long failureCount,
        List<Throwable> errors
) {
    public long totalAttempts() {
        return successCount + failureCount;
    }
}
