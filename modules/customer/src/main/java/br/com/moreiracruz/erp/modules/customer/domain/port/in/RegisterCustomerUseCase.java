package br.com.moreiracruz.erp.modules.customer.domain.port.in;

/**
 * Inbound port for registering a new customer.
 */
public interface RegisterCustomerUseCase {
    ClienteResponse register(RegisterCustomerCommand cmd);
}
