import { computed, Injectable, signal } from '@angular/core';
import { catchError, EMPTY, finalize, Observable, tap } from 'rxjs';

import { Product, ProductSummary } from '../../../../core/models';
import { ProductPort } from '../../../../core/ports/product.port';
import {
  DEFAULT_FILTERS,
  FilterState,
  SortOption,
  CACHE_DURATION_MS,
  PAGE_SIZE,
} from '../models';
import { filterProducts, sortProducts, shouldTriggerSearch } from '../utils';

@Injectable({ providedIn: 'root' })
export class CatalogService {
  // State signals
  readonly products = signal<ProductSummary[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly filters = signal<FilterState>(DEFAULT_FILTERS);
  readonly sort = signal<SortOption>('newest');
  readonly page = signal<number>(0);
  readonly searchResults = signal<ProductSummary[]>([]);

  // Computed signals
  readonly filteredProducts = computed<ProductSummary[]>(() =>
    sortProducts(filterProducts(this.products(), this.filters()), this.sort())
  );

  readonly paginatedProducts = computed<ProductSummary[]>(() =>
    this.filteredProducts().slice(0, (this.page() + 1) * PAGE_SIZE)
  );

  readonly hasMore = computed<boolean>(
    () => this.paginatedProducts().length < this.filteredProducts().length
  );

  // Cache
  private cachedAt = 0;

  constructor(private readonly productPort: ProductPort) {}

  loadProducts(): void {
    if (Date.now() - this.cachedAt < CACHE_DURATION_MS && this.products().length > 0) {
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.productPort
      .getAll()
      .pipe(
        tap((products) => {
          this.products.set(products);
          this.cachedAt = Date.now();
        }),
        catchError((err) => {
          this.error.set(this.mapError(err));
          return EMPTY;
        }),
        finalize(() => this.loading.set(false))
      )
      .subscribe();
  }

  applyFilter(filters: FilterState): void {
    this.filters.set(filters);
    this.page.set(0);
  }

  applySort(sort: SortOption): void {
    this.sort.set(sort);
    this.page.set(0);
  }

  loadNextPage(): void {
    this.page.update((p) => p + 1);
  }

  search(query: string): void {
    if (!shouldTriggerSearch(query)) {
      this.searchResults.set([]);
      return;
    }

    this.productPort
      .search(query)
      .pipe(
        tap((results) => this.searchResults.set(results)),
        catchError(() => {
          this.searchResults.set([]);
          return EMPTY;
        })
      )
      .subscribe();
  }

  getProductByUuid(uuid: string): Observable<Product> {
    return this.productPort.getByUuid(uuid);
  }

  retry(): void {
    this.cachedAt = 0;
    this.loadProducts();
  }

  private mapError(err: { status?: number }): string {
    if (err.status === 0) {
      return 'Não foi possível carregar. Verifique sua conexão.';
    }
    if (err.status === 404) {
      return 'Produto não encontrado.';
    }
    return 'Ocorreu um erro. Tente novamente.';
  }
}
