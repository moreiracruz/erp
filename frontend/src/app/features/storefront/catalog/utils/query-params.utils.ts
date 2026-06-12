import { Params } from '@angular/router';
import { FilterState, DEFAULT_FILTERS, SortOption } from '../models';

/**
 * Serializes the current FilterState and SortOption into URL-friendly query parameters.
 * Omits keys whose values are empty or equal to defaults.
 */
export function serializeFilters(filters: FilterState, sort: SortOption): Params {
  const params: Params = {};

  if (filters.categories.length > 0) {
    params['cat'] = filters.categories.join(',');
  }

  if (filters.sizes.length > 0) {
    params['size'] = filters.sizes.join(',');
  }

  if (filters.colors.length > 0) {
    params['color'] = filters.colors.join(',');
  }

  if (filters.priceRange !== null) {
    params['priceMin'] = String(filters.priceRange.min);
    params['priceMax'] = String(filters.priceRange.max);
  }

  if (sort !== 'newest') {
    params['sort'] = sort;
  }

  return params;
}

/**
 * Deserializes URL query parameters back into a FilterState and SortOption.
 * Handles missing or invalid params gracefully by falling back to defaults.
 */
export function deserializeFilters(params: Params): { filters: FilterState; sort: SortOption } {
  const categories = params['cat']
    ? params['cat'].split(',').filter((v: string) => v.length > 0)
    : [];

  const sizes = params['size']
    ? params['size'].split(',').filter((v: string) => v.length > 0)
    : [];

  const colors = params['color']
    ? params['color'].split(',').filter((v: string) => v.length > 0)
    : [];

  let priceRange: { min: number; max: number } | null = null;
  const priceMin = parseFloat(params['priceMin']);
  const priceMax = parseFloat(params['priceMax']);
  if (!isNaN(priceMin) && !isNaN(priceMax) && priceMin <= priceMax) {
    priceRange = { min: priceMin, max: priceMax };
  }

  const validSortOptions: SortOption[] = ['newest', 'price-asc', 'price-desc', 'popularity'];
  const sort: SortOption = validSortOptions.includes(params['sort'] as SortOption)
    ? (params['sort'] as SortOption)
    : 'newest';

  return {
    filters: { categories, sizes, colors, priceRange },
    sort,
  };
}
