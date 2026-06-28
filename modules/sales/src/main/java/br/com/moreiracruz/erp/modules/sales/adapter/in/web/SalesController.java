package br.com.moreiracruz.erp.modules.sales.adapter.in.web;

import br.com.moreiracruz.erp.modules.sales.domain.port.out.ProductPort;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.AddItemCommand;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.AddItemUseCase;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.CancelSaleCommand;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.CancelSaleUseCase;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.FinalizationResponse;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.FinalizeSaleCommand;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.FinalizeSaleUseCase;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.OpenSaleCommand;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.OpenSaleUseCase;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.VendaResponse;
import br.com.moreiracruz.erp.modules.sales.domain.port.out.VendaRepository;
import br.com.moreiracruz.erp.shared.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales")
public class SalesController {

    private final OpenSaleUseCase openSaleUseCase;
    private final AddItemUseCase addItemUseCase;
    private final FinalizeSaleUseCase finalizeSaleUseCase;
    private final CancelSaleUseCase cancelSaleUseCase;
    private final ProductPort productPort;
    private final VendaRepository vendaRepository;

    public SalesController(OpenSaleUseCase openSaleUseCase,
                           AddItemUseCase addItemUseCase,
                           FinalizeSaleUseCase finalizeSaleUseCase,
                           CancelSaleUseCase cancelSaleUseCase,
                           ProductPort productPort,
                           VendaRepository vendaRepository) {
        this.openSaleUseCase = openSaleUseCase;
        this.addItemUseCase = addItemUseCase;
        this.finalizeSaleUseCase = finalizeSaleUseCase;
        this.cancelSaleUseCase = cancelSaleUseCase;
        this.productPort = productPort;
        this.vendaRepository = vendaRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_CASHIER') or hasAuthority('ROLE_MANAGER')")
    public VendaResponse open(@RequestBody OpenSaleRequest request, Authentication auth) {
        UUID operatorUuid = (UUID) auth.getPrincipal();
        return openSaleUseCase.open(operatorUuid,
                new OpenSaleCommand(request.terminalId(), request.clienteUuid()));
    }

    @PostMapping("/{uuid}/items")
    @PreAuthorize("hasAuthority('ROLE_CASHIER') or hasAuthority('ROLE_MANAGER')")
    public VendaResponse addItem(@PathVariable UUID uuid, @RequestBody AddItemRequest request) {
        ProductPort.VariantInfo variant = productPort.findByBarcode(request.barcode());
        AddItemCommand cmd = new AddItemCommand(
                variant.uuid(), variant.sku(), variant.price(), request.quantity());
        return addItemUseCase.addItem(uuid, cmd);
    }

    @PostMapping("/{uuid}/finalize")
    @PreAuthorize("hasAuthority('ROLE_CASHIER') or hasAuthority('ROLE_MANAGER')")
    public FinalizationResponse finalize(@PathVariable UUID uuid,
                                         @RequestBody FinalizeSaleRequest request) {
        return finalizeSaleUseCase.finalize(uuid,
                new FinalizeSaleCommand(request.paymentMethod(), request.amountPaid(),
                        request.couponCode(), request.expectedTotal()));
    }

    @PostMapping("/{uuid}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_CASHIER') or hasAuthority('ROLE_MANAGER')")
    public void cancel(@PathVariable UUID uuid, @RequestBody CancelSaleRequest request) {
        cancelSaleUseCase.cancel(uuid, new CancelSaleCommand(request.reason()));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_FINANCE')")
    public VendaResponse findByUuid(@PathVariable UUID uuid) {
        return vendaRepository.findByUuid(uuid)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Venda não encontrada: " + uuid));
    }

    private VendaResponse toResponse(br.com.moreiracruz.erp.modules.sales.domain.model.Venda venda) {
        var items = venda.getItems().stream()
                .map(item -> new br.com.moreiracruz.erp.modules.sales.domain.port.in.ItemVendaResponse(
                        item.getVarianteUuid(), item.getSku(), item.getQuantity(),
                        item.getUnitPrice(), item.getLineTotal()))
                .toList();
        return new VendaResponse(
                venda.getUuid(), venda.getOperatorUuid(), venda.getTerminalId(),
                venda.getClienteUuid(),
                venda.getStatus().name(),
                venda.getPaymentMethod() != null ? venda.getPaymentMethod().name() : null,
                venda.getSubtotal(), venda.getDiscountAmount(), venda.getTaxAmount(),
                venda.getTotal(), venda.getChangeAmount(), items, venda.getCreatedAt());
    }
}
