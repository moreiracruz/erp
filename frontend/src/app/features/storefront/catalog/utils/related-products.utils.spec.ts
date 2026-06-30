// Feature: storefront-catalog, Property 8: Related products category constraint
import * as fc from 'fast-check';
import { ProductSummary } from '../../../../core/models';
import { getRelatedProducts } from './related-products.utils';

/**
 * Validates: Requirements 5.5
 *
 * Property 8: Related products category constraint
 * For any product and any catalog of products, the related products list SHALL contain only
 * products from the same category as the target product, SHALL exclude the target product
 * itself, and SHALL have a length of at most 4.
 */
describe('Property 8: Related products category constraint', () => {
  const arbProductSummary: fc.Arbitrary<ProductSummary> = fc.record({
    uuid: fc.uuid(),
    name: fc.string({ minLength: 1, maxLength: 50 }),
    brand: fc.string({ minLength: 1, maxLength: 30 }),
    category: fc.constantFrom('Vestidos', 'Blusas', 'Calças', 'Saias', 'Acessórios'),
    imageUrl: fc.option(fc.webUrl(), { nil: undefined }),
    minPrice: fc.float({ min: 10, max: 500, noNaN: true }),
    maxPrice: fc.float({ min: 500, max: 1000, noNaN: true }),
  });

  const arbCatalog = fc.array(arbProductSummary, { minLength: 0, maxLength: 20 });

  it('all related products have the same category as the current product', () => {
    fc.assert(
      fc.property(arbCatalog, arbProductSummary, (catalog, currentProduct) => {
        const catalogWithCurrent = [...catalog, currentProduct];
        const result = getRelatedProducts(
          catalogWithCurrent,
          currentProduct.uuid,
          currentProduct.category,
        );

        for (const related of result) {
          expect(related.category).toBe(currentProduct.category);
        }
      }),
      { numRuns: 100 },
    );
  });

  it('the current product is excluded from the results', () => {
    fc.assert(
      fc.property(arbCatalog, arbProductSummary, (catalog, currentProduct) => {
        const catalogWithCurrent = [...catalog, currentProduct];
        const result = getRelatedProducts(
          catalogWithCurrent,
          currentProduct.uuid,
          currentProduct.category,
        );

        const uuids = result.map((p) => p.uuid);
        expect(uuids).not.toContain(currentProduct.uuid);
      }),
      { numRuns: 100 },
    );
  });

  it('result length is at most 4 (the default limit)', () => {
    fc.assert(
      fc.property(arbCatalog, arbProductSummary, (catalog, currentProduct) => {
        const catalogWithCurrent = [...catalog, currentProduct];
        const result = getRelatedProducts(
          catalogWithCurrent,
          currentProduct.uuid,
          currentProduct.category,
        );

        expect(result.length).toBeLessThanOrEqual(4);
      }),
      { numRuns: 100 },
    );
  });
});
