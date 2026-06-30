package br.com.moreiracruz.erp.modules.consignment.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import br.com.moreiracruz.erp.shared.kernel.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class ContratoConsignacaoEnvio extends AggregateRoot {

    private UUID consigneeUuid;
    private String code;
    private ContratoConsignacaoStatus status;
    private Instant openedAt;
    private Instant closedAt;

    private ContratoConsignacaoEnvio() {}

    public static ContratoConsignacaoEnvio open(UUID consigneeUuid, String code) {
        if (consigneeUuid == null) {
            throw new ValidationException("Consignatário é obrigatório");
        }
        if (code == null || code.isBlank() || code.length() > 50) {
            throw new ValidationException("Código do contrato deve ter entre 1 e 50 caracteres");
        }
        ContratoConsignacaoEnvio contract = new ContratoConsignacaoEnvio();
        contract.uuid = UUID.randomUUID();
        contract.consigneeUuid = consigneeUuid;
        contract.code = code.trim();
        contract.status = ContratoConsignacaoStatus.ABERTO;
        contract.openedAt = Instant.now();
        return contract;
    }

    public static ContratoConsignacaoEnvio restore(UUID uuid, UUID consigneeUuid, String code,
                                                   ContratoConsignacaoStatus status, Instant openedAt,
                                                   Instant closedAt) {
        ContratoConsignacaoEnvio contract = new ContratoConsignacaoEnvio();
        contract.uuid = uuid;
        contract.consigneeUuid = consigneeUuid;
        contract.code = code;
        contract.status = status;
        contract.openedAt = openedAt;
        contract.closedAt = closedAt;
        return contract;
    }

    public void ensureOpen() {
        if (status != ContratoConsignacaoStatus.ABERTO && status != ContratoConsignacaoStatus.PARCIALMENTE_ACERTADO) {
            throw new ValidationException("Contrato não está aberto");
        }
    }

    public void markPartiallySettled() {
        ensureOpen();
        status = ContratoConsignacaoStatus.PARCIALMENTE_ACERTADO;
    }

    public void close() {
        ensureOpen();
        status = ContratoConsignacaoStatus.ENCERRADO;
        closedAt = Instant.now();
    }

    public UUID getConsigneeUuid() { return consigneeUuid; }
    public String getCode() { return code; }
    public ContratoConsignacaoStatus getStatus() { return status; }
    public Instant getOpenedAt() { return openedAt; }
    public Instant getClosedAt() { return closedAt; }
}
