package br.com.moreiracruz.erp.modules.product.application.usecase;

import br.com.moreiracruz.erp.modules.product.domain.model.Produto;
import br.com.moreiracruz.erp.modules.product.domain.model.VarianteProduto;
import br.com.moreiracruz.erp.modules.product.domain.port.in.CatalogProductResponse;
import br.com.moreiracruz.erp.modules.product.domain.port.in.CatalogVariantResponse;
import br.com.moreiracruz.erp.modules.product.domain.port.in.GetProductUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.in.ProdutoResponse;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ProdutoRepository;
import br.com.moreiracruz.erp.modules.product.domain.port.out.VarianteRepository;
import br.com.moreiracruz.erp.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Use case implementation for retrieving products.
 */
@Service
@Transactional(readOnly = true)
public class GetProductUseCaseImpl implements GetProductUseCase {

    private final ProdutoRepository produtoRepository;
    private final VarianteRepository varianteRepository;

    public GetProductUseCaseImpl(ProdutoRepository produtoRepository,
                                 VarianteRepository varianteRepository) {
        this.produtoRepository = produtoRepository;
        this.varianteRepository = varianteRepository;
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

    @Override
    public List<CatalogProductResponse> findCatalog() {
        return produtoRepository.findAllActive().stream()
                .map(this::toCatalogResponse)
                .toList();
    }

    @Override
    public CatalogProductResponse findCatalogByUuid(UUID uuid) {
        return produtoRepository.findByUuid(uuid)
                .filter(Produto::isActive)
                .map(this::toCatalogResponse)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado"));
    }

    private CatalogProductResponse toCatalogResponse(Produto produto) {
        List<CatalogVariantResponse> variants = varianteRepository.findByProdutoId(produto.getId()).stream()
                .filter(VarianteProduto::isActive)
                .map(this::toCatalogVariantResponse)
                .toList();

        BigDecimal minPrice = variants.stream()
                .map(CatalogVariantResponse::price)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = variants.stream()
                .map(CatalogVariantResponse::price)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return new CatalogProductResponse(
                produto.getUuid(),
                produto.getName(),
                produto.getBrand(),
                produto.getCategory(),
                produto.isActive(),
                variants,
                minPrice,
                maxPrice,
                produto.getCreatedAt()
        );
    }

    private CatalogVariantResponse toCatalogVariantResponse(VarianteProduto variant) {
        return new CatalogVariantResponse(
                variant.getUuid(),
                variant.getSku().value(),
                variant.getSize(),
                variant.getColor(),
                variant.getBarcode().value(),
                variant.getPrice().amount(),
                variant.isActive()
        );
    }
}
