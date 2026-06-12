import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { BreadcrumbSegment, FilterCounts, FilterState, SortOption } from './models';
import { CatalogService } from './services/catalog.service';
import { computeFilterCounts, deserializeFilters, serializeFilters } from './utils';
import { BreadcrumbComponent } from '../shared/components/breadcrumb/breadcrumb.component';
import { FilterPanelComponent } from './components/filter-panel/filter-panel.component';
import { SortDropdownComponent } from './components/sort-dropdown/sort-dropdown.component';
import { ProductGridComponent } from './components/product-grid/product-grid.component';
import { SearchOverlayComponent } from './components/search-overlay/search-overlay.component';

@Component({
  selector: 'app-catalog-page',
  standalone: true,
  imports: [
    BreadcrumbComponent,
    FilterPanelComponent,
    SortDropdownComponent,
    ProductGridComponent,
    SearchOverlayComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './catalog-page.component.html',
  styleUrls: ['./catalog-page.component.scss'],
})
export class CatalogPageComponent implements OnInit {
  private readonly catalogService = inject(CatalogService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  // State
  protected readonly mobileFilterOpen = signal(false);
  protected readonly searchOpen = signal(false);

  // Delegated signals from service
  protected readonly products = this.catalogService.paginatedProducts;
  protected readonly loading = this.catalogService.loading;
  protected readonly error = this.catalogService.error;
  protected readonly hasMore = this.catalogService.hasMore;
  protected readonly filters = this.catalogService.filters;
  protected readonly sort = this.catalogService.sort;
  protected readonly searchResults = this.catalogService.searchResults;

  // Computed
  protected readonly filterCounts = computed<FilterCounts>(() =>
    computeFilterCounts(this.catalogService.products(), this.filters())
  );

  protected readonly breadcrumbs = computed<BreadcrumbSegment[]>(() => {
    const segments: BreadcrumbSegment[] = [{ label: 'Home', path: '/' }];
    const categories = this.filters().categories;

    if (categories.length === 1) {
      segments.push({ label: categories[0], path: null });
    } else {
      segments.push({ label: 'Catálogo', path: null });
    }

    return segments;
  });

  ngOnInit(): void {
    // Read query params and apply to service
    const params = this.route.snapshot.queryParams;
    const { filters, sort } = deserializeFilters(params);

    this.catalogService.applyFilter(filters);
    this.catalogService.applySort(sort);
    this.catalogService.loadProducts();
  }

  onFilterChange(filters: FilterState): void {
    this.catalogService.applyFilter(filters);
    this.syncQueryParams();
  }

  onSortChange(sort: SortOption): void {
    this.catalogService.applySort(sort);
    this.syncQueryParams();
  }

  onLoadMore(): void {
    this.catalogService.loadNextPage();
  }

  onProductNavigate(uuid: string): void {
    this.router.navigate(['/product', uuid]);
  }

  onSearch(query: string): void {
    this.catalogService.search(query);
  }

  onSearchClose(): void {
    this.searchOpen.set(false);
  }

  openSearch(): void {
    this.searchOpen.set(true);
  }

  toggleMobileFilter(): void {
    this.mobileFilterOpen.update((open) => !open);
  }

  retry(): void {
    this.catalogService.retry();
  }

  private syncQueryParams(): void {
    const params = serializeFilters(this.filters(), this.sort());
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: params,
      replaceUrl: true,
    });
  }
}
