package br.com.moreiracruz.erp.modules.auth.domain.model;

/**
 * Roles supported by the RBAC system.
 *
 * <p>Every {@link Usuario} carries exactly one {@code Role}. The value stored
 * in the database (and encoded in the JWT {@code role} claim) matches the
 * enum constant name (e.g., {@code "ROLE_MANAGER"}).
 */
public enum Role {

    /** Full administrative access to all modules. */
    ROLE_MANAGER,

    /** Access to the POS / Sales module and read-only Product and Customer access. */
    ROLE_CASHIER,

    /** Write access to the Inventory module and read-only Product access. */
    ROLE_STOCK,

    /** Write access to the Finance module and read-only financial reports. */
    ROLE_FINANCE
}
