package br.com.moreiracruz.erp.modules.consignment.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import br.com.moreiracruz.erp.shared.kernel.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class ConsignatarioEnvio extends AggregateRoot {

    private String name;
    private String document;
    private String email;
    private String phone;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    private ConsignatarioEnvio() {}

    public static ConsignatarioEnvio create(String name, String document, String email, String phone) {
        ConsignatarioEnvio consignee = new ConsignatarioEnvio();
        consignee.uuid = UUID.randomUUID();
        consignee.createdAt = Instant.now();
        consignee.active = true;
        consignee.update(name, document, email, phone);
        return consignee;
    }

    public static ConsignatarioEnvio restore(UUID uuid, String name, String document, String email, String phone,
                                             boolean active, Instant createdAt, Instant updatedAt) {
        ConsignatarioEnvio consignee = new ConsignatarioEnvio();
        consignee.uuid = uuid;
        consignee.name = name;
        consignee.document = document;
        consignee.email = email;
        consignee.phone = phone;
        consignee.active = active;
        consignee.createdAt = createdAt;
        consignee.updatedAt = updatedAt;
        return consignee;
    }

    public void update(String name, String document, String email, String phone) {
        if (name == null || name.isBlank() || name.length() > 255) {
            throw new ValidationException("Nome do consignatário deve ter entre 1 e 255 caracteres");
        }
        this.name = name.trim();
        this.document = blankToNull(document);
        this.email = blankToNull(email);
        this.phone = blankToNull(phone);
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        active = false;
        updatedAt = Instant.now();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public String getName() { return name; }
    public String getDocument() { return document; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
