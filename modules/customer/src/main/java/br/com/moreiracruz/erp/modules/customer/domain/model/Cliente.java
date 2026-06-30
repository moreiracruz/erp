package br.com.moreiracruz.erp.modules.customer.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import br.com.moreiracruz.erp.shared.kernel.AggregateRoot;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate Root for the Customer module.
 */
public class Cliente extends AggregateRoot {

    private String fullName;
    private Cpf cpf;
    private Email email;
    private String phone;
    private LocalDate birthDate;
    private boolean active;
    private Instant createdAt;

    protected Cliente() {
        // JPA / restore
    }

    public static Cliente create(String fullName, String cpfValue, String emailValue,
                                  String phone, LocalDate birthDate) {
        var c = new Cliente();
        c.setFullName(fullName);
        c.cpf = new Cpf(cpfValue);
        if (emailValue != null && !emailValue.isBlank()) {
            c.email = new Email(emailValue);
        }
        c.setPhone(phone);
        c.birthDate = birthDate;
        c.active = true;
        c.createdAt = Instant.now();
        return c;
    }

    public static Cliente restore(UUID uuid, String fullName, String cpfValue, String emailValue,
                                   String phone, LocalDate birthDate, boolean active, Instant createdAt) {
        var c = new Cliente();
        c.uuid = uuid;
        c.fullName = fullName;
        c.cpf = new Cpf(cpfValue);
        if (emailValue != null && !emailValue.isBlank()) {
            c.email = new Email(emailValue);
        }
        c.phone = phone;
        c.birthDate = birthDate;
        c.active = active;
        c.createdAt = createdAt;
        return c;
    }

    public void deactivate() {
        this.active = false;
    }

    private void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank() || fullName.length() > 255) {
            throw new ValidationException("Nome completo deve ter entre 1 e 255 caracteres");
        }
        this.fullName = fullName.trim();
    }

    private void setPhone(String phone) {
        if (phone != null && !phone.isBlank()) {
            if (!phone.matches("\\d{8,15}")) {
                throw new ValidationException("Telefone deve conter entre 8 e 15 dígitos");
            }
            this.phone = phone;
        }
    }

    // Getters
    public String getFullName() { return fullName; }
    public Cpf getCpf() { return cpf; }
    public Email getEmail() { return email; }
    public String getPhone() { return phone; }
    public LocalDate getBirthDate() { return birthDate; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
