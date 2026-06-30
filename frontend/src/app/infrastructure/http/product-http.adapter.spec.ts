import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { describe, afterEach, beforeEach, expect, it } from 'vitest';

import { environment } from '../../../environments/environment';
import { ProductHttpAdapter } from './product-http.adapter';

describe('ProductHttpAdapter', () => {
  let adapter: ProductHttpAdapter;
  let httpTesting: HttpTestingController;

  const catalogProduct = {
    uuid: 'a1b2c3d4-1111-4000-a000-000000000001',
    name: 'Vestido Floral Primavera',
    brand: 'Reino & Flor',
    category: 'Vestidos',
    active: true,
    minPrice: '289.90',
    maxPrice: '299.90',
    createdAt: '2026-01-01T10:00:00Z',
    variants: [
      {
        uuid: 'b1b2c3d4-1111-4000-a000-000000000001',
        sku: 'VFP-M-ROSA',
        size: 'M',
        color: 'Rosa',
        barcode: '7891000000102',
        price: '289.90',
        active: true,
      },
    ],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProductHttpAdapter,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    adapter = TestBed.inject(ProductHttpAdapter);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should load product summaries from the backend catalog endpoint', () => {
    let summaries: unknown;

    adapter.getAll().subscribe((result) => {
      summaries = result;
    });

    const request = httpTesting.expectOne(`${environment.apiUrl}/api/v1/products/catalog`);
    expect(request.request.method).toBe('GET');
    request.flush([catalogProduct]);

    expect(summaries).toEqual([
      {
        uuid: catalogProduct.uuid,
        name: catalogProduct.name,
        brand: catalogProduct.brand,
        category: catalogProduct.category,
        minPrice: 289.9,
        maxPrice: 299.9,
      },
    ]);
  });

  it('should load product detail with variants from the backend catalog endpoint', () => {
    let product: unknown;

    adapter.getByUuid(catalogProduct.uuid).subscribe((result) => {
      product = result;
    });

    const request = httpTesting.expectOne(
      `${environment.apiUrl}/api/v1/products/catalog/${catalogProduct.uuid}`
    );
    expect(request.request.method).toBe('GET');
    request.flush(catalogProduct);

    expect(product).toEqual({
      uuid: catalogProduct.uuid,
      name: catalogProduct.name,
      brand: catalogProduct.brand,
      category: catalogProduct.category,
      active: true,
      createdAt: catalogProduct.createdAt,
      variants: [
        {
          uuid: catalogProduct.variants[0].uuid,
          sku: 'VFP-M-ROSA',
          size: 'M',
          color: 'Rosa',
          barcode: '7891000000102',
          price: 289.9,
          cost: 0,
          active: true,
        },
      ],
    });
  });
});
