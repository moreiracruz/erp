package br.com.moreiracruz.erp.modules.consignment.domain.port.out;

import br.com.moreiracruz.erp.modules.consignment.domain.model.AcertoConsignacao;
import br.com.moreiracruz.erp.modules.consignment.domain.model.Consignante;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ContratoConsignacao;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ContratoConsignacaoStatus;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ItemConsignado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsignmentRepository {

    Consignante saveConsignante(Consignante consignante);

    Optional<Consignante> findConsignanteByUuid(UUID uuid);

    List<Consignante> findAllConsignantes();

    ContratoConsignacao saveContrato(ContratoConsignacao contrato);

    Optional<ContratoConsignacao> findContratoByUuid(UUID uuid);

    List<ContratoConsignacao> findContratos(ContratoConsignacaoStatus status, UUID consignanteUuid);

    ItemConsignado saveItem(ItemConsignado item);

    Optional<ItemConsignado> findItemByUuid(UUID uuid);

    List<ItemConsignado> findItemsByContratoUuid(UUID contratoUuid);

    List<ItemConsignado> findSellableItemsByVarianteUuid(UUID varianteUuid);

    AcertoConsignacao saveAcerto(AcertoConsignacao acerto);
}
