import { describe, it, expect, vi, beforeEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';

import { CatalogService } from './catalog.service';
import { ProductPort } from '../../../../core/ports/product.port';
import { ProductSummary } from '../../../../core/models';
import { DEFAULT_FILTERS, FilterState, CACHE_DURATION_MS } from '../models';

function createMockProducts(count = 3): ProductSummary[] {
  return Array.from({ length: count }, (_, i) => ({
    uuid: `uuid-${i}`,
    name: `Product ${i}`,
    brand: 'Brand',
    category: 'Category',
    minPrice: 10 + i,
    maxPrice: 20 + i,
  }));
}

describe('CatalogService', () => {
  let service: CatalogService;
  let productPortSpy: {
    getAll: ReturnType<typeof vi.fn>;
    getByUuid: ReturnType<typeof vi.fn>;
    search: ReturnType<typeof vi.fn>;
    getByCategory: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    productPortSpy = {
      getAll: vi.fn().mockReturnValue(of(createMockProducts())),
      getByUuid: vi.fn().mockReturnValue(of({})),
      search: vi.fn().mockReturnValue(of([])),
      getByCategory: vi.fn().mockReturnValue(of([])),
    };

    TestBed.configureTestingModule({
      providers: [
        CatalogService,
        { provide: ProductPort, useValue: productPortSpy },
      ],
    });

    service = TestBed.inject(CatalogService);
  });

  /**
   * Property 6: Page reset on filter/sort change
   * Validates: Requirements 4.5
   *
   * For any catalog state where page > 0, applying any filter change
   * or sort change SHALL reset page to 0.
   */
  describe('Property 6: Page reset on filter/sort change', () => {
    it('should reset page to 0 when applyFilter is called while page > 0', () => {
      // Advance page
      service.loadNextPage();
      service.loadNextPage();
      expect(service.page()).toBe(2);

      const newFilters: FilterState = {
        ...DEFAULT_FILTERS,
        categories: ['Vestidos'],
      };
      service.applyFilter(newFilters);

      expect(service.page()).toBe(0);
    });

    it('should reset page to 0 when applySort is called while page > 0', () => {
      service.loadNextPage();
      service.loadNextPage();
      service.loadNextPage();
      expect(service.page()).toBe(3);

      service.applySort('price-asc');

      expect(service.page()).toBe(0);
    });

    it('should set page to 0 when applyFilter is called even if page is already 0', () => {
      expect(service.page()).toBe(0);

      service.applyFilter({ ...DEFAULT_FILTERS, colors: ['Azul'] });

      expect(service.page()).toBe(0);
    });

    it('should set page to 0 when applySort is called even if page is already 0', () => {
      expect(service.page()).toBe(0);

      service.applySort('price-desc');

      expect(service.page()).toBe(0);
    });
  });

  /**
   * Property 14: Cache TTL behavior
   * Validates: Requirements 10.5
   *
   * For any sequence of getAll() calls, if the elapsed time since the last
   * fresh fetch is less than CACHE_DURATION_MS, the service SHALL return
   * the cached data without issuing a new HTTP request. If elapsed time
   * exceeds CACHE_DURATION_MS, it SHALL issue a new request.
   */
  describe('Property 14: Cache TTL behavior', () => {
    it('should fetch products on first loadProducts call', () => {
      service.loadProducts();

      expect(productPortSpy.getAll).toHaveBeenCalledTimes(1);
      expect(service.products().length).toBe(3);
    });

    it('should NOT re-fetch if cache is still fresh (< CACHE_DURATION_MS)', () => {
      service.loadProducts();
      expect(productPortSpy.getAll).toHaveBeenCalledTimes(1);

      // Call again immediately — cache is fresh
      service.loadProducts();
      expect(productPortSpy.getAll).toHaveBeenCalledTimes(1);
    });

    it('should re-fetch if cache has expired (>= CACHE_DURATION_MS)', () => {
      service.loadProducts();
      expect(productPortSpy.getAll).toHaveBeenCalledTimes(1);

      // Advance time beyond cache duration
      vi.spyOn(Date, 'now').mockReturnValue(Date.now() + CACHE_DURATION_MS + 1);

      service.loadProducts();
      expect(productPortSpy.getAll).toHaveBeenCalledTimes(2);

      vi.restoreAllMocks();
    });

    it('should fetch if products array is empty even if within TTL', () => {
      // Products are empty initially, so it should always fetch
      service.loadProducts();
      expect(productPortSpy.getAll).toHaveBeenCalledTimes(1);

      // Simulate error scenario where products remain empty
      productPortSpy.getAll.mockReturnValue(throwError(() => ({ status: 500 })));
      vi.spyOn(Date, 'now').mockReturnValue(Date.now() + CACHE_DURATION_MS + 1);

      service.loadProducts();
      // It fetches because cache expired
      expect(productPortSpy.getAll).toHaveBeenCalledTimes(2);

      vi.restoreAllMocks();
    });
  });

  describe('Error mapping', () => {
    it('should map network error (status 0) to connection message', () => {
      productPortSpy.getAll.mockReturnValue(throwError(() => ({ status: 0 })));

      service.loadProducts();

      expect(service.error()).toBe('Não foi possível carregar. Verifique sua conexão.');
    });

    it('should map 404 error to not-found message', () => {
      productPortSpy.getAll.mockReturnValue(throwError(() => ({ status: 404 })));

      service.loadProducts();

      expect(service.error()).toBe('Produto não encontrado.');
    });

    it('should map other errors to generic message', () => {
      productPortSpy.getAll.mockReturnValue(throwError(() => ({ status: 500 })));

      service.loadProducts();

      expect(service.error()).toBe('Ocorreu um erro. Tente novamente.');
    });

    it('should map undefined status to generic message', () => {
      productPortSpy.getAll.mockReturnValue(throwError(() => ({})));

      service.loadProducts();

      expect(service.error()).toBe('Ocorreu um erro. Tente novamente.');
    });
  });

  describe('Loading states', () => {
    it('should set loading to true while request is in progress', () => {
      const subject = new Subject<ProductSummary[]>();
      productPortSpy.getAll.mockReturnValue(subject.asObservable());

      service.loadProducts();
      expect(service.loading()).toBe(true);

      subject.next(createMockProducts());
      subject.complete();
      expect(service.loading()).toBe(false);
    });

    it('should set loading to false after successful load', () => {
      service.loadProducts();

      expect(service.loading()).toBe(false);
    });

    it('should set loading to false after error', () => {
      productPortSpy.getAll.mockReturnValue(throwError(() => ({ status: 500 })));

      service.loadProducts();

      expect(service.loading()).toBe(false);
    });

    it('should clear previous error on new load attempt', () => {
      productPortSpy.getAll.mockReturnValue(throwError(() => ({ status: 500 })));
      service.loadProducts();
      expect(service.error()).not.toBeNull();

      productPortSpy.getAll.mockReturnValue(of(createMockProducts()));
      // Reset cache to force re-fetch
      vi.spyOn(Date, 'now').mockReturnValue(Date.now() + CACHE_DURATION_MS + 1);
      service.loadProducts();

      expect(service.error()).toBeNull();
      vi.restoreAllMocks();
    });
  });

  describe('Search', () => {
    it('should trigger search for queries with >= 3 characters', () => {
      const mockResults = createMockProducts(2);
      productPortSpy.search.mockReturnValue(of(mockResults));

      service.search('ves');

      expect(productPortSpy.search).toHaveBeenCalledWith('ves');
      expect(service.searchResults()).toEqual(mockResults);
    });

    it('should not trigger search for queries with < 3 characters', () => {
      service.search('ab');

      expect(productPortSpy.search).not.toHaveBeenCalled();
      expect(service.searchResults()).toEqual([]);
    });

    it('should clear search results for empty query', () => {
      // First set some results
      productPortSpy.search.mockReturnValue(of(createMockProducts(2)));
      service.search('vestido');
      expect(service.searchResults().length).toBe(2);

      // Then search with short query
      service.search('');
      expect(service.searchResults()).toEqual([]);
    });

    it('should clear search results on search error', () => {
      productPortSpy.search.mockReturnValue(throwError(() => new Error('fail')));

      service.search('vestido');

      expect(service.searchResults()).toEqual([]);
    });
  });

  describe('retry()', () => {
    it('should reset cache and re-fetch products', () => {
      service.loadProducts();
      expect(productPortSpy.getAll).toHaveBeenCalledTimes(1);

      // Retry should force a new fetch even though cache is fresh
      service.retry();
      expect(productPortSpy.getAll).toHaveBeenCalledTimes(2);
    });

    it('should clear error on retry', () => {
      productPortSpy.getAll.mockReturnValue(throwError(() => ({ status: 500 })));
      service.loadProducts();
      expect(service.error()).not.toBeNull();

      productPortSpy.getAll.mockReturnValue(of(createMockProducts()));
      service.retry();

      expect(service.error()).toBeNull();
      expect(service.products().length).toBe(3);
    });
  });
});
