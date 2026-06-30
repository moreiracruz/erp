package br.com.moreiracruz.erp.modules.consignment.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import br.com.moreiracruz.erp.shared.kernel.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class ItemConsignacaoEnvio extends AggregateRoot {

    private UUID contratoUuid;
    private UUID varianteUuid;
    private int quantity;
    private int availableQuantity;
    private int soldQuantity;
    private int settledQuantity;
    private int returnedQuantity;
    private ItemConsignacaoEnvioStatus status;
    private Instant sentAt;
    private Instant returnedAt;

    private ItemConsignacaoEnvio() {}

    public static ItemConsignacaoEnvio send(UUID contratoUuid, UUID varianteUuid, int quantity) {
        validateQuantity(quantity);
        if (contratoUuid == null || varianteUuid == null) {
            throw new ValidationException("Contrato e variante são obrigatórios");
        }
        ItemConsignacaoEnvio item = new ItemConsignacaoEnvio();
        item.uuid = UUID.randomUUID();
        item.contratoUuid = contratoUuid;
        item.varianteUuid = varianteUuid;
        item.quantity = quantity;
        item.availableQuantity = quantity;
        item.status = ItemConsignacaoEnvioStatus.ENVIADO;
        item.sentAt = Instant.now();
        return item;
    }

    public static ItemConsignacaoEnvio restore(UUID uuid, UUID contratoUuid, UUID varianteUuid, int quantity,
                                               int availableQuantity, int soldQuantity, int settledQuantity,
                                               int returnedQuantity, ItemConsignacaoEnvioStatus status,
                                               Instant sentAt, Instant returnedAt) {
        ItemConsignacaoEnvio item = new ItemConsignacaoEnvio();
        item.uuid = uuid;
        item.contratoUuid = contratoUuid;
        item.varianteUuid = varianteUuid;
        item.quantity = quantity;
        item.availableQuantity = availableQuantity;
        item.soldQuantity = soldQuantity;
        item.settledQuantity = settledQuantity;
        item.returnedQuantity = returnedQuantity;
        item.status = status;
        item.sentAt = sentAt;
        item.returnedAt = returnedAt;
        return item;
    }

    public void markSold(int quantitySold) {
        validateQuantity(quantitySold);
        if (quantitySold > availableQuantity) {
            throw new ValidationException("Venda informada excede saldo em consignação");
        }
        availableQuantity -= quantitySold;
        soldQuantity += quantitySold;
        if (availableQuantity == 0) {
            status = ItemConsignacaoEnvioStatus.VENDIDO;
        }
    }

    public void returnQuantity(int quantityToReturn) {
        validateQuantity(quantityToReturn);
        if (quantityToReturn > availableQuantity) {
            throw new ValidationException("Não é possível devolver mais que o saldo no consignatário");
        }
        availableQuantity -= quantityToReturn;
        returnedQuantity += quantityToReturn;
        returnedAt = Instant.now();
        if (availableQuantity == 0) {
            status = soldQuantity > settledQuantity
                    ? ItemConsignacaoEnvioStatus.VENDIDO
                    : ItemConsignacaoEnvioStatus.DEVOLVIDO;
        }
    }

    public void settle(int quantityToSettle) {
        validateQuantity(quantityToSettle);
        if (quantityToSettle > getSoldAvailableToSettle()) {
            throw new ValidationException("Acerto excede quantidade vendida ainda não acertada");
        }
        settledQuantity += quantityToSettle;
        if (soldQuantity == settledQuantity && availableQuantity == 0) {
            status = ItemConsignacaoEnvioStatus.ACERTADO;
        }
    }

    public int getSoldAvailableToSettle() {
        return soldQuantity - settledQuantity;
    }

    public boolean hasPendingQuantity() {
        return availableQuantity > 0 || getSoldAvailableToSettle() > 0;
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new ValidationException("Quantidade deve ser positiva");
        }
    }

    public UUID getContratoUuid() { return contratoUuid; }
    public UUID getVarianteUuid() { return varianteUuid; }
    public int getQuantity() { return quantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public int getSoldQuantity() { return soldQuantity; }
    public int getSettledQuantity() { return settledQuantity; }
    public int getReturnedQuantity() { return returnedQuantity; }
    public ItemConsignacaoEnvioStatus getStatus() { return status; }
    public Instant getSentAt() { return sentAt; }
    public Instant getReturnedAt() { return returnedAt; }
}
