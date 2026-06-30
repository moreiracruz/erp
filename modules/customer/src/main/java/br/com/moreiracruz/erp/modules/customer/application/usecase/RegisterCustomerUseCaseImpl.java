package br.com.moreiracruz.erp.modules.customer.application.usecase;

import br.com.moreiracruz.erp.modules.customer.domain.model.Cliente;
import br.com.moreiracruz.erp.modules.customer.domain.port.in.ClienteResponse;
import br.com.moreiracruz.erp.modules.customer.domain.port.in.RegisterCustomerCommand;
import br.com.moreiracruz.erp.modules.customer.domain.port.in.RegisterCustomerUseCase;
import br.com.moreiracruz.erp.modules.customer.domain.port.out.ClienteRepository;
import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterCustomerUseCaseImpl implements RegisterCustomerUseCase {

    private final ClienteRepository clienteRepository;

    public RegisterCustomerUseCaseImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public ClienteResponse register(RegisterCustomerCommand cmd) {
        if (clienteRepository.existsByCpf(cmd.cpf())) {
            throw new ValidationException("CPF já cadastrado");
        }

        Cliente cliente = Cliente.create(
                cmd.fullName(),
                cmd.cpf(),
                cmd.email(),
                cmd.phone(),
                cmd.birthDate()
        );

        Cliente saved = clienteRepository.save(cliente);
        return toResponse(saved);
    }

    private ClienteResponse toResponse(Cliente c) {
        return new ClienteResponse(
                c.getUuid(),
                c.getFullName(),
                c.getCpf().value(),
                c.getEmail() != null ? c.getEmail().value() : null,
                c.getPhone(),
                c.getBirthDate(),
                c.isActive()
        );
    }
}
