package br.com.moreiracruz.erp.modules.consignment.application.usecase;

import br.com.moreiracruz.erp.modules.consignment.domain.model.AcertoConsignacao;
import br.com.moreiracruz.erp.modules.consignment.domain.model.Consignante;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ContratoConsignacao;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ContratoConsignacaoStatus;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ItemConsignado;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ConsignanteCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ConsignanteResponse;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ContractResponse;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ItemResponse;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.OpenContractCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ReceiveItemsCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ReturnItemsCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.SettleConsignmentCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.SettlementResponse;
import br.com.moreiracruz.erp.modules.consignment.domain.port.out.ConsignmentRepository;
import br.com.moreiracruz.erp.shared.exceptions.NotFoundException;
import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import br.com.moreiracruz.erp.shared.kernel.FinancePort;
import br.com.moreiracruz.erp.shared.kernel.InventoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ConsignmentService {

    private final ConsignmentRepository repository;
    private final InventoryPort inventoryPort;
    private final FinancePort financePort;

    public ConsignmentService(ConsignmentRepository repository,
                              InventoryPort inventoryPort,
                              FinancePort financePort) {
        this.repository = repository;
        this.inventoryPort = inventoryPort;
        this.financePort = financePort;
    }

    public ConsignanteResponse createConsignante(ConsignanteCommand command) {
        return toResponse(repository.saveConsignante(
                Consignante.create(command.name(), command.document(), command.email(), command.phone())));
    }

    public List<ConsignanteResponse> listConsignantes() {
        return repository.findAllConsignantes().stream().map(this::toResponse).toList();
    }

    public ConsignanteResponse getConsignante(UUID uuid) {
        return toResponse(findConsignante(uuid));
    }

    public ConsignanteResponse updateConsignante(UUID uuid, ConsignanteCommand command) {
        Consignante consignante = findConsignante(uuid);
        consignante.update(command.name(), command.document(), command.email(), command.phone());
        return toResponse(repository.saveConsignante(consignante));
    }

    public void deactivateConsignante(UUID uuid) {
        Consignante consignante = findConsignante(uuid);
        consignante.deactivate();
        repository.saveConsignante(consignante);
    }

    public ContractResponse openContract(OpenContractCommand command) {
        Consignante consignante = findConsignante(command.consignorUuid());
        if (!consignante.isActive()) {
            throw new ValidationException("Consignante inativo");
        }
        ContratoConsignacao contrato = repository.saveContrato(
                ContratoConsignacao.open(command.consignorUuid(), command.code()));
        return toResponse(contrato);
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> listContracts(String status, UUID consignorUuid) {
        ContratoConsignacaoStatus parsedStatus = status == null || status.isBlank()
                ? null
                : ContratoConsignacaoStatus.valueOf(status);
        return repository.findContratos(parsedStatus, consignorUuid).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContractResponse getContract(UUID uuid) {
        return toResponse(findContrato(uuid));
    }

    public ContractResponse receiveItems(UUID contratoUuid, ReceiveItemsCommand command) {
        ContratoConsignacao contrato = findContrato(contratoUuid);
        contrato.ensureOpen();
        validateLines(command.items());
        for (ReceiveItemsCommand.ItemLine line : command.items()) {
            ItemConsignado item = repository.saveItem(
                    ItemConsignado.receive(contratoUuid, line.varianteUuid(), line.quantity()));
            inventoryPort.registerEntry(item.getVarianteUuid(), item.getQuantity(), command.actorUuid(), contratoUuid);
        }
        return toResponse(contrato);
    }

    public ContractResponse returnItems(UUID contratoUuid, ReturnItemsCommand command) {
        ContratoConsignacao contrato = findContrato(contratoUuid);
        contrato.ensureOpen();
        validateLines(command.items());
        for (ReturnItemsCommand.ItemLine line : command.items()) {
            ItemConsignado item = findItemInContract(contratoUuid, line.itemUuid());
            item.returnQuantity(line.quantity());
            repository.saveItem(item);
            inventoryPort.registerWithdrawal(item.getVarianteUuid(), line.quantity(), command.actorUuid(), contratoUuid);
        }
        return toResponse(contrato);
    }

    public SettlementResponse settle(UUID contratoUuid, SettleConsignmentCommand command) {
        ContratoConsignacao contrato = findContrato(contratoUuid);
        contrato.ensureOpen();
        validateLines(command.items());
        List<AcertoConsignacao.AcertoItem> settlementItems = command.items().stream()
                .map(line -> new AcertoConsignacao.AcertoItem(line.itemUuid(), line.quantity(), line.manualAmount()))
                .toList();
        for (SettleConsignmentCommand.ItemLine line : command.items()) {
            ItemConsignado item = findItemInContract(contratoUuid, line.itemUuid());
            item.settle(line.quantity());
            repository.saveItem(item);
        }
        AcertoConsignacao acerto = repository.saveAcerto(
                AcertoConsignacao.create(contratoUuid, command.responsibleUuid(), command.notes(), settlementItems));
        contrato.markPartiallySettled();
        repository.saveContrato(contrato);
        financePort.registerSupplierExpense(
                acerto.getTotalAmount(),
                "Acerto consignação " + contrato.getCode(),
                command.responsibleUuid(),
                acerto.getUuid());
        return new SettlementResponse(acerto.getUuid(), acerto.getContratoUuid(),
                acerto.getTotalAmount(), acerto.getNotes(), acerto.getCreatedAt());
    }

    public ContractResponse close(UUID contratoUuid) {
        ContratoConsignacao contrato = findContrato(contratoUuid);
        boolean hasPending = repository.findItemsByContratoUuid(contratoUuid).stream()
                .anyMatch(ItemConsignado::hasPendingQuantity);
        if (hasPending) {
            throw new ValidationException("Contrato possui itens pendentes");
        }
        contrato.close();
        return toResponse(repository.saveContrato(contrato));
    }

    public void markSaleCompleted(UUID saleUuid, List<br.com.moreiracruz.erp.shared.events.SaleCompletedPayload.SaleItem> items) {
        for (var saleItem : items) {
            int remaining = saleItem.quantity();
            for (ItemConsignado item : repository.findSellableItemsByVarianteUuid(saleItem.varianteUuid())) {
                if (remaining <= 0) break;
                int sold = item.markSold(remaining, saleUuid);
                remaining -= sold;
                repository.saveItem(item);
            }
        }
    }

    private Consignante findConsignante(UUID uuid) {
        return repository.findConsignanteByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Consignante não encontrado: " + uuid));
    }

    private ContratoConsignacao findContrato(UUID uuid) {
        return repository.findContratoByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Contrato de consignação não encontrado: " + uuid));
    }

    private ItemConsignado findItemInContract(UUID contratoUuid, UUID itemUuid) {
        ItemConsignado item = repository.findItemByUuid(itemUuid)
                .orElseThrow(() -> new NotFoundException("Item consignado não encontrado: " + itemUuid));
        if (!contratoUuid.equals(item.getContratoUuid())) {
            throw new ValidationException("Item não pertence ao contrato informado");
        }
        return item;
    }

    private void validateLines(List<?> items) {
        if (items == null || items.isEmpty()) {
            throw new ValidationException("Informe pelo menos um item");
        }
    }

    private ContractResponse toResponse(ContratoConsignacao contrato) {
        return new ContractResponse(
                contrato.getUuid(),
                contrato.getConsignanteUuid(),
                contrato.getCode(),
                contrato.getStatus().name(),
                contrato.getOpenedAt(),
                contrato.getClosedAt(),
                repository.findItemsByContratoUuid(contrato.getUuid()).stream().map(this::toResponse).toList());
    }

    private ConsignanteResponse toResponse(Consignante consignante) {
        return new ConsignanteResponse(consignante.getUuid(), consignante.getName(), consignante.getDocument(),
                consignante.getEmail(), consignante.getPhone(), consignante.isActive(), consignante.getCreatedAt());
    }

    private ItemResponse toResponse(ItemConsignado item) {
        return new ItemResponse(item.getUuid(), item.getContratoUuid(), item.getVarianteUuid(),
                item.getQuantity(), item.getRemainingQuantity(), item.getSoldQuantity(), item.getSettledQuantity(),
                item.getReturnedQuantity(), item.getStatus().name(), item.getReceivedAt(), item.getSoldSaleUuid());
    }
}
