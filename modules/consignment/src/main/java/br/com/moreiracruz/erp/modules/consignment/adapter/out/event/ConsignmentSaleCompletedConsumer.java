package br.com.moreiracruz.erp.modules.consignment.adapter.out.event;

import br.com.moreiracruz.erp.modules.consignment.application.usecase.ConsignmentService;
import br.com.moreiracruz.erp.shared.events.EventEnvelope;
import br.com.moreiracruz.erp.shared.events.SaleCompletedPayload;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ConsignmentSaleCompletedConsumer {

    private final ConsignmentService consignmentService;

    public ConsignmentSaleCompletedConsumer(ConsignmentService consignmentService) {
        this.consignmentService = consignmentService;
    }

    @EventListener
    public void onSaleCompleted(EventEnvelope<?> event) {
        if (!"SaleCompleted".equals(event.eventType()) || !(event.payload() instanceof SaleCompletedPayload payload)) {
            return;
        }
        consignmentService.markSaleCompleted(payload.saleUuid(), payload.items());
    }
}
