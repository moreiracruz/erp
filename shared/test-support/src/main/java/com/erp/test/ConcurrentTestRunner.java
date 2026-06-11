package com.erp.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Utility for executing concurrent operations and collecting results.
 * Uses a CountDownLatch to maximize contention by releasing all threads simultaneously.
 */
public class ConcurrentTestRunner {

    /**
     * Execute a task concurrently across N threads. All threads start simultaneously
     * using a CountDownLatch to maximize contention.
     *
     * @param threadCount number of concurrent threads
     * @param task        the operation to execute (returns true for success, false for failure)
     * @return aggregated result with success/failure counts and any exceptions
     */
    public static ConcurrentTestResult run(int threadCount, Supplier<Boolean> task) {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait until all threads are ready
                    Boolean result = task.get();
                    results.add(result);
                } catch (Exception e) {
                    errors.add(e);
                    results.add(false);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Release all threads simultaneously
        try {
            boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
            if (!completed) {
                throw new AssertionError("Concurrent test did not complete within 30 seconds");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Concurrent test interrupted", e);
        } finally {
            executor.shutdownNow();
        }

        long successCount = results.stream().filter(Boolean::booleanValue).count();
        long failureCount = results.stream().filter(r -> !r).count();
        return new ConcurrentTestResult(successCount, failureCount, errors);
    }

    /**
     * Overload: execute a Runnable task (exceptions count as failures).
     */
    public static ConcurrentTestResult run(int threadCount, Runnable task) {
        return run(threadCount, () -> {
            task.run();
            return true;
        });
    }
}
