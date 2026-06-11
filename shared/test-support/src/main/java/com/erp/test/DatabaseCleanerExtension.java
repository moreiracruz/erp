package com.erp.test;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * JUnit 5 Extension that invokes {@link DatabaseCleaner} after each test method.
 * Skips cleanup for {@code @Transactional} tests where Spring manages rollback.
 */
public class DatabaseCleanerExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) {
        if (isTransactional(context)) {
            return;
        }

        var applicationContext = SpringExtension.getApplicationContext(context);
        var cleaner = applicationContext.getBean(DatabaseCleaner.class);
        cleaner.clean();
    }

    private boolean isTransactional(ExtensionContext context) {
        return context.getRequiredTestMethod().isAnnotationPresent(Transactional.class)
                || context.getRequiredTestClass().isAnnotationPresent(Transactional.class);
    }
}
