package com.erp.modules.customer.domain.port.in;

import org.springframework.data.domain.Page;

/**
 * Inbound port for searching customers.
 */
public interface SearchCustomerUseCase {
    Page<ClienteResponse> search(CustomerSearchQuery query);
}
