// Feature: storefront-catalog, Property 7: Breadcrumb segment correctness
import * as fc from 'fast-check';
import { buildBreadcrumbs } from './breadcrumb.utils';
import { Product } from '../../../../core/models';

/**
 * Validates: Requirements 5.4, 12.1, 12.2
 *
 * Property 7: Breadcrumb segment correctness
 * For any product with a non-empty category and name, the breadcrumb on PDP SHALL produce
 * exactly 3 segments: ["Home", product.category, product.name], and for any active category
 * filter on the Catalog Page, the breadcrumb SHALL produce exactly 2 segments: ["Home", activeCategory].
 */
describe('Property 7: Breadcrumb segment correctness', () => {
  const arbVariant = fc.record({
    uuid: fc.uuid(),
    sku: fc.string({ minLength: 1, maxLength: 10 }),
    size: fc.constantFrom('P', 'M', 'G', 'GG'),
    color: fc.constantFrom('Preto', 'Branco', 'Azul'),
    barcode: fc.string({ minLength: 8, maxLength: 13 }),
    price: fc.float({ min: 10, max: 500, noNaN: true }),
    cost: fc.float({ min: 5, max: 250, noNaN: true }),
    active: fc.boolean(),
  });

  const arbProduct: fc.Arbitrary<Product> = fc.record({
    uuid: fc.uuid(),
    name: fc.string({ minLength: 1, maxLength: 50 }),
    brand: fc.string({ minLength: 1, maxLength: 30 }),
    category: fc.string({ minLength: 1, maxLength: 30 }),
    active: fc.boolean(),
    variants: fc.array(arbVariant, { minLength: 0, maxLength: 5 }),
    createdAt: fc.date().map((d) => d.toISOString()),
  });

  it('PDP breadcrumbs produce exactly 3 segments with correct labels', () => {
    fc.assert(
      fc.property(arbProduct, (product) => {
        const segments = buildBreadcrumbs(product);

        // Exactly 3 segments
        expect(segments.length).toBe(3);

        // First segment: Home with path "/"
        expect(segments[0].label).toBe('Home');
        expect(segments[0].path).toBe('/');

        // Second segment: product category with catalog link
        expect(segments[1].label).toBe(product.category);
        expect(segments[1].path).toBe(`/catalog?cat=${encodeURIComponent(product.category)}`);

        // Third segment: product name with path null (current page)
        expect(segments[2].label).toBe(product.name);
        expect(segments[2].path).toBeNull();
      }),
      { numRuns: 100 }
    );
  });

  it('Catalog with category produces exactly 2 segments with correct labels', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 1, maxLength: 30 }),
        (category) => {
          const segments = buildBreadcrumbs(undefined, category);

          // Exactly 2 segments
          expect(segments.length).toBe(2);

          // First segment: Home with path "/"
          expect(segments[0].label).toBe('Home');
          expect(segments[0].path).toBe('/');

          // Second segment: category with path null (current page)
          expect(segments[1].label).toBe(category);
          expect(segments[1].path).toBeNull();
        }
      ),
      { numRuns: 100 }
    );
  });

  it('Catalog without category produces exactly 1 segment: Home with path null', () => {
    const segments = buildBreadcrumbs();

    expect(segments.length).toBe(1);
    expect(segments[0].label).toBe('Home');
    expect(segments[0].path).toBeNull();
  });
});
