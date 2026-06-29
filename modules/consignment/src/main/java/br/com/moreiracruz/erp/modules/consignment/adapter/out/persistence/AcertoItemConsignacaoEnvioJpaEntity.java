package br.com.moreiracruz.erp.modules.consignment.adapter.out.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "acerto_itens_consignacao_envio")
public class AcertoItemConsignacaoEnvioJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "acerto_uuid", nullable = false)
    private UUID acertoUuid;
    @Column(name = "item_uuid", nullable = false)
    private UUID itemUuid;
    @Column(name = "settled_quantity", nullable = false)
    private int settledQuantity;
    @Column(name = "manual_amount", nullable = false)
    private BigDecimal manualAmount;

    public UUID getAcertoUuid() { return acertoUuid; }
    public void setAcertoUuid(UUID acertoUuid) { this.acertoUuid = acertoUuid; }
    public UUID getItemUuid() { return itemUuid; }
    public void setItemUuid(UUID itemUuid) { this.itemUuid = itemUuid; }
    public int getSettledQuantity() { return settledQuantity; }
    public void setSettledQuantity(int settledQuantity) { this.settledQuantity = settledQuantity; }
    public BigDecimal getManualAmount() { return manualAmount; }
    public void setManualAmount(BigDecimal manualAmount) { this.manualAmount = manualAmount; }
}
