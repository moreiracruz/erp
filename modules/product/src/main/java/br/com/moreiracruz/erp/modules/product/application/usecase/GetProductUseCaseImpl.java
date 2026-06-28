package br.com.moreiracruz.erp.modules.product.application.usecase;

import br.com.moreiracruz.erp.modules.product.domain.port.in.GetProductUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.in.ProdutoResponse;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ProdutoRepository;
import br.com.moreiracruz.erp.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case implementation for retrieving products.
 */
@Service
@Transactional(readOnly = true)
public class GetProductUseCaseImpl implements GetProductUseCase {

    private final ProdutoRepository produtoRepository;

    public GetProductUseCaseImpl(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Override
    public List<ProdutoResponse> findAll() {
        return produtoRepository.findAllActive().stream()
                .map(RegisterProductUseCaseImpl::toResponse)
                .toList();
    }

    @Override
    public ProdutoResponse findByUuid(UUID uuid) {
        return produtoRepository.findByUuid(uuid)
                .map(RegisterProductUseCaseImpl::toResponse)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado"));
    }
}
