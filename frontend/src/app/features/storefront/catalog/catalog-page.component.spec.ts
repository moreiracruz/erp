import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { Component, signal, Input } from '@angular/core';
import { NO_ERRORS_SCHEMA } from '@angular/core';

import { CatalogPageComponent } from './catalog-page.component';
import { CatalogService } from './services/catalog.service';
import { DEFAULT_FILTERS, FilterState, SortOption } from './models';
import { ProductSummary } from '../../../core/models';
import { BreadcrumbComponent } from '../shared/components/breadcrumb/breadcrumb.component';
import { FilterPanelComponent } from './components/filter-panel/filter-panel.component';
import { SortDropdownComponent } from './components/sort-dropdown/sort-dropdown.component';
import { ProductGridComponent } from './components/product-grid/product-grid.component';
import { SearchOverlayComponent } from './components/search-overlay/search-overlay.component';

@Component({ selector: 'app-breadcrumb', standalone: true, template: '' })
class MockBreadcrumbComponent {
  @Input() segments: unknown[] = [];
}

@Component({ selector: 'app-filter-panel', standalone: true, template: '' })
class MockFilterPanelComponent {
  @Input() filters: unknown;
  @Input() counts: unknown;
  @Input() open = false;
}

@Component({ selector: 'app-sort-dropdown', standalone: true, template: '' })
class MockSortDropdownComponent {
  @Input() currentSort: unknown;
}

@Component({ selector: 'app-product-grid', standalone: true, template: '' })
class MockProductGridComponent {
  @Input() products: unknown[] = [];
  @Input() loading = false;
  @Input() hasMore = false;
}

@Component({ selector: 'app-search-overlay', standalone: true, template: '' })
class MockSearchOverlayComponent {
  @Input() open = false;
  @Input() suggestions: unknown[] = [];
}

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

describe('CatalogPageComponent', () => {
  let component: CatalogPageComponent;
  let fixture: ComponentFixture<CatalogPageComponent>;
  let catalogServiceMock: {
    products: ReturnType<typeof signal<ProductSummary[]>>;
    paginatedProducts: ReturnType<typeof signal<ProductSummary[]>>;
    loading: ReturnType<typeof signal<boolean>>;
    error: ReturnType<typeof signal<string | null>>;
    hasMore: ReturnType<typeof signal<boolean>>;
    filters: ReturnType<typeof signal<FilterState>>;
    sort: ReturnType<typeof signal<SortOption>>;
    searchResults: ReturnType<typeof signal<ProductSummary[]>>;
    filteredProducts: ReturnType<typeof signal<ProductSummary[]>>;
    loadProducts: ReturnType<typeof vi.fn>;
    applyFilter: ReturnType<typeof vi.fn>;
    applySort: ReturnType<typeof vi.fn>;
    loadNextPage: ReturnType<typeof vi.fn>;
    search: ReturnType<typeof vi.fn>;
    retry: ReturnType<typeof vi.fn>;
  };
  let routerMock: { navigate: ReturnType<typeof vi.fn> };
  let activatedRouteMock: { snapshot: { queryParams: Record<string, string> } };

  beforeEach(() => {
    catalogServiceMock = {
      products: signal<ProductSummary[]>([]),
      paginatedProducts: signal<ProductSummary[]>([]),
      loading: signal<boolean>(false),
      error: signal<string | null>(null),
      hasMore: signal<boolean>(false),
      filters: signal<FilterState>(DEFAULT_FILTERS),
      sort: signal<SortOption>('newest'),
      searchResults: signal<ProductSummary[]>([]),
      filteredProducts: signal<ProductSummary[]>([]),
      loadProducts: vi.fn(),
      applyFilter: vi.fn(),
      applySort: vi.fn(),
      loadNextPage: vi.fn(),
      search: vi.fn(),
      retry: vi.fn(),
    };

    routerMock = {
      navigate: vi.fn(),
    };

    activatedRouteMock = {
      snapshot: { queryParams: {} },
    };

    TestBed.configureTestingModule({
      imports: [CatalogPageComponent],
      providers: [
        { provide: CatalogService, useValue: catalogServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    })
      .overrideComponent(CatalogPageComponent, {
        remove: {
          imports: [
            BreadcrumbComponent,
            FilterPanelComponent,
            SortDropdownComponent,
            ProductGridComponent,
            SearchOverlayComponent,
          ],
        },
        add: {
          imports: [
            MockBreadcrumbComponent,
            MockFilterPanelComponent,
            MockSortDropdownComponent,
            MockProductGridComponent,
            MockSearchOverlayComponent,
          ],
        },
      });

    fixture = TestBed.createComponent(CatalogPageComponent);
    component = fixture.componentInstance;
  });

  /**
   * Validates: Requirements 1.5
   */
  describe('Query param reading on init', () => {
    it('should read query params and apply default filters/sort on init when no params present', () => {
      activatedRouteMock.snapshot.queryParams = {};

      fixture.detectChanges(); // triggers ngOnInit

      expect(catalogServiceMock.applyFilter).toHaveBeenCalledWith(DEFAULT_FILTERS);
      expect(catalogServiceMock.applySort).toHaveBeenCalledWith('newest');
      expect(catalogServiceMock.loadProducts).toHaveBeenCalled();
    });

    it('should deserialize category query params and apply filters on init', () => {
      activatedRouteMock.snapshot.queryParams = { cat: 'Vestidos,Saias', sort: 'price-asc' };

      fixture.detectChanges();

      expect(catalogServiceMock.applyFilter).toHaveBeenCalledWith(
        expect.objectContaining({ categories: ['Vestidos', 'Saias'] })
      );
      expect(catalogServiceMock.applySort).toHaveBeenCalledWith('price-asc');
      expect(catalogServiceMock.loadProducts).toHaveBeenCalled();
    });

    it('should deserialize size and color query params on init', () => {
      activatedRouteMock.snapshot.queryParams = { size: 'P,M', color: 'Rosa,Preto' };

      fixture.detectChanges();

      expect(catalogServiceMock.applyFilter).toHaveBeenCalledWith(
        expect.objectContaining({
          sizes: ['P', 'M'],
          colors: ['Rosa', 'Preto'],
        })
      );
    });

    it('should deserialize price range query params on init', () => {
      activatedRouteMock.snapshot.queryParams = { priceMin: '50', priceMax: '200' };

      fixture.detectChanges();

      expect(catalogServiceMock.applyFilter).toHaveBeenCalledWith(
        expect.objectContaining({
          priceRange: { min: 50, max: 200 },
        })
      );
    });
  });

  /**
   * Validates: Requirements 2.3, 2.4
   */
  describe('Filter/sort sync to URL', () => {
    it('should call applyFilter on service and sync query params on filter change', () => {
      fixture.detectChanges();

      const newFilters: FilterState = {
        categories: ['Vestidos'],
        sizes: ['M'],
        colors: [],
        priceRange: null,
      };

      component.onFilterChange(newFilters);

      expect(catalogServiceMock.applyFilter).toHaveBeenCalledWith(newFilters);
      expect(routerMock.navigate).toHaveBeenCalledWith([], expect.objectContaining({
        queryParams: expect.any(Object),
        replaceUrl: true,
      }));
    });

    it('should call applySort on service and sync query params on sort change', () => {
      fixture.detectChanges();

      component.onSortChange('price-desc');

      expect(catalogServiceMock.applySort).toHaveBeenCalledWith('price-desc');
      expect(routerMock.navigate).toHaveBeenCalledWith([], expect.objectContaining({
        queryParams: expect.any(Object),
        replaceUrl: true,
      }));
    });

    it('should pass relativeTo route when syncing query params', () => {
      fixture.detectChanges();

      component.onSortChange('popularity');

      expect(routerMock.navigate).toHaveBeenCalledWith([], expect.objectContaining({
        relativeTo: activatedRouteMock,
      }));
    });
  });

  /**
   * Validates: Requirements 10.3
   */
  describe('Error state display', () => {
    it('should render error message when error signal is set', () => {
      catalogServiceMock.error.set('Ocorreu um erro. Tente novamente.');
      fixture.detectChanges();

      const errorEl = fixture.nativeElement.querySelector('.catalog-page__error');
      const messageEl = fixture.nativeElement.querySelector('.catalog-page__error-message');

      expect(errorEl).not.toBeNull();
      expect(messageEl.textContent).toContain('Ocorreu um erro. Tente novamente.');
    });

    it('should render retry button in error state', () => {
      catalogServiceMock.error.set('Erro de rede');
      fixture.detectChanges();

      const retryBtn = fixture.nativeElement.querySelector('.catalog-page__retry-btn');
      expect(retryBtn).not.toBeNull();
      expect(retryBtn.textContent).toContain('Tentar novamente');
    });

    it('should have role="alert" on error container for accessibility', () => {
      catalogServiceMock.error.set('Erro');
      fixture.detectChanges();

      const errorEl = fixture.nativeElement.querySelector('.catalog-page__error');
      expect(errorEl.getAttribute('role')).toBe('alert');
    });

    it('should not render product grid or empty state when error is shown', () => {
      catalogServiceMock.error.set('Erro');
      fixture.detectChanges();

      const grid = fixture.nativeElement.querySelector('app-product-grid');
      const empty = fixture.nativeElement.querySelector('.catalog-page__empty');

      expect(grid).toBeNull();
      expect(empty).toBeNull();
    });
  });

  /**
   * Validates: Requirements 10.3
   */
  describe('Empty state display', () => {
    it('should render empty message when products is empty and not loading', () => {
      catalogServiceMock.paginatedProducts.set([]);
      catalogServiceMock.loading.set(false);
      catalogServiceMock.error.set(null);
      fixture.detectChanges();

      const emptyEl = fixture.nativeElement.querySelector('.catalog-page__empty');
      const messageEl = fixture.nativeElement.querySelector('.catalog-page__empty-message');

      expect(emptyEl).not.toBeNull();
      expect(messageEl.textContent).toContain('Nenhum produto encontrado');
    });

    it('should render hint text in empty state', () => {
      catalogServiceMock.paginatedProducts.set([]);
      catalogServiceMock.loading.set(false);
      catalogServiceMock.error.set(null);
      fixture.detectChanges();

      const hintEl = fixture.nativeElement.querySelector('.catalog-page__empty-hint');
      expect(hintEl.textContent).toContain('Tente ajustar os filtros ou buscar por outro termo.');
    });

    it('should not render empty state while loading', () => {
      catalogServiceMock.paginatedProducts.set([]);
      catalogServiceMock.loading.set(true);
      catalogServiceMock.error.set(null);
      fixture.detectChanges();

      const emptyEl = fixture.nativeElement.querySelector('.catalog-page__empty');
      expect(emptyEl).toBeNull();
    });

    it('should not render empty state when products are available', () => {
      catalogServiceMock.paginatedProducts.set(createMockProducts(2));
      catalogServiceMock.loading.set(false);
      catalogServiceMock.error.set(null);
      fixture.detectChanges();

      const emptyEl = fixture.nativeElement.querySelector('.catalog-page__empty');
      expect(emptyEl).toBeNull();
    });
  });

  /**
   * Validates: Requirements 10.3
   */
  describe('Retry', () => {
    it('should call catalogService.retry() when retry button is clicked', () => {
      catalogServiceMock.error.set('Erro');
      fixture.detectChanges();

      const retryBtn: HTMLButtonElement = fixture.nativeElement.querySelector('.catalog-page__retry-btn');
      retryBtn.click();

      expect(catalogServiceMock.retry).toHaveBeenCalled();
    });

    it('should call catalogService.retry() when retry method is called directly', () => {
      component.retry();

      expect(catalogServiceMock.retry).toHaveBeenCalled();
    });
  });
});
