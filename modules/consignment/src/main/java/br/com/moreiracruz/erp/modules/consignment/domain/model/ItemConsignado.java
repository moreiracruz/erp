package br.com.moreiracruz.erp.modules.consignment.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import br.com.moreiracruz.erp.shared.kernel.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class ItemConsignado extends AggregateRoot {

    private UUID contratoUuid;
    private UUID varianteUuid;
    private int quantity;
    private int remainingQuantity;
    private int soldQuantity;
    private int settledQuantity;
    private int returnedQuantity;
    private ItemConsignadoStatus status;
    private Instant receivedAt;
    private UUID soldSaleUuid;
    private Instant returnedAt;

    private ItemConsignado() {}

    public static ItemConsignado receive(UUID contratoUuid, UUID varianteUuid, int quantity) {
        validateQuantity(quantity);
        if (contratoUuid == null || varianteUuid == null) {
            throw new ValidationException("Contrato e variante são obrigatórios");
        }
        ItemConsignado item = new ItemConsignado();
        item.uuid = UUID.randomUUID();
        item.contratoUuid = contratoUuid;
        item.varianteUuid = varianteUuid;
        item.quantity = quantity;
        item.remainingQuantity = quantity;
        item.status = ItemConsignadoStatus.RECEBIDO;
        item.receivedAt = Instant.now();
        return item;
    }

    public static ItemConsignado restore(UUID uuid, UUID contratoUuid, UUID varianteUuid, int quantity,
                                         int remainingQuantity, int soldQuantity, int settledQuantity,
                                         int returnedQuantity, ItemConsignadoStatus status, Instant receivedAt,
                                         UUID soldSaleUuid, Instant returnedAt) {
        ItemConsignado item = new ItemConsignado();
        item.uuid = uuid;
        item.contratoUuid = contratoUuid;
        item.varianteUuid = varianteUuid;
        item.quantity = quantity;
        item.remainingQuantity = remainingQuantity;
        item.soldQuantity = soldQuantity;
        item.settledQuantity = settledQuantity;
        item.returnedQuantity = returnedQuantity;
        item.status = status;
        item.receivedAt = receivedAt;
        item.soldSaleUuid = soldSaleUuid;
        item.returnedAt = returnedAt;
        return item;
    }

    public int markSold(int requestedQuantity, UUID saleUuid) {
        validateQuantity(requestedQuantity);
        if (saleUuid == null) {
            throw new ValidationException("Venda é obrigatória");
        }
        int soldNow = Math.min(requestedQuantity, remainingQuantity);
        if (soldNow == 0) return 0;
        remainingQuantity -= soldNow;
        soldQuantity += soldNow;
        soldSaleUuid = saleUuid;
        if (remainingQuantity == 0) {
            status = ItemConsignadoStatus.VENDIDO;
        }
        return soldNow;
    }

    public void returnQuantity(int quantityToReturn) {
        validateQuantity(quantityToReturn);
        if (quantityToReturn > remainingQuantity) {
            throw new ValidationException("Não é possível devolver mais que o saldo pendente");
        }
        remainingQuantity -= quantityToReturn;
        returnedQuantity += quantityToReturn;
        returnedAt = Instant.now();
        if (remainingQuantity == 0) {
            status = soldQuantity > settledQuantity
                    ? ItemConsignadoStatus.VENDIDO
                    : ItemConsignadoStatus.DEVOLVIDO;
        }
    }

    public void settle(int quantityToSettle) {
        validateQuantity(quantityToSettle);
        if (quantityToSettle > getSoldAvailableToSettle()) {
            throw new ValidationException("Acerto excede quantidade vendida ainda não acertada");
        }
        settledQuantity += quantityToSettle;
        if (soldQuantity == settledQuantity && remainingQuantity == 0) {
            status = ItemConsignadoStatus.ACERTADO;
        }
    }

    public int getSoldAvailableToSettle() {
        return soldQuantity - settledQuantity;
    }

    public boolean hasPendingQuantity() {
        return remainingQuantity > 0 || getSoldAvailableToSettle() > 0;
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new ValidationException("Quantidade deve ser positiva");
        }
    }

    public UUID getContratoUuid() { return contratoUuid; }
    public UUID getVarianteUuid() { return varianteUuid; }
    public int getQuantity() { return quantity; }
    public int getRemainingQuantity() { return remainingQuantity; }
    public int getSoldQuantity() { return soldQuantity; }
    public int getSettledQuantity() { return settledQuantity; }
    public int getReturnedQuantity() { return returnedQuantity; }
    public ItemConsignadoStatus getStatus() { return status; }
    public Instant getReceivedAt() { return receivedAt; }
    public UUID getSoldSaleUuid() { return soldSaleUuid; }
    public Instant getReturnedAt() { return returnedAt; }
}
