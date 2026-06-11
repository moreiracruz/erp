package com.erp.modules.product.application.usecase;

import com.erp.modules.product.domain.port.in.GetProductUseCase;
import com.erp.modules.product.domain.port.in.ProdutoResponse;
import com.erp.modules.product.domain.port.out.ProdutoRepository;
import com.erp.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case implementation for retrieving a product by its public UUID.
 */
@Service
@Transactional(readOnly = true)
public class GetProductUseCaseImpl implements GetProductUseCase {

    private final ProdutoRepository produtoRepository;

    public GetProductUseCaseImpl(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Override
    public ProdutoResponse findByUuid(UUID uuid) {
        return produtoRepository.findByUuid(uuid)
                .map(RegisterProductUseCaseImpl::toResponse)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado"));
    }
}
