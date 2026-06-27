# Implementation Plan: Storefront Catalog

## Overview

Implement the Storefront Catalog feature for the "Reino & Flor" Angular frontend, consisting of a responsive product grid (Catalog Page) and a Product Detail Page (PDP). The implementation follows hexagonal architecture with signal-based state management, pure utility functions for filtering/sorting, and property-based testing with fast-check. All components use OnPush change detection and standalone component patterns.

## Tasks

- [x] 1. Set up feature models, utilities, and core infrastructure
  - [x] 1.1 Create feature-specific data models and constants
    - Create `features/storefront/catalog/models/catalog.models.ts` with `FilterState`, `DEFAULT_FILTERS`, `SortOption`, `FilterCounts`, `SelectedVariant`, `BreadcrumbSegment`, `SizeGuideEntry`, `SearchSuggestion`, `CartItem` interfaces
    - Create `features/storefront/catalog/models/catalog.constants.ts` with `PAGE_SIZE`, `SCROLL_THRESHOLD_PX`, `CACHE_DURATION_MS`, `SEARCH_DEBOUNCE_MS`, `MIN_SEARCH_LENGTH`
    - Export all models via barrel file `features/storefront/catalog/models/index.ts`
    - _Requirements: 2.1, 3.1, 4.1, 6.1, 6.2, 9.2_

  - [x] 1.2 Implement ProductHttpAdapter
    - Create `infrastructure/http/product-http.adapter.ts` implementing `ProductPort`
    - Implement `getAll()`, `getByUuid()`, `search()`, `getByCategory()` methods using `HttpClient`
    - Register provider `{ provide: ProductPort, useClass: ProductHttpAdapter }` in `app.config.ts`
    - _Requirements: 10.1, 10.2_

  - [x] 1.3 Implement pure utility functions for filtering
    - Create `features/storefront/catalog/utils/filter.utils.ts` with `filterProducts(products, filters)` and `computeFilterCounts(products, currentFilters)` pure functions
    - `filterProducts` applies AND semantics across all filter dimensions (category, size, color, priceRange)
    - `computeFilterCounts` computes per-option counts reflecting what would match if toggled
    - _Requirements: 2.2, 2.5, 2.6_

  - [x] 1.4 Implement pure utility functions for sorting
    - Create `features/storefront/catalog/utils/sort.utils.ts` with `sortProducts(products, sortOption)` pure function
    - Support `newest`, `price-asc`, `price-desc`, `popularity` options with stable ordering
    - _Requirements: 3.1, 3.2_

  - [x] 1.5 Implement query params serialization utilities
    - Create `features/storefront/catalog/utils/query-params.utils.ts` with `serializeFilters(state, sort)` and `deserializeFilters(params)` pure functions
    - Ensure round-trip consistency for all valid FilterState and SortOption values
    - _Requirements: 2.3, 2.4, 3.4_

  - [x] 1.6 Implement variant utility functions
    - Create `features/storefront/catalog/utils/variant.utils.ts` with `getAvailableSizes(variants)`, `getAvailableColors(variants)`, `getDisabledColors(variants, selectedSize)`, `getDisabledSizes(variants, selectedColor)`, `getVariantPrice(variants, size, color)`, `getDefaultVariant(variants)` pure functions
    - All functions operate on active variants only
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

  - [x] 1.7 Implement breadcrumb and related products utilities
    - Create `features/storefront/catalog/utils/breadcrumb.utils.ts` with `buildBreadcrumbs(product?, category?)` function
    - Create `features/storefront/catalog/utils/related-products.utils.ts` with `getRelatedProducts(catalog, product, limit)` function
    - Breadcrumbs: PDP = 3 segments [Home, category, name]; Catalog with active category = 2 segments [Home, category]
    - Related products: same category, exclude self, max 4
    - _Requirements: 5.4, 5.5, 12.1, 12.2_

  - [x] 1.8 Implement search utility
    - Create `features/storefront/catalog/utils/search.utils.ts` with `shouldTriggerSearch(input)` pure function
    - Returns true only if trimmed input length >= `MIN_SEARCH_LENGTH` (3)
    - _Requirements: 9.2_

- [x] 2. Checkpoint - Ensure utility functions compile and unit tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 3. Property-based tests for utility functions
  - [x] 3.1 Write property test for filter correctness (AND semantics)
    - **Property 1: Filter correctness (AND semantics)**
    - **Validates: Requirements 2.2**
    - Use fast-check to generate arbitrary product lists and filter states
    - Verify filtered output contains only products matching ALL criteria AND contains every matching product
    - File: `features/storefront/catalog/utils/filter.utils.spec.ts`

  - [x] 3.2 Write property test for query params round-trip
    - **Property 2: Query params round-trip**
    - **Validates: Requirements 2.3, 2.4, 3.4**
    - Use fast-check to generate arbitrary FilterState and SortOption
    - Verify `deserializeFilters(serializeFilters(state, sort))` produces equivalent state
    - File: `features/storefront/catalog/utils/query-params.utils.spec.ts`

  - [x] 3.3 Write property test for filter counts accuracy
    - **Property 3: Filter counts accuracy**
    - **Validates: Requirements 2.5**
    - Use fast-check to generate product lists and active filters
    - Verify counts match the number of products that would appear if each option were toggled
    - File: `features/storefront/catalog/utils/filter.utils.spec.ts`

  - [x] 3.4 Write property test for clear filters idempotence
    - **Property 4: Clear filters idempotence**
    - **Validates: Requirements 2.6**
    - Verify `filterProducts(products, DEFAULT_FILTERS)` returns the complete unfiltered list in original order
    - File: `features/storefront/catalog/utils/filter.utils.spec.ts`

  - [x] 3.5 Write property test for sort ordering invariant
    - **Property 5: Sort ordering invariant**
    - **Validates: Requirements 3.2**
    - Use fast-check to generate non-empty product lists and sort options
    - Verify adjacent pair ordering predicate holds for all pairs
    - File: `features/storefront/catalog/utils/sort.utils.spec.ts`

  - [x] 3.6 Write property test for variant attribute extraction
    - **Property 9: Variant attribute extraction**
    - **Validates: Requirements 6.1, 6.2**
    - Verify displayed sizes/colors equal distinct values from active variants
    - File: `features/storefront/catalog/utils/variant.utils.spec.ts`

  - [x] 3.7 Write property test for cross-dimension availability
    - **Property 10: Cross-dimension availability**
    - **Validates: Requirements 6.3, 6.4**
    - Verify a color is disabled iff no active variant with that size+color exists
    - File: `features/storefront/catalog/utils/variant.utils.spec.ts`

  - [x] 3.8 Write property test for selected variant price correctness
    - **Property 11: Selected variant price correctness**
    - **Validates: Requirements 6.5**
    - Verify displayed price equals the matching variant's price field
    - File: `features/storefront/catalog/utils/variant.utils.spec.ts`

  - [x] 3.9 Write property test for default variant selection
    - **Property 12: Default variant selection**
    - **Validates: Requirements 6.6**
    - Verify pre-selected size/color matches the first active variant
    - File: `features/storefront/catalog/utils/variant.utils.spec.ts`

  - [x] 3.10 Write property test for breadcrumb segment correctness
    - **Property 7: Breadcrumb segment correctness**
    - **Validates: Requirements 5.4, 12.1, 12.2**
    - Verify PDP breadcrumbs = 3 segments, catalog with category = 2 segments, correct labels
    - File: `features/storefront/catalog/utils/breadcrumb.utils.spec.ts`

  - [x] 3.11 Write property test for related products category constraint
    - **Property 8: Related products category constraint**
    - **Validates: Requirements 5.5**
    - Verify same category, excludes self, max 4 items
    - File: `features/storefront/catalog/utils/related-products.utils.spec.ts`

  - [x] 3.12 Write property test for search minimum length gate
    - **Property 13: Search minimum length gate**
    - **Validates: Requirements 9.2**
    - Verify search triggers iff trimmed length >= 3
    - File: `features/storefront/catalog/utils/search.utils.spec.ts`

- [x] 4. Implement CatalogService (facade with signals)
  - [x] 4.1 Create CatalogService with signal-based state management
    - Create `features/storefront/catalog/services/catalog.service.ts`
    - Implement signals: `products`, `loading`, `error`, `filters`, `sort`, `page`, `searchResults`
    - Implement computed signals: `filteredProducts`, `paginatedProducts`, `hasMore`
    - Implement methods: `loadProducts()`, `applyFilter()`, `applySort()`, `loadNextPage()`, `search()`, `getProductByUuid()`, `retry()`
    - Use pure utility functions for filtering/sorting within computed signals
    - Implement 5-minute cache for product list responses
    - Reset page to 0 on filter/sort change (Property 6)
    - Implement error mapping with user-friendly Portuguese messages
    - _Requirements: 2.2, 2.6, 3.2, 3.3, 4.1, 4.2, 4.5, 9.2, 10.1, 10.3, 10.4, 10.5_

  - [x] 4.2 Write unit tests for CatalogService
    - Test cache behavior (Property 14: Cache TTL behavior)
    - Test page reset on filter/sort change (Property 6)
    - Test error mapping
    - Test loading states
    - Mock ProductPort for isolation
    - File: `features/storefront/catalog/services/catalog.service.spec.ts`
    - **Property 6: Page reset on filter/sort change**
    - **Property 14: Cache TTL behavior**
    - **Validates: Requirements 4.5, 10.5**

- [x] 5. Implement presentational components (shared)
  - [x] 5.1 Implement ProductCardComponent
    - Create `features/storefront/shared/components/product-card/product-card.component.ts` (standalone, OnPush)
    - Input: `product: ProductSummary`; Outputs: `quickView`, `navigate`
    - Display product image (lazy loading), name, price range
    - Quick-view overlay on hover (desktop) showing sizes and secondary image
    - Shimmer placeholder while image loads, branded fallback on error
    - Include `aria-label` with product name and price for screen readers
    - Minimum 44x44px touch targets
    - _Requirements: 1.2, 1.3, 1.4, 11.1, 11.2, 11.3, 13.2, 13.7_

  - [x] 5.2 Implement ShimmerPlaceholderComponent
    - Create `features/storefront/shared/components/shimmer-placeholder/shimmer-placeholder.component.ts` (standalone, OnPush)
    - Inputs: `width`, `height`, `count`
    - Animated skeleton placeholder with BEM SCSS
    - _Requirements: 1.4, 5.6, 11.2_

  - [x] 5.3 Implement BreadcrumbComponent
    - Create `features/storefront/shared/components/breadcrumb/breadcrumb.component.ts` (standalone, OnPush)
    - Input: `segments: BreadcrumbSegment[]`
    - Render nav > ol with links; last segment is current page (no link)
    - Use `aria-label="Breadcrumb"` and `aria-current="page"` on last item
    - _Requirements: 5.4, 12.1, 12.2, 12.3, 13.1_

  - [x] 5.4 Write unit tests for ProductCardComponent
    - Test rendering of product data, hover overlay, image fallback, aria-label
    - File: `features/storefront/shared/components/product-card/product-card.component.spec.ts`
    - _Requirements: 1.2, 1.3, 11.3, 13.2_

- [x] 6. Implement catalog page components
  - [x] 6.1 Implement FilterPanelComponent
    - Create `features/storefront/catalog/components/filter-panel/filter-panel.component.ts` (standalone, OnPush)
    - Inputs: `filters: FilterState`, `counts: FilterCounts`; Output: `filterChange`
    - Collapsible accordion sections for category, size, color, price range
    - Display count badges per option
    - "Limpar filtros" (Clear filters) button
    - Full keyboard operability (Tab, Enter, Space)
    - Mobile: hidden by default, slides in as side panel on toggle
    - _Requirements: 2.1, 2.2, 2.5, 2.6, 13.3, 14.1, 14.2_

  - [x] 6.2 Implement SortDropdownComponent
    - Create `features/storefront/catalog/components/sort-dropdown/sort-dropdown.component.ts` (standalone, OnPush)
    - Input: `currentSort: SortOption`; Output: `sortChange`
    - Dropdown with options: Mais recentes, Preço: menor, Preço: maior, Popularidade
    - _Requirements: 3.1, 3.2_

  - [x] 6.3 Implement ProductGridComponent with infinite scroll
    - Create `features/storefront/catalog/components/product-grid/product-grid.component.ts` (standalone, OnPush)
    - Inputs: `products: ProductSummary[]`, `loading: boolean`, `hasMore: boolean`; Output: `loadMore`
    - Responsive grid: 2 cols (<768px), 3 cols (768-1024px), 4 cols (>1024px)
    - IntersectionObserver for infinite scroll trigger (200px threshold)
    - Loading indicator when appending, end-of-catalog message when no more
    - Use semantic HTML landmarks
    - _Requirements: 1.1, 4.1, 4.2, 4.3, 4.4, 13.1, 14.4_

  - [x] 6.4 Implement SearchOverlayComponent
    - Create `features/storefront/catalog/components/search-overlay/search-overlay.component.ts` (standalone, OnPush)
    - Inputs: `open: boolean`, `suggestions: ProductSummary[]`; Outputs: `search`, `close`
    - Focus input on open, debounced search (300ms, min 3 chars)
    - ARIA combobox pattern with `aria-expanded`, `aria-controls`, `aria-activedescendant`
    - Keyboard navigation: Arrow keys, Enter to select, Escape to dismiss
    - Dismissible by Escape or clicking outside
    - _Requirements: 9.1, 9.2, 9.5, 9.6, 13.5_

  - [x] 6.5 Implement CatalogPageComponent (smart/container)
    - Create `features/storefront/catalog/catalog-page.component.ts` (standalone, OnPush)
    - Inject CatalogService, ActivatedRoute, Router
    - Read query params on init, sync filters/sort to URL
    - Compose FilterPanel, SortDropdown, ProductGrid, SearchOverlay, Breadcrumb
    - Handle loading, error (with retry button), and empty states
    - Display error messages in Portuguese
    - _Requirements: 1.1, 1.4, 1.5, 2.3, 2.4, 3.3, 3.4, 4.5, 10.3, 12.1, 14.1_

  - [x] 6.6 Write unit tests for CatalogPageComponent
    - Test query param sync, filter/sort application, error state display, empty state
    - File: `features/storefront/catalog/catalog-page.component.spec.ts`
    - _Requirements: 1.5, 2.3, 2.4, 10.3_

- [x] 7. Checkpoint - Ensure catalog page compiles and basic tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Implement product detail page components
  - [x] 8.1 Implement ImageGalleryComponent
    - Create `features/storefront/product-detail/components/image-gallery/image-gallery.component.ts` (standalone, OnPush)
    - Inputs: `images: string[]`, `currentIndex: number`; Output: `imageSelect`
    - Primary image + thumbnail strip; update within 150ms on click
    - Preload next/previous images
    - Swipe gesture support on mobile (<768px)
    - Descriptive alt text per image, keyboard navigation with Arrow keys
    - _Requirements: 5.2, 5.3, 11.4, 13.6, 14.3_

  - [x] 8.2 Implement VariantSelectorComponent
    - Create `features/storefront/product-detail/components/variant-selector/variant-selector.component.ts` (standalone, OnPush)
    - Inputs: `variants: Variant[]`, `selected: SelectedVariant`; Output: `variantChange`
    - Display size buttons and color swatches
    - Use variant utility functions for disabled states
    - `aria-live` region for announcing variant changes
    - Minimum 44x44px touch targets
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 13.4, 13.7_

  - [x] 8.3 Implement SizeGuideModalComponent
    - Create `features/storefront/product-detail/components/size-guide-modal/size-guide-modal.component.ts` (standalone, OnPush)
    - Inputs: `sizes: SizeGuideEntry[]`, `open: boolean`; Output: `close`
    - Table with bust, waist, hips measurements per size
    - Dismiss via outside click, Escape key, or close button
    - Focus trap when open
    - _Requirements: 8.1, 8.2, 8.3_

  - [x] 8.4 Implement AddToCartButtonComponent
    - Create `features/storefront/product-detail/components/add-to-cart-button/add-to-cart-button.component.ts` (standalone, OnPush)
    - Inputs: `disabled: boolean`, `loading: boolean`; Output: `addToCart`
    - Text: "Adicionar ao Carrinho"
    - Fixed position at bottom on mobile (<768px)
    - Loading spinner and disable on submission
    - _Requirements: 7.1, 7.2, 7.5_

  - [x] 8.5 Implement RelatedProductsComponent
    - Create `features/storefront/product-detail/components/related-products/related-products.component.ts` (standalone, OnPush)
    - Input: `products: ProductSummary[]`
    - Render up to 4 ProductCards in a horizontal scroll/row
    - _Requirements: 5.5_

  - [x] 8.6 Implement ProductDetailPageComponent (smart/container)
    - Create `features/storefront/product-detail/product-detail-page.component.ts` (standalone, OnPush)
    - Inject CatalogService, ActivatedRoute, CartService
    - Read `:uuid` route param, load product via `getProductByUuid()`
    - Manage `selectedVariant` signal, default to first active variant on load
    - Compose ImageGallery, VariantSelector, SizeGuideModal, AddToCartButton, RelatedProducts, Breadcrumb
    - Handle loading (shimmer), error (retry + "Voltar ao catálogo" link for 404), and add-to-cart validation
    - _Requirements: 5.1, 5.2, 5.4, 5.5, 5.6, 6.5, 6.6, 7.1, 7.3, 7.4, 7.5, 10.4, 12.2_

  - [x] 8.7 Write unit tests for ProductDetailPageComponent
    - Test variant selection, default variant, price update, add-to-cart validation, error states
    - File: `features/storefront/product-detail/product-detail-page.component.spec.ts`
    - _Requirements: 6.5, 6.6, 7.3, 7.4, 10.4_

- [x] 9. Implement CartService and wire routing
  - [x] 9.1 Implement CartService
    - Create `features/storefront/services/cart.service.ts`
    - Signal-based cart state: `items: CartItem[]`, `totalItems` computed
    - Methods: `addItem(item: CartItem)`, `removeItem(variantUuid)`, `clear()`
    - Prevent duplicate submissions (guard with loading flag)
    - _Requirements: 7.4, 7.5_

  - [x] 9.2 Update storefront routes
    - Add catalog and product-detail routes to `storefront.routes.ts`
    - `{ path: 'catalog', loadComponent: () => ... CatalogPageComponent }`
    - `{ path: 'product/:uuid', loadComponent: () => ... ProductDetailPageComponent }`
    - Ensure lazy-loading of feature chunk
    - _Requirements: 10.2_

- [x] 10. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- All components use `ChangeDetectionStrategy.OnPush` and standalone pattern
- SCSS follows BEM naming with design tokens from `_variables.scss`
- All text strings are in Portuguese (pt-BR) as per the store's locale

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "5.2"] },
    { "id": 1, "tasks": ["1.3", "1.4", "1.5", "1.6", "1.7", "1.8"] },
    { "id": 2, "tasks": ["3.1", "3.2", "3.3", "3.4", "3.5", "3.6", "3.7", "3.8", "3.9", "3.10", "3.11", "3.12"] },
    { "id": 3, "tasks": ["4.1", "5.1", "5.3"] },
    { "id": 4, "tasks": ["4.2", "5.4", "6.1", "6.2", "6.3", "6.4"] },
    { "id": 5, "tasks": ["6.5", "8.1", "8.2", "8.3", "8.4", "8.5"] },
    { "id": 6, "tasks": ["6.6", "9.1", "9.2"] },
    { "id": 7, "tasks": ["8.6"] },
    { "id": 8, "tasks": ["8.7"] }
  ]
}
```
