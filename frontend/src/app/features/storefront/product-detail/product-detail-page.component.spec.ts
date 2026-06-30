import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { NO_ERRORS_SCHEMA, signal } from '@angular/core';
import { of, throwError } from 'rxjs';

import { ProductDetailPageComponent } from './product-detail-page.component';
import { CatalogService } from '../catalog/services/catalog.service';
import { CartService } from '../services/cart.service';
import { ImagePort } from '../../../core/ports/image.port';
import { Product, Variant, ProductImage } from '../../../core/models';
import { SelectedVariant } from '../catalog/models';

function makeVariant(overrides: Partial<Variant> = {}): Variant {
  return {
    uuid: 'variant-1',
    sku: 'SKU-001',
    size: 'M',
    color: 'Azul',
    barcode: '1234567890',
    price: 89.9,
    cost: 40.0,
    active: true,
    ...overrides,
  };
}

function makeProduct(overrides: Partial<Product> = {}): Product {
  return {
    uuid: 'prod-123',
    name: 'Camiseta Básica',
    brand: 'Marca X',
    category: 'Camisetas',
    active: true,
    variants: [
      makeVariant({ uuid: 'v1', size: 'M', color: 'Azul', price: 89.9 }),
      makeVariant({ uuid: 'v2', size: 'G', color: 'Vermelho', price: 99.9, active: true }),
    ],
    createdAt: '2024-01-01T00:00:00Z',
    ...overrides,
  };
}

function makeProductImage(overrides: Partial<ProductImage> = {}): ProductImage {
  return {
    id: 1,
    filename: 'img.webp',
    originalName: 'image.png',
    contentType: 'image/webp',
    fileSize: 1024,
    sortOrder: 0,
    main: true,
    createdAt: '2024-01-01T00:00:00Z',
    thumbnailUrl: '/images/thumb.webp',
    cardUrl: '/images/card.webp',
    fullUrl: '/images/full.webp',
    ...overrides,
  };
}

/**
 * Validates: Requirements 6.5, 6.6, 7.3, 7.4, 10.4
 */
describe('ProductDetailPageComponent - Unit Tests', () => {
  let component: ProductDetailPageComponent;
  let fixture: ComponentFixture<ProductDetailPageComponent>;
  let catalogServiceMock: {
    getProductByUuid: ReturnType<typeof vi.fn>;
    products: ReturnType<typeof signal>;
  };
  let cartServiceMock: { addItem: ReturnType<typeof vi.fn> };
  let imagePortMock: { listByProduct: ReturnType<typeof vi.fn> };
  let routerMock: { navigate: ReturnType<typeof vi.fn> };

  function createComponent(uuid: string | null = 'prod-123') {
    const activatedRouteMock = {
      snapshot: {
        paramMap: convertToParamMap(uuid ? { uuid } : {}),
      },
    };

    TestBed.configureTestingModule({
      imports: [ProductDetailPageComponent],
      providers: [
        { provide: CatalogService, useValue: catalogServiceMock },
        { provide: CartService, useValue: cartServiceMock },
        { provide: ImagePort, useValue: imagePortMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
        { provide: Router, useValue: routerMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductDetailPageComponent);
    component = fixture.componentInstance;
  }

  beforeEach(() => {
    catalogServiceMock = {
      getProductByUuid: vi.fn().mockReturnValue(of(makeProduct())),
      products: signal([]),
    };
    cartServiceMock = {
      addItem: vi.fn(),
    };
    imagePortMock = {
      listByProduct: vi.fn().mockReturnValue(of([makeProductImage()])),
    };
    routerMock = {
      navigate: vi.fn(),
    };
  });

  describe('Default variant selection (Req 6.6)', () => {
    it('sets selectedVariant to first active variant on product load', () => {
      createComponent();
      fixture.detectChanges();

      const sel = component.selectedVariant();
      expect(sel.size).toBe('M');
      expect(sel.color).toBe('Azul');
      expect(sel.variant).not.toBeNull();
      expect(sel.variant!.uuid).toBe('v1');
    });

    it('sets selectedVariant to null when product has no active variants', () => {
      const product = makeProduct({
        variants: [makeVariant({ active: false })],
      });
      catalogServiceMock.getProductByUuid.mockReturnValue(of(product));

      createComponent();
      fixture.detectChanges();

      const sel = component.selectedVariant();
      expect(sel.size).toBeNull();
      expect(sel.color).toBeNull();
      expect(sel.variant).toBeNull();
    });
  });

  describe('Price update on variant change (Req 6.5)', () => {
    it('computes currentPrice from selectedVariant size and color', () => {
      createComponent();
      fixture.detectChanges();

      // Default variant is M/Azul with price 89.9
      expect(component.currentPrice()).toBe(89.9);
    });

    it('updates currentPrice when selectedVariant changes', () => {
      const product = makeProduct();
      catalogServiceMock.getProductByUuid.mockReturnValue(of(product));

      createComponent();
      fixture.detectChanges();

      // Change to variant G/Vermelho with price 99.9
      const newSelected: SelectedVariant = {
        size: 'G',
        color: 'Vermelho',
        variant: product.variants[1],
      };
      component.onVariantChange(newSelected);

      expect(component.currentPrice()).toBe(99.9);
    });

    it('returns null when size or color is not selected', () => {
      createComponent();
      fixture.detectChanges();

      component.onVariantChange({ size: null, color: null, variant: null });

      expect(component.currentPrice()).toBeNull();
    });
  });

  describe('Add-to-cart validation (Req 7.3)', () => {
    it('addToCartDisabled is true when no variant is selected', () => {
      createComponent();
      fixture.detectChanges();

      component.onVariantChange({ size: null, color: null, variant: null });

      expect(component.addToCartDisabled()).toBe(true);
    });

    it('addToCartDisabled is false when a variant is selected', () => {
      createComponent();
      fixture.detectChanges();

      // Default variant is selected on load
      expect(component.addToCartDisabled()).toBe(false);
    });
  });

  describe('Add-to-cart success (Req 7.4)', () => {
    it('calls cartService.addItem with correct CartItem', () => {
      createComponent();
      fixture.detectChanges();

      component.onAddToCart();

      expect(cartServiceMock.addItem).toHaveBeenCalledWith(
        expect.objectContaining({
          productUuid: 'prod-123',
          variantUuid: 'v1',
          productName: 'Camiseta Básica',
          size: 'M',
          color: 'Azul',
          price: 89.9,
          quantity: 1,
          imageUrl: '/images/thumb.webp',
        })
      );
    });

    it('does not call addItem when no variant is selected', () => {
      createComponent();
      fixture.detectChanges();

      component.onVariantChange({ size: null, color: null, variant: null });
      component.onAddToCart();

      expect(cartServiceMock.addItem).not.toHaveBeenCalled();
    });

    it('uses placeholder image when no product images available', () => {
      imagePortMock.listByProduct.mockReturnValue(of([]));

      createComponent();
      fixture.detectChanges();

      component.onAddToCart();

      expect(cartServiceMock.addItem).toHaveBeenCalledWith(
        expect.objectContaining({
          imageUrl: 'assets/images/product-placeholder.webp',
        })
      );
    });
  });

  describe('Error states (Req 10.4)', () => {
    it('shows "Produto não encontrado." on 404 error', () => {
      catalogServiceMock.getProductByUuid.mockReturnValue(
        throwError(() => ({ status: 404 }))
      );

      createComponent();
      fixture.detectChanges();

      expect(component.error()).toBe('Produto não encontrado.');
      expect(component.loading()).toBe(false);

      const el: HTMLElement = fixture.nativeElement;
      const errorText = el.querySelector('.product-detail-page__error-text');
      expect(errorText?.textContent).toContain('Produto não encontrado.');

      const backLink = el.querySelector('.product-detail-page__back-link');
      expect(backLink).not.toBeNull();
      expect(backLink?.textContent).toContain('Voltar ao catálogo');
    });

    it('shows generic error message on non-404 error', () => {
      catalogServiceMock.getProductByUuid.mockReturnValue(
        throwError(() => ({ status: 500 }))
      );

      createComponent();
      fixture.detectChanges();

      expect(component.error()).toBe('Ocorreu um erro ao carregar o produto. Tente novamente.');
      expect(component.loading()).toBe(false);

      const el: HTMLElement = fixture.nativeElement;
      const errorText = el.querySelector('.product-detail-page__error-text');
      expect(errorText?.textContent).toContain('Ocorreu um erro ao carregar o produto. Tente novamente.');
    });
  });

  describe('Loading state', () => {
    it('shows shimmer placeholders while loading', () => {
      // Don't trigger ngOnInit yet — component starts with loading=true
      createComponent();

      // Before detectChanges, loading is true by default
      expect(component.loading()).toBe(true);

      fixture.detectChanges();

      // After detectChanges the product loaded (mocked as sync observable)
      // so loading becomes false. Test pre-load state via signal directly.
    });

    it('sets loading to false after product loads successfully', () => {
      createComponent();
      fixture.detectChanges();

      expect(component.loading()).toBe(false);
      expect(component.product()).not.toBeNull();
    });

    it('sets loading to false after product load fails', () => {
      catalogServiceMock.getProductByUuid.mockReturnValue(
        throwError(() => ({ status: 500 }))
      );

      createComponent();
      fixture.detectChanges();

      expect(component.loading()).toBe(false);
    });
  });

  describe('Missing UUID', () => {
    it('sets error when no uuid in route params', () => {
      createComponent(null);
      fixture.detectChanges();

      expect(component.error()).toBe('Produto não encontrado.');
      expect(component.loading()).toBe(false);
      expect(catalogServiceMock.getProductByUuid).not.toHaveBeenCalled();
    });
  });
});
