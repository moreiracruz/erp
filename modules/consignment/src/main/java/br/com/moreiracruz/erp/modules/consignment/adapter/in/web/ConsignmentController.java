package br.com.moreiracruz.erp.modules.consignment.adapter.in.web;

import br.com.moreiracruz.erp.modules.consignment.application.usecase.ConsignmentService;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ConsignanteCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ConsignanteResponse;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ContractResponse;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.OpenContractCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ReceiveItemsCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ReturnItemsCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.SettleConsignmentCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.SettlementResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/consignments")
public class ConsignmentController {

    private final ConsignmentService service;

    public ConsignmentController(ConsignmentService service) {
        this.service = service;
    }

    @PostMapping("/consignors")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ConsignanteResponse createConsignor(@RequestBody ConsignanteCommand command) {
        return service.createConsignante(command);
    }

    @GetMapping("/consignors")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_STOCK')")
    public List<ConsignanteResponse> listConsignors() {
        return service.listConsignantes();
    }

    @GetMapping("/consignors/{uuid}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_STOCK')")
    public ConsignanteResponse getConsignor(@PathVariable UUID uuid) {
        return service.getConsignante(uuid);
    }

    @PutMapping("/consignors/{uuid}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ConsignanteResponse updateConsignor(@PathVariable UUID uuid, @RequestBody ConsignanteCommand command) {
        return service.updateConsignante(uuid, command);
    }

    @DeleteMapping("/consignors/{uuid}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public void deactivateConsignor(@PathVariable UUID uuid) {
        service.deactivateConsignante(uuid);
    }

    @PostMapping("/contracts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ContractResponse openContract(@RequestBody OpenContractCommand command) {
        return service.openContract(command);
    }

    @GetMapping("/contracts")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_STOCK')")
    public List<ContractResponse> listContracts(@RequestParam(required = false) String status,
                                                @RequestParam(required = false) UUID consignorUuid) {
        return service.listContracts(status, consignorUuid);
    }

    @GetMapping("/contracts/{uuid}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_STOCK')")
    public ContractResponse getContract(@PathVariable UUID uuid) {
        return service.getContract(uuid);
    }

    @PostMapping("/contracts/{uuid}/items")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_STOCK')")
    public ContractResponse receiveItems(@PathVariable UUID uuid,
                                         @RequestBody ReceiveItemsRequest request,
                                         Authentication authentication) {
        UUID actorUuid = (UUID) authentication.getPrincipal();
        return service.receiveItems(uuid, new ReceiveItemsCommand(actorUuid, request.items()));
    }

    @PostMapping("/contracts/{uuid}/returns")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_STOCK')")
    public ContractResponse returnItems(@PathVariable UUID uuid,
                                        @RequestBody ReturnItemsRequest request,
                                        Authentication authentication) {
        UUID actorUuid = (UUID) authentication.getPrincipal();
        return service.returnItems(uuid, new ReturnItemsCommand(actorUuid, request.items()));
    }

    @PostMapping("/contracts/{uuid}/settlements")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public SettlementResponse settle(@PathVariable UUID uuid,
                                     @RequestBody SettlementRequest request,
                                     Authentication authentication) {
        UUID responsibleUuid = (UUID) authentication.getPrincipal();
        return service.settle(uuid, new SettleConsignmentCommand(responsibleUuid, request.notes(), request.items()));
    }

    @PostMapping("/contracts/{uuid}/close")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ContractResponse close(@PathVariable UUID uuid) {
        return service.close(uuid);
    }

    public record ReceiveItemsRequest(List<ReceiveItemsCommand.ItemLine> items) {}
    public record ReturnItemsRequest(List<ReturnItemsCommand.ItemLine> items) {}
    public record SettlementRequest(String notes, List<SettleConsignmentCommand.ItemLine> items) {}
}
