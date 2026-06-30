import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import { environment } from '../../../environments/environment';
import { ImageHttpAdapter } from './image-http.adapter';

describe('ImageHttpAdapter', () => {
  let adapter: ImageHttpAdapter;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ImageHttpAdapter,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    adapter = TestBed.inject(ImageHttpAdapter);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should prefix backend-relative image URLs in development', () => {
    const productUuid = 'a1b2c3d4-1111-4000-a000-000000000001';
    let images: unknown;

    adapter.listByProduct(productUuid).subscribe((result) => {
      images = result;
    });

    const request = httpTesting.expectOne(`${environment.apiUrl}/api/v1/products/${productUuid}/images`);
    request.flush([
      {
        id: 1,
        filename: 'vestido.png',
        originalName: 'vestido.png',
        contentType: 'image/png',
        fileSize: 123,
        sortOrder: 0,
        main: true,
        createdAt: '2026-01-01T10:00:00Z',
        thumbnailUrl: '/uploads/products/product/vestido_thumb.png',
        cardUrl: '/uploads/products/product/vestido_card.png',
        fullUrl: '/uploads/products/product/vestido_full.png',
      },
    ]);

    expect(images).toEqual([
      {
        id: 1,
        filename: 'vestido.png',
        originalName: 'vestido.png',
        contentType: 'image/png',
        fileSize: 123,
        sortOrder: 0,
        main: true,
        createdAt: '2026-01-01T10:00:00Z',
        thumbnailUrl: `${environment.apiUrl}/uploads/products/product/vestido_thumb.png`,
        cardUrl: `${environment.apiUrl}/uploads/products/product/vestido_card.png`,
        fullUrl: `${environment.apiUrl}/uploads/products/product/vestido_full.png`,
      },
    ]);
  });
});
