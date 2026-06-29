package br.com.moreiracruz.erp.modules.consignment.adapter.out.persistence;

import br.com.moreiracruz.erp.modules.consignment.domain.model.AcertoConsignacao;
import br.com.moreiracruz.erp.modules.consignment.domain.model.AcertoConsignacaoEnvio;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ConsignatarioEnvio;
import br.com.moreiracruz.erp.modules.consignment.domain.model.Consignante;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ContratoConsignacao;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ContratoConsignacaoEnvio;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ContratoConsignacaoStatus;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ItemConsignacaoEnvio;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ItemConsignacaoEnvioStatus;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ItemConsignado;
import br.com.moreiracruz.erp.modules.consignment.domain.model.ItemConsignadoStatus;
import br.com.moreiracruz.erp.modules.consignment.domain.port.out.ConsignmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ConsignmentRepositoryAdapter implements ConsignmentRepository {

    private final ConsignanteJpaRepository consignanteJpaRepository;
    private final ContratoConsignacaoJpaRepository contratoJpaRepository;
    private final ItemConsignadoJpaRepository itemJpaRepository;
    private final AcertoConsignacaoJpaRepository acertoJpaRepository;
    private final AcertoItemJpaRepository acertoItemJpaRepository;
    private final ConsignatarioEnvioJpaRepository consignatarioEnvioJpaRepository;
    private final ContratoConsignacaoEnvioJpaRepository contratoEnvioJpaRepository;
    private final ItemConsignacaoEnvioJpaRepository itemEnvioJpaRepository;
    private final AcertoConsignacaoEnvioJpaRepository acertoEnvioJpaRepository;
    private final AcertoItemConsignacaoEnvioJpaRepository acertoItemEnvioJpaRepository;

    public ConsignmentRepositoryAdapter(ConsignanteJpaRepository consignanteJpaRepository,
                                        ContratoConsignacaoJpaRepository contratoJpaRepository,
                                        ItemConsignadoJpaRepository itemJpaRepository,
                                        AcertoConsignacaoJpaRepository acertoJpaRepository,
                                        AcertoItemJpaRepository acertoItemJpaRepository,
                                        ConsignatarioEnvioJpaRepository consignatarioEnvioJpaRepository,
                                        ContratoConsignacaoEnvioJpaRepository contratoEnvioJpaRepository,
                                        ItemConsignacaoEnvioJpaRepository itemEnvioJpaRepository,
                                        AcertoConsignacaoEnvioJpaRepository acertoEnvioJpaRepository,
                                        AcertoItemConsignacaoEnvioJpaRepository acertoItemEnvioJpaRepository) {
        this.consignanteJpaRepository = consignanteJpaRepository;
        this.contratoJpaRepository = contratoJpaRepository;
        this.itemJpaRepository = itemJpaRepository;
        this.acertoJpaRepository = acertoJpaRepository;
        this.acertoItemJpaRepository = acertoItemJpaRepository;
        this.consignatarioEnvioJpaRepository = consignatarioEnvioJpaRepository;
        this.contratoEnvioJpaRepository = contratoEnvioJpaRepository;
        this.itemEnvioJpaRepository = itemEnvioJpaRepository;
        this.acertoEnvioJpaRepository = acertoEnvioJpaRepository;
        this.acertoItemEnvioJpaRepository = acertoItemEnvioJpaRepository;
    }

    @Override
    public Consignante saveConsignante(Consignante consignante) {
        ConsignanteJpaEntity entity = consignanteJpaRepository.findByUuid(consignante.getUuid())
                .orElseGet(ConsignanteJpaEntity::new);
        copyToEntity(consignante, entity);
        return toDomain(consignanteJpaRepository.save(entity));
    }

    @Override
    public Optional<Consignante> findConsignanteByUuid(UUID uuid) {
        return consignanteJpaRepository.findByUuid(uuid).map(this::toDomain);
    }

    @Override
    public List<Consignante> findAllConsignantes() {
        return consignanteJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public ContratoConsignacao saveContrato(ContratoConsignacao contrato) {
        ContratoConsignacaoJpaEntity entity = contratoJpaRepository.findByUuid(contrato.getUuid())
                .orElseGet(ContratoConsignacaoJpaEntity::new);
        copyToEntity(contrato, entity);
        return toDomain(contratoJpaRepository.save(entity));
    }

    @Override
    public Optional<ContratoConsignacao> findContratoByUuid(UUID uuid) {
        return contratoJpaRepository.findByUuid(uuid).map(this::toDomain);
    }

    @Override
    public List<ContratoConsignacao> findContratos(ContratoConsignacaoStatus status, UUID consignanteUuid) {
        if (status != null && consignanteUuid != null) {
            return contratoJpaRepository.findByStatusAndConsignanteUuid(status.name(), consignanteUuid).stream().map(this::toDomain).toList();
        }
        if (status != null) {
            return contratoJpaRepository.findByStatus(status.name()).stream().map(this::toDomain).toList();
        }
        if (consignanteUuid != null) {
            return contratoJpaRepository.findByConsignanteUuid(consignanteUuid).stream().map(this::toDomain).toList();
        }
        return contratoJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public ItemConsignado saveItem(ItemConsignado item) {
        ItemConsignadoJpaEntity entity = itemJpaRepository.findByUuid(item.getUuid())
                .orElseGet(ItemConsignadoJpaEntity::new);
        copyToEntity(item, entity);
        return toDomain(itemJpaRepository.save(entity));
    }

    @Override
    public Optional<ItemConsignado> findItemByUuid(UUID uuid) {
        return itemJpaRepository.findByUuid(uuid).map(this::toDomain);
    }

    @Override
    public List<ItemConsignado> findItemsByContratoUuid(UUID contratoUuid) {
        return itemJpaRepository.findByContratoUuid(contratoUuid).stream().map(this::toDomain).toList();
    }

    @Override
    public List<ItemConsignado> findSellableItemsByVarianteUuid(UUID varianteUuid) {
        return itemJpaRepository.findByVarianteUuidAndRemainingQuantityGreaterThanOrderByReceivedAtAsc(varianteUuid, 0)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public AcertoConsignacao saveAcerto(AcertoConsignacao acerto) {
        AcertoConsignacaoJpaEntity saved = acertoJpaRepository.save(toEntity(acerto));
        acerto.getItems().forEach(item -> acertoItemJpaRepository.save(toEntity(saved.getUuid(), item)));
        return AcertoConsignacao.restore(saved.getUuid(), saved.getContratoUuid(), saved.getResponsibleUuid(),
                saved.getTotalAmount(), saved.getNotes(), saved.getCreatedAt(), acerto.getItems());
    }

    @Override
    public ConsignatarioEnvio saveConsignatarioEnvio(ConsignatarioEnvio consignatario) {
        ConsignatarioEnvioJpaEntity entity = consignatarioEnvioJpaRepository.findByUuid(consignatario.getUuid())
                .orElseGet(ConsignatarioEnvioJpaEntity::new);
        copyToEntity(consignatario, entity);
        return toDomain(consignatarioEnvioJpaRepository.save(entity));
    }

    @Override
    public Optional<ConsignatarioEnvio> findConsignatarioEnvioByUuid(UUID uuid) {
        return consignatarioEnvioJpaRepository.findByUuid(uuid).map(this::toDomain);
    }

    @Override
    public List<ConsignatarioEnvio> findAllConsignatariosEnvio() {
        return consignatarioEnvioJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public ContratoConsignacaoEnvio saveContratoEnvio(ContratoConsignacaoEnvio contrato) {
        ContratoConsignacaoEnvioJpaEntity entity = contratoEnvioJpaRepository.findByUuid(contrato.getUuid())
                .orElseGet(ContratoConsignacaoEnvioJpaEntity::new);
        copyToEntity(contrato, entity);
        return toDomain(contratoEnvioJpaRepository.save(entity));
    }

    @Override
    public Optional<ContratoConsignacaoEnvio> findContratoEnvioByUuid(UUID uuid) {
        return contratoEnvioJpaRepository.findByUuid(uuid).map(this::toDomain);
    }

    @Override
    public List<ContratoConsignacaoEnvio> findContratosEnvio(ContratoConsignacaoStatus status, UUID consigneeUuid) {
        if (status != null && consigneeUuid != null) {
            return contratoEnvioJpaRepository.findByStatusAndConsigneeUuid(status.name(), consigneeUuid).stream().map(this::toDomain).toList();
        }
        if (status != null) {
            return contratoEnvioJpaRepository.findByStatus(status.name()).stream().map(this::toDomain).toList();
        }
        if (consigneeUuid != null) {
            return contratoEnvioJpaRepository.findByConsigneeUuid(consigneeUuid).stream().map(this::toDomain).toList();
        }
        return contratoEnvioJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public ItemConsignacaoEnvio saveItemEnvio(ItemConsignacaoEnvio item) {
        ItemConsignacaoEnvioJpaEntity entity = itemEnvioJpaRepository.findByUuid(item.getUuid())
                .orElseGet(ItemConsignacaoEnvioJpaEntity::new);
        copyToEntity(item, entity);
        return toDomain(itemEnvioJpaRepository.save(entity));
    }

    @Override
    public Optional<ItemConsignacaoEnvio> findItemEnvioByUuid(UUID uuid) {
        return itemEnvioJpaRepository.findByUuid(uuid).map(this::toDomain);
    }

    @Override
    public List<ItemConsignacaoEnvio> findItemsEnvioByContratoUuid(UUID contratoUuid) {
        return itemEnvioJpaRepository.findByContratoUuid(contratoUuid).stream().map(this::toDomain).toList();
    }

    @Override
    public AcertoConsignacaoEnvio saveAcertoEnvio(AcertoConsignacaoEnvio acerto) {
        AcertoConsignacaoEnvioJpaEntity saved = acertoEnvioJpaRepository.save(toEntity(acerto));
        acerto.getItems().forEach(item -> acertoItemEnvioJpaRepository.save(toEntity(saved.getUuid(), item)));
        return AcertoConsignacaoEnvio.restore(saved.getUuid(), saved.getContratoUuid(), saved.getResponsibleUuid(),
                saved.getTotalAmount(), saved.getNotes(), saved.getCreatedAt(), acerto.getItems());
    }

    private Consignante toDomain(ConsignanteJpaEntity e) {
        return Consignante.restore(e.getUuid(), e.getName(), e.getDocument(), e.getEmail(), e.getPhone(),
                e.isActive(), e.getCreatedAt(), e.getUpdatedAt());
    }

    private void copyToEntity(Consignante c, ConsignanteJpaEntity e) {
        e.setUuid(c.getUuid());
        e.setName(c.getName());
        e.setDocument(c.getDocument());
        e.setEmail(c.getEmail());
        e.setPhone(c.getPhone());
        e.setActive(c.isActive());
        e.setCreatedAt(c.getCreatedAt());
        e.setUpdatedAt(c.getUpdatedAt());
    }

    private ContratoConsignacao toDomain(ContratoConsignacaoJpaEntity e) {
        return ContratoConsignacao.restore(e.getUuid(), e.getConsignanteUuid(), e.getCode(),
                ContratoConsignacaoStatus.valueOf(e.getStatus()), e.getOpenedAt(), e.getClosedAt());
    }

    private void copyToEntity(ContratoConsignacao c, ContratoConsignacaoJpaEntity e) {
        e.setUuid(c.getUuid());
        e.setConsignanteUuid(c.getConsignanteUuid());
        e.setCode(c.getCode());
        e.setStatus(c.getStatus().name());
        e.setOpenedAt(c.getOpenedAt());
        e.setClosedAt(c.getClosedAt());
    }

    private ItemConsignado toDomain(ItemConsignadoJpaEntity e) {
        return ItemConsignado.restore(e.getUuid(), e.getContratoUuid(), e.getVarianteUuid(),
                e.getQuantity(), e.getRemainingQuantity(), e.getSoldQuantity(), e.getSettledQuantity(),
                e.getReturnedQuantity(), ItemConsignadoStatus.valueOf(e.getStatus()), e.getReceivedAt(),
                e.getSoldSaleUuid(), e.getReturnedAt());
    }

    private void copyToEntity(ItemConsignado i, ItemConsignadoJpaEntity e) {
        e.setUuid(i.getUuid());
        e.setContratoUuid(i.getContratoUuid());
        e.setVarianteUuid(i.getVarianteUuid());
        e.setQuantity(i.getQuantity());
        e.setRemainingQuantity(i.getRemainingQuantity());
        e.setSoldQuantity(i.getSoldQuantity());
        e.setSettledQuantity(i.getSettledQuantity());
        e.setReturnedQuantity(i.getReturnedQuantity());
        e.setStatus(i.getStatus().name());
        e.setReceivedAt(i.getReceivedAt());
        e.setSoldSaleUuid(i.getSoldSaleUuid());
        e.setReturnedAt(i.getReturnedAt());
    }

    private AcertoConsignacaoJpaEntity toEntity(AcertoConsignacao a) {
        AcertoConsignacaoJpaEntity e = new AcertoConsignacaoJpaEntity();
        e.setUuid(a.getUuid());
        e.setContratoUuid(a.getContratoUuid());
        e.setResponsibleUuid(a.getResponsibleUuid());
        e.setTotalAmount(a.getTotalAmount());
        e.setNotes(a.getNotes());
        e.setCreatedAt(a.getCreatedAt());
        return e;
    }

    private AcertoItemJpaEntity toEntity(UUID acertoUuid, AcertoConsignacao.AcertoItem item) {
        AcertoItemJpaEntity e = new AcertoItemJpaEntity();
        e.setAcertoUuid(acertoUuid);
        e.setItemUuid(item.itemUuid());
        e.setSettledQuantity(item.settledQuantity());
        e.setManualAmount(item.manualAmount());
        return e;
    }

    private ConsignatarioEnvio toDomain(ConsignatarioEnvioJpaEntity e) {
        return ConsignatarioEnvio.restore(e.getUuid(), e.getName(), e.getDocument(), e.getEmail(), e.getPhone(),
                e.isActive(), e.getCreatedAt(), e.getUpdatedAt());
    }

    private void copyToEntity(ConsignatarioEnvio c, ConsignatarioEnvioJpaEntity e) {
        e.setUuid(c.getUuid());
        e.setName(c.getName());
        e.setDocument(c.getDocument());
        e.setEmail(c.getEmail());
        e.setPhone(c.getPhone());
        e.setActive(c.isActive());
        e.setCreatedAt(c.getCreatedAt());
        e.setUpdatedAt(c.getUpdatedAt());
    }

    private ContratoConsignacaoEnvio toDomain(ContratoConsignacaoEnvioJpaEntity e) {
        return ContratoConsignacaoEnvio.restore(e.getUuid(), e.getConsigneeUuid(), e.getCode(),
                ContratoConsignacaoStatus.valueOf(e.getStatus()), e.getOpenedAt(), e.getClosedAt());
    }

    private void copyToEntity(ContratoConsignacaoEnvio c, ContratoConsignacaoEnvioJpaEntity e) {
        e.setUuid(c.getUuid());
        e.setConsigneeUuid(c.getConsigneeUuid());
        e.setCode(c.getCode());
        e.setStatus(c.getStatus().name());
        e.setOpenedAt(c.getOpenedAt());
        e.setClosedAt(c.getClosedAt());
    }

    private ItemConsignacaoEnvio toDomain(ItemConsignacaoEnvioJpaEntity e) {
        return ItemConsignacaoEnvio.restore(e.getUuid(), e.getContratoUuid(), e.getVarianteUuid(),
                e.getQuantity(), e.getAvailableQuantity(), e.getSoldQuantity(), e.getSettledQuantity(),
                e.getReturnedQuantity(), ItemConsignacaoEnvioStatus.valueOf(e.getStatus()),
                e.getSentAt(), e.getReturnedAt());
    }

    private void copyToEntity(ItemConsignacaoEnvio i, ItemConsignacaoEnvioJpaEntity e) {
        e.setUuid(i.getUuid());
        e.setContratoUuid(i.getContratoUuid());
        e.setVarianteUuid(i.getVarianteUuid());
        e.setQuantity(i.getQuantity());
        e.setAvailableQuantity(i.getAvailableQuantity());
        e.setSoldQuantity(i.getSoldQuantity());
        e.setSettledQuantity(i.getSettledQuantity());
        e.setReturnedQuantity(i.getReturnedQuantity());
        e.setStatus(i.getStatus().name());
        e.setSentAt(i.getSentAt());
        e.setReturnedAt(i.getReturnedAt());
    }

    private AcertoConsignacaoEnvioJpaEntity toEntity(AcertoConsignacaoEnvio a) {
        AcertoConsignacaoEnvioJpaEntity e = new AcertoConsignacaoEnvioJpaEntity();
        e.setUuid(a.getUuid());
        e.setContratoUuid(a.getContratoUuid());
        e.setResponsibleUuid(a.getResponsibleUuid());
        e.setTotalAmount(a.getTotalAmount());
        e.setNotes(a.getNotes());
        e.setCreatedAt(a.getCreatedAt());
        return e;
    }

    private AcertoItemConsignacaoEnvioJpaEntity toEntity(UUID acertoUuid, AcertoConsignacaoEnvio.AcertoItem item) {
        AcertoItemConsignacaoEnvioJpaEntity e = new AcertoItemConsignacaoEnvioJpaEntity();
        e.setAcertoUuid(acertoUuid);
        e.setItemUuid(item.itemUuid());
        e.setSettledQuantity(item.settledQuantity());
        e.setManualAmount(item.manualAmount());
        return e;
    }
}
