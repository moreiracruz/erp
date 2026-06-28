package br.com.moreiracruz.erp.modules.consignment.domain.port.in;

import java.util.UUID;

public record OpenContractCommand(UUID consignorUuid, String code) {}
