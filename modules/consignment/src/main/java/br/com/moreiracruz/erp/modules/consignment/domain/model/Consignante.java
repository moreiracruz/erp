package br.com.moreiracruz.erp.modules.consignment.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import br.com.moreiracruz.erp.shared.kernel.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class Consignante extends AggregateRoot {

    private String name;
    private String document;
    private String email;
    private String phone;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    private Consignante() {}

    public static Consignante create(String name, String document, String email, String phone) {
        Consignante consignante = new Consignante();
        consignante.uuid = UUID.randomUUID();
        consignante.setData(name, document, email, phone);
        consignante.active = true;
        consignante.createdAt = Instant.now();
        consignante.updatedAt = consignante.createdAt;
        return consignante;
    }

    public static Consignante restore(UUID uuid, String name, String document, String email, String phone,
                                      boolean active, Instant createdAt, Instant updatedAt) {
        Consignante consignante = new Consignante();
        consignante.uuid = uuid;
        consignante.name = name;
        consignante.document = document;
        consignante.email = email;
        consignante.phone = phone;
        consignante.active = active;
        consignante.createdAt = createdAt;
        consignante.updatedAt = updatedAt;
        return consignante;
    }

    public void update(String name, String document, String email, String phone) {
        setData(name, document, email, phone);
        updatedAt = Instant.now();
    }

    public void deactivate() {
        active = false;
        updatedAt = Instant.now();
    }

    private void setData(String name, String document, String email, String phone) {
        if (name == null || name.isBlank() || name.length() > 255) {
            throw new ValidationException("Nome do consignante deve ter entre 1 e 255 caracteres");
        }
        this.name = name.trim();
        this.document = cleanOptional(document, 30, "Documento");
        this.email = cleanOptional(email, 255, "Email");
        this.phone = cleanOptional(phone, 30, "Telefone");
    }

    private String cleanOptional(String value, int maxLength, String field) {
        if (value == null || value.isBlank()) return null;
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new ValidationException(field + " excede " + maxLength + " caracteres");
        }
        return trimmed;
    }

    public String getName() { return name; }
    public String getDocument() { return document; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
