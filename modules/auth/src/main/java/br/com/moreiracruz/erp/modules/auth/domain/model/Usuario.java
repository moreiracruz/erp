package br.com.moreiracruz.erp.modules.auth.domain.model;

import br.com.moreiracruz.erp.shared.kernel.AggregateRoot;
import br.com.moreiracruz.erp.shared.exceptions.ValidationException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Aggregate root representing a system user.
 *
 * <p>Encapsulates brute-force lockout logic:
 * <ul>
 *   <li>After 5 consecutive failed attempts the account is locked for 15 minutes.</li>
 *   <li>A successful login resets the counter and clears the lock.</li>
 * </ul>
 *
 * <p>Use the static factory {@link #create(String, String, Role)} to construct
 * new instances; do not call the no-arg constructor directly in application code.
 */
public class Usuario extends AggregateRoot {

    /** Lock window applied after reaching the failure threshold. */
    private static final int LOCK_THRESHOLD = 5;

    /** Duration of the lockout period in minutes. */
    private static final int LOCK_DURATION_MINUTES = 15;

    private String username;
    private String passwordHash;
    private Role role;
    private boolean active;
    private UsuarioStatus status;
    private int failedAttempts;
    private Instant lockedUntil;
    private Instant createdAt;

    /** Required by JPA — not for direct use in application code. */
    protected Usuario() {}

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    /**
     * Creates a new active {@code Usuario} with zero failed attempts.
     *
     * @param username     login name / e-mail (must be unique in the database)
     * @param passwordHash bcrypt hash of the raw password
     * @param role         assigned role
     * @return a ready-to-persist {@code Usuario}
     */
    public static Usuario create(String username, String passwordHash, Role role) {
        validateUsername(username);
        validatePasswordHash(passwordHash);
        if (role == null) {
            throw new ValidationException("Perfil do usuário é obrigatório");
        }
        Usuario u = new Usuario();
        u.uuid = UUID.randomUUID();
        u.username = username.trim().toLowerCase();
        u.passwordHash = passwordHash;
        u.role = role;
        u.active = true;
        u.status = UsuarioStatus.ACTIVE;
        u.failedAttempts = 0;
        u.lockedUntil = null;
        u.createdAt = Instant.now();
        return u;
    }

    public static Usuario createPendingActivation(String username, String unusablePasswordHash, Role role) {
        validateUsername(username);
        validatePasswordHash(unusablePasswordHash);
        if (role == null) {
            throw new ValidationException("Perfil do usuário é obrigatório");
        }
        Usuario u = new Usuario();
        u.uuid = UUID.randomUUID();
        u.username = username.trim().toLowerCase();
        u.passwordHash = unusablePasswordHash;
        u.role = role;
        u.active = false;
        u.status = UsuarioStatus.PENDING_ACTIVATION;
        u.failedAttempts = 0;
        u.lockedUntil = null;
        u.createdAt = Instant.now();
        return u;
    }

    /**
     * Reconstructs a {@code Usuario} from persisted state (used by repository adapters).
     * Not intended for use in domain logic — prefer {@link #create} for new instances.
     *
     * @param id             surrogate PK
     * @param uuid           public UUID
     * @param username       login name
     * @param passwordHash   stored bcrypt hash
     * @param role           assigned role
     * @param active         account active flag
     * @param failedAttempts current failed-attempt counter
     * @param lockedUntil    lock expiry instant (nullable)
     * @param createdAt      creation timestamp
     * @return a fully populated {@code Usuario}
     */
    public static Usuario reconstruct(Long id, UUID uuid, String username, String passwordHash,
                                      Role role, boolean active, UsuarioStatus status, int failedAttempts,
                                      Instant lockedUntil, Instant createdAt) {
        Usuario u = new Usuario();
        u.id = id;
        u.uuid = uuid;
        u.username = username;
        u.passwordHash = passwordHash;
        u.role = role;
        u.active = active;
        u.status = status != null ? status : (active ? UsuarioStatus.ACTIVE : UsuarioStatus.INACTIVE);
        u.failedAttempts = failedAttempts;
        u.lockedUntil = lockedUntil;
        u.createdAt = createdAt;
        return u;
    }

    // -------------------------------------------------------------------------
    // Domain behaviour
    // -------------------------------------------------------------------------

    /**
     * Records one failed authentication attempt.
     *
     * <p>If the accumulated count reaches {@value #LOCK_THRESHOLD}, the account
     * is locked for {@value #LOCK_DURATION_MINUTES} minutes from the current
     * instant.
     */
    public void recordFailedAttempt() {
        failedAttempts++;
        if (failedAttempts >= LOCK_THRESHOLD) {
            lockedUntil = Instant.now().plus(LOCK_DURATION_MINUTES, ChronoUnit.MINUTES);
        }
    }

    /**
     * Resets the failed-attempt counter and removes any active lock.
     * Called after a successful login.
     */
    public void resetAttempts() {
        failedAttempts = 0;
        lockedUntil = null;
    }

    public void changeRole(Role role) {
        if (role == null) {
            throw new ValidationException("Perfil do usuário é obrigatório");
        }
        this.role = role;
    }

    public void changePasswordHash(String passwordHash) {
        validatePasswordHash(passwordHash);
        this.passwordHash = passwordHash;
        resetAttempts();
    }

    public void activateWithPasswordHash(String passwordHash) {
        changePasswordHash(passwordHash);
        this.active = true;
        this.status = UsuarioStatus.ACTIVE;
    }

    public void resetLockout() {
        resetAttempts();
    }

    /**
     * Returns {@code true} if the account is currently locked (i.e., the lock
     * instant is set and has not yet passed).
     */
    public boolean isLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active && status == UsuarioStatus.ACTIVE;
    }

    public UsuarioStatus getStatus() {
        return status;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.status = active ? UsuarioStatus.ACTIVE : UsuarioStatus.INACTIVE;
    }

    private static void validateUsername(String username) {
        if (username == null || username.isBlank() || username.length() > 255) {
            throw new ValidationException("Usuário deve ter entre 1 e 255 caracteres");
        }
    }

    private static void validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new ValidationException("Senha é obrigatória");
        }
    }
}
