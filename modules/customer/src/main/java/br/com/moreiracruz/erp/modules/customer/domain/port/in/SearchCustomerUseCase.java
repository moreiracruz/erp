package br.com.moreiracruz.erp.modules.customer.domain.port.in;

import br.com.moreiracruz.erp.shared.kernel.pagination.PageResult;

/**
 * Inbound port for searching customers.
 */
public interface SearchCustomerUseCase {
    PageResult<ClienteResponse> search(CustomerSearchQuery query);
}
