package com.erp.test;

/**
 * Data structure for parameterized RBAC testing.
 */
public record RbacTestCase(
        String httpMethod,
        String path,
        String role,
        int expectedStatus,
        String description
) {
    /** Factory for authorized cases. */
    public static RbacTestCase allowed(String method, String path, String role) {
        return new RbacTestCase(method, path, role, 200, role + " → " + method + " " + path);
    }

    /** Factory for forbidden cases. */
    public static RbacTestCase forbidden(String method, String path, String role) {
        return new RbacTestCase(method, path, role, 403, role + " → " + method + " " + path);
    }
}
