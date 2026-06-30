package br.com.moreiracruz.erp.modules.sales.application.usecase;

import br.com.moreiracruz.erp.modules.sales.domain.model.ItemVenda;
import br.com.moreiracruz.erp.modules.sales.domain.model.Venda;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.ItemVendaResponse;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.VendaResponse;

import java.util.List;

/**
 * Maps domain Venda/ItemVenda to their response DTOs.
 */
final class VendaResponseMapper {

    private VendaResponseMapper() {}

    static VendaResponse toResponse(Venda venda) {
        List<ItemVendaResponse> items = venda.getItems().stream()
                .map(VendaResponseMapper::toItemResponse)
                .toList();

        return new VendaResponse(
                venda.getUuid(),
                venda.getOperatorUuid(),
                venda.getTerminalId(),
                venda.getClienteUuid(),
                venda.getStatus().name(),
                venda.getPaymentMethod() != null ? venda.getPaymentMethod().name() : null,
                venda.getSubtotal(),
                venda.getDiscountAmount(),
                venda.getTaxAmount(),
                venda.getTotal(),
                venda.getChangeAmount(),
                items,
                venda.getCreatedAt()
        );
    }

    static ItemVendaResponse toItemResponse(ItemVenda item) {
        return new ItemVendaResponse(
                item.getVarianteUuid(),
                item.getSku(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }
}
