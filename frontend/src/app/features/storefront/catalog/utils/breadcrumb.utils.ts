import { Product } from '../../../../core/models';
import { BreadcrumbSegment } from '../models';

/**
 * Builds breadcrumb segments based on the current context:
 *
 * - PDP (product provided): [Home(/), Category(/catalog?cat=X), ProductName(null)]
 * - Catalog with category filter: [Home(/), Category(null)]
 * - Catalog without category: [Home(null)]
 */
export function buildBreadcrumbs(product?: Product, category?: string): BreadcrumbSegment[] {
  if (product) {
    // Product Detail Page
    return [
      { label: 'Home', path: '/' },
      { label: product.category, path: `/catalog?cat=${encodeURIComponent(product.category)}` },
      { label: product.name, path: null },
    ];
  }

  if (category) {
    // Catalog page with active category filter
    return [
      { label: 'Home', path: '/' },
      { label: category, path: null },
    ];
  }

  // Catalog page without category filter
  return [{ label: 'Home', path: null }];
}
