package br.com.moreiracruz.erp.modules.consignment.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsignmentDomainTest {

    @Test
    void returnQuantityCannotExceedRemainingQuantity() {
        ItemConsignado item = ItemConsignado.receive(UUID.randomUUID(), UUID.randomUUID(), 2);

        assertThatThrownBy(() -> item.returnQuantity(3))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("saldo pendente");
    }

    @Test
    void settlementCannotExceedSoldNotSettledQuantity() {
        ItemConsignado item = ItemConsignado.receive(UUID.randomUUID(), UUID.randomUUID(), 3);
        item.markSold(1, UUID.randomUUID());

        assertThatThrownBy(() -> item.settle(2))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("quantidade vendida");
    }

    @Test
    void itemBecomesSettledWhenSoldQuantityIsFullySettledAndNoRemainingQuantityExists() {
        ItemConsignado item = ItemConsignado.receive(UUID.randomUUID(), UUID.randomUUID(), 2);
        item.markSold(2, UUID.randomUUID());
        item.settle(2);

        assertThat(item.getStatus()).isEqualTo(ItemConsignadoStatus.ACERTADO);
        assertThat(item.hasPendingQuantity()).isFalse();
    }

    @Test
    void itemRemainsSoldWhenReturnClearsRemainingQuantityButSoldQuantityIsNotSettled() {
        ItemConsignado item = ItemConsignado.receive(UUID.randomUUID(), UUID.randomUUID(), 2);
        item.markSold(1, UUID.randomUUID());
        item.returnQuantity(1);

        assertThat(item.getStatus()).isEqualTo(ItemConsignadoStatus.VENDIDO);
        assertThat(item.hasPendingQuantity()).isTrue();
    }

    @Test
    void sentConsignmentSaleCannotExceedAvailableQuantity() {
        ItemConsignacaoEnvio item = ItemConsignacaoEnvio.send(UUID.randomUUID(), UUID.randomUUID(), 2);

        assertThatThrownBy(() -> item.markSold(3))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("excede saldo");
    }

    @Test
    void sentConsignmentCannotClosePendingSoldQuantityAfterReturn() {
        ItemConsignacaoEnvio item = ItemConsignacaoEnvio.send(UUID.randomUUID(), UUID.randomUUID(), 2);
        item.markSold(1);
        item.returnQuantity(1);

        assertThat(item.getStatus()).isEqualTo(ItemConsignacaoEnvioStatus.VENDIDO);
        assertThat(item.hasPendingQuantity()).isTrue();
    }

    @Test
    void sentConsignmentItemBecomesSettledWhenSoldQuantityIsFullySettledAndNoAvailableQuantityExists() {
        ItemConsignacaoEnvio item = ItemConsignacaoEnvio.send(UUID.randomUUID(), UUID.randomUUID(), 2);
        item.markSold(2);
        item.settle(2);

        assertThat(item.getStatus()).isEqualTo(ItemConsignacaoEnvioStatus.ACERTADO);
        assertThat(item.hasPendingQuantity()).isFalse();
    }
}
