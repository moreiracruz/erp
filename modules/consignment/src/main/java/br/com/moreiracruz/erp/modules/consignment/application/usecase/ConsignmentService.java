package br.com.moreiracruz.erp.modules.consignment.application.usecase;

import br.com.moreiracruz.erp.modules.consignment.domain.model.AcertoConsignacao;
import br.com.moreiracruz.erp.modules.consignment.domain.model.AcertoConsignacaoEnvio;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ConsignatarioEnvio;
import br.com.moreiracruz.erp.modules.consignment.domain.model.Consignante;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ContratoConsignacao;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ContratoConsignacaoEnvio;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ContratoConsignacaoStatus;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ItemConsignacaoEnvio;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ItemConsignado;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ConsigneeCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ConsigneeResponse;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ConsignanteCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ConsignanteResponse;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ContractResponse;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ItemResponse;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.OpenContractCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.OpenSentContractCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ReceiveItemsCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ReportSentSalesCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ReturnItemsCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.ReturnSentItemsCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.SendItemsCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.SentContractResponse;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.SentItemResponse;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.SentSettlementResponse;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.SettleConsignmentCommand;
import br.com.moreiracruz.erp.modules.consignment.domain.port.in.SettleSentConsignmentCommand;
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

    public ConsigneeResponse createConsignee(ConsigneeCommand command) {
        return toResponse(repository.saveConsignatarioEnvio(
                ConsignatarioEnvio.create(command.name(), command.document(), command.email(), command.phone())));
    }

    @Transactional(readOnly = true)
    public List<ConsigneeResponse> listConsignees() {
        return repository.findAllConsignatariosEnvio().stream().map(this::toResponse).toList();
    }

    public ConsigneeResponse updateConsignee(UUID uuid, ConsigneeCommand command) {
        ConsignatarioEnvio consignee = findConsignee(uuid);
        consignee.update(command.name(), command.document(), command.email(), command.phone());
        return toResponse(repository.saveConsignatarioEnvio(consignee));
    }

    public void deactivateConsignee(UUID uuid) {
        ConsignatarioEnvio consignee = findConsignee(uuid);
        consignee.deactivate();
        repository.saveConsignatarioEnvio(consignee);
    }

    public SentContractResponse openSentContract(OpenSentContractCommand command) {
        ConsignatarioEnvio consignee = findConsignee(command.consigneeUuid());
        if (!consignee.isActive()) {
            throw new ValidationException("Consignatário inativo");
        }
        ContratoConsignacaoEnvio contract = repository.saveContratoEnvio(
                ContratoConsignacaoEnvio.open(command.consigneeUuid(), command.code()));
        return toResponse(contract);
    }

    @Transactional(readOnly = true)
    public List<SentContractResponse> listSentContracts(String status, UUID consigneeUuid) {
        ContratoConsignacaoStatus parsedStatus = status == null || status.isBlank()
                ? null
                : ContratoConsignacaoStatus.valueOf(status);
        return repository.findContratosEnvio(parsedStatus, consigneeUuid).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SentContractResponse getSentContract(UUID uuid) {
        return toResponse(findSentContract(uuid));
    }

    public SentContractResponse sendItems(UUID contratoUuid, SendItemsCommand command) {
        ContratoConsignacaoEnvio contract = findSentContract(contratoUuid);
        contract.ensureOpen();
        validateLines(command.items());
        for (SendItemsCommand.ItemLine line : command.items()) {
            ItemConsignacaoEnvio item = repository.saveItemEnvio(
                    ItemConsignacaoEnvio.send(contratoUuid, line.varianteUuid(), line.quantity()));
            inventoryPort.registerWithdrawal(item.getVarianteUuid(), item.getQuantity(), command.actorUuid(), contratoUuid);
        }
        return toResponse(contract);
    }

    public SentContractResponse reportSentSales(UUID contratoUuid, ReportSentSalesCommand command) {
        ContratoConsignacaoEnvio contract = findSentContract(contratoUuid);
        contract.ensureOpen();
        validateLines(command.items());
        for (ReportSentSalesCommand.ItemLine line : command.items()) {
            ItemConsignacaoEnvio item = findSentItemInContract(contratoUuid, line.itemUuid());
            item.markSold(line.quantity());
            repository.saveItemEnvio(item);
        }
        return toResponse(contract);
    }

    public SentContractResponse returnSentItems(UUID contratoUuid, ReturnSentItemsCommand command) {
        ContratoConsignacaoEnvio contract = findSentContract(contratoUuid);
        contract.ensureOpen();
        validateLines(command.items());
        for (ReturnSentItemsCommand.ItemLine line : command.items()) {
            ItemConsignacaoEnvio item = findSentItemInContract(contratoUuid, line.itemUuid());
            item.returnQuantity(line.quantity());
            repository.saveItemEnvio(item);
            inventoryPort.registerEntry(item.getVarianteUuid(), line.quantity(), command.actorUuid(), contratoUuid);
        }
        return toResponse(contract);
    }

    public SentSettlementResponse settleSent(UUID contratoUuid, SettleSentConsignmentCommand command) {
        ContratoConsignacaoEnvio contract = findSentContract(contratoUuid);
        contract.ensureOpen();
        validateLines(command.items());
        List<AcertoConsignacaoEnvio.AcertoItem> settlementItems = command.items().stream()
                .map(line -> new AcertoConsignacaoEnvio.AcertoItem(line.itemUuid(), line.quantity(), line.manualAmount()))
                .toList();
        for (SettleSentConsignmentCommand.ItemLine line : command.items()) {
            ItemConsignacaoEnvio item = findSentItemInContract(contratoUuid, line.itemUuid());
            item.settle(line.quantity());
            repository.saveItemEnvio(item);
        }
        AcertoConsignacaoEnvio settlement = repository.saveAcertoEnvio(
                AcertoConsignacaoEnvio.create(contratoUuid, command.responsibleUuid(), command.notes(), settlementItems));
        contract.markPartiallySettled();
        repository.saveContratoEnvio(contract);
        financePort.registerConsignmentRevenue(
                settlement.getTotalAmount(),
                "Acerto consignação enviada " + contract.getCode(),
                command.responsibleUuid(),
                settlement.getUuid());
        return new SentSettlementResponse(settlement.getUuid(), settlement.getContratoUuid(),
                settlement.getTotalAmount(), settlement.getNotes(), settlement.getCreatedAt());
    }

    public SentContractResponse closeSent(UUID contratoUuid) {
        ContratoConsignacaoEnvio contract = findSentContract(contratoUuid);
        boolean hasPending = repository.findItemsEnvioByContratoUuid(contratoUuid).stream()
                .anyMatch(ItemConsignacaoEnvio::hasPendingQuantity);
        if (hasPending) {
            throw new ValidationException("Contrato possui itens pendentes");
        }
        contract.close();
        return toResponse(repository.saveContratoEnvio(contract));
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

    private ConsignatarioEnvio findConsignee(UUID uuid) {
        return repository.findConsignatarioEnvioByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Consignatário não encontrado: " + uuid));
    }

    private ContratoConsignacaoEnvio findSentContract(UUID uuid) {
        return repository.findContratoEnvioByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Contrato de consignação enviada não encontrado: " + uuid));
    }

    private ItemConsignacaoEnvio findSentItemInContract(UUID contratoUuid, UUID itemUuid) {
        ItemConsignacaoEnvio item = repository.findItemEnvioByUuid(itemUuid)
                .orElseThrow(() -> new NotFoundException("Item de consignação enviada não encontrado: " + itemUuid));
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

    private ConsigneeResponse toResponse(ConsignatarioEnvio consignee) {
        return new ConsigneeResponse(consignee.getUuid(), consignee.getName(), consignee.getDocument(),
                consignee.getEmail(), consignee.getPhone(), consignee.isActive(), consignee.getCreatedAt());
    }

    private SentContractResponse toResponse(ContratoConsignacaoEnvio contract) {
        return new SentContractResponse(
                contract.getUuid(),
                contract.getConsigneeUuid(),
                contract.getCode(),
                contract.getStatus().name(),
                contract.getOpenedAt(),
                contract.getClosedAt(),
                repository.findItemsEnvioByContratoUuid(contract.getUuid()).stream().map(this::toResponse).toList());
    }

    private SentItemResponse toResponse(ItemConsignacaoEnvio item) {
        return new SentItemResponse(item.getUuid(), item.getContratoUuid(), item.getVarianteUuid(),
                item.getQuantity(), item.getAvailableQuantity(), item.getSoldQuantity(), item.getSettledQuantity(),
                item.getReturnedQuantity(), item.getStatus().name(), item.getSentAt());
    }
}
