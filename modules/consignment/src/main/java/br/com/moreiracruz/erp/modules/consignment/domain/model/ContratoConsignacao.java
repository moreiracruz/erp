package br.com.moreiracruz.erp.modules.consignment.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import br.com.moreiracruz.erp.shared.kernel.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class ContratoConsignacao extends AggregateRoot {

    private UUID consignanteUuid;
    private String code;
    private ContratoConsignacaoStatus status;
    private Instant openedAt;
    private Instant closedAt;

    private ContratoConsignacao() {}

    public static ContratoConsignacao open(UUID consignanteUuid, String code) {
        if (consignanteUuid == null) {
            throw new ValidationException("Consignante é obrigatório");
        }
        if (code == null || code.isBlank() || code.length() > 50) {
            throw new ValidationException("Código do contrato deve ter entre 1 e 50 caracteres");
        }
        ContratoConsignacao contrato = new ContratoConsignacao();
        contrato.uuid = UUID.randomUUID();
        contrato.consignanteUuid = consignanteUuid;
        contrato.code = code.trim();
        contrato.status = ContratoConsignacaoStatus.ABERTO;
        contrato.openedAt = Instant.now();
        return contrato;
    }

    public static ContratoConsignacao restore(UUID uuid, UUID consignanteUuid, String code,
                                              ContratoConsignacaoStatus status,
                                              Instant openedAt, Instant closedAt) {
        ContratoConsignacao contrato = new ContratoConsignacao();
        contrato.uuid = uuid;
        contrato.consignanteUuid = consignanteUuid;
        contrato.code = code;
        contrato.status = status;
        contrato.openedAt = openedAt;
        contrato.closedAt = closedAt;
        return contrato;
    }

    public void ensureOpen() {
        if (status != ContratoConsignacaoStatus.ABERTO && status != ContratoConsignacaoStatus.PARCIALMENTE_ACERTADO) {
            throw new ValidationException("Contrato de consignação não está aberto");
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

    public UUID getConsignanteUuid() { return consignanteUuid; }
    public String getCode() { return code; }
    public ContratoConsignacaoStatus getStatus() { return status; }
    public Instant getOpenedAt() { return openedAt; }
    public Instant getClosedAt() { return closedAt; }
}
