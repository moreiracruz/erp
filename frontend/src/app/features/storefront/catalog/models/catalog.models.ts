import { Variant } from '../../../../core/models';

/**
 * State representing active filter selections on the catalog page.
 */
export interface FilterState {
  categories: string[];
  sizes: string[];
  colors: string[];
  priceRange: { min: number; max: number } | null;
}

/**
 * Default (empty) filter state — no filters applied.
 */
export const DEFAULT_FILTERS: FilterState = {
  categories: [],
  sizes: [],
  colors: [],
  priceRange: null,
};

/**
 * Available sorting options for the catalog grid.
 */
export type SortOption = 'newest' | 'price-asc' | 'price-desc' | 'popularity';

/**
 * Counts of matching products per filter option value.
 */
export interface FilterCounts {
  categories: Record<string, number>;
  sizes: Record<string, number>;
  colors: Record<string, number>;
}

/**
 * Currently selected variant attributes on the PDP.
 */
export interface SelectedVariant {
  size: string | null;
  color: string | null;
  variant: Variant | null;
}

/**
 * A single segment in the breadcrumb navigation trail.
 */
export interface BreadcrumbSegment {
  label: string;
  path: string | null;
}

/**
 * A row in the size guide measurement table.
 */
export interface SizeGuideEntry {
  size: string;
  bust: string;
  waist: string;
  hips: string;
}

/**
 * A product suggestion returned during search autocomplete.
 */
export interface SearchSuggestion {
  uuid: string;
  name: string;
  category: string;
  imageUrl?: string;
  minPrice: number;
}

/**
 * An item stored in the shopping cart.
 */
export interface CartItem {
  productUuid: string;
  variantUuid: string;
  productName: string;
  size: string;
  color: string;
  price: number;
  quantity: number;
  imageUrl?: string;
}
