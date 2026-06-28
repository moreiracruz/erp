import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { ProductPort } from '../../core/ports';
import { Product, ProductSummary, Variant } from '../../core/models';
import { environment } from '../../../environments/environment';

interface CatalogVariantResponse {
  uuid: string;
  sku: string;
  size: string;
  color: string;
  barcode: string;
  price: number | string;
  active: boolean;
}

interface CatalogProductResponse {
  uuid: string;
  name: string;
  brand: string;
  category: string;
  active: boolean;
  variants: CatalogVariantResponse[];
  minPrice: number | string;
  maxPrice: number | string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class ProductHttpAdapter extends ProductPort {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/products/catalog`;

  constructor(private readonly http: HttpClient) {
    super();
  }

  getAll(): Observable<ProductSummary[]> {
    return this.http
      .get<CatalogProductResponse[]>(this.baseUrl)
      .pipe(map((products) => products.map((product) => this.toSummary(product))));
  }

  getByUuid(uuid: string): Observable<Product> {
    return this.http
      .get<CatalogProductResponse>(`${this.baseUrl}/${uuid}`)
      .pipe(map((product) => this.toProduct(product)));
  }

  search(query: string): Observable<ProductSummary[]> {
    const normalizedQuery = query.trim().toLowerCase();
    return this.getAll().pipe(
      map((products) => products.filter((product) =>
        product.name.toLowerCase().includes(normalizedQuery)
        || product.brand.toLowerCase().includes(normalizedQuery)
        || product.category.toLowerCase().includes(normalizedQuery)
      ))
    );
  }

  getByCategory(category: string): Observable<ProductSummary[]> {
    const normalizedCategory = category.trim().toLowerCase();
    return this.getAll().pipe(
      map((products) => products.filter((product) =>
        product.category.toLowerCase() === normalizedCategory
      ))
    );
  }

  private toSummary(product: CatalogProductResponse): ProductSummary {
    return {
      uuid: product.uuid,
      name: product.name,
      brand: product.brand,
      category: product.category,
      minPrice: this.toNumber(product.minPrice),
      maxPrice: this.toNumber(product.maxPrice),
    };
  }

  private toProduct(product: CatalogProductResponse): Product {
    return {
      uuid: product.uuid,
      name: product.name,
      brand: product.brand,
      category: product.category,
      active: product.active,
      variants: product.variants.map((variant) => this.toVariant(variant)),
      createdAt: product.createdAt,
    };
  }

  private toVariant(variant: CatalogVariantResponse): Variant {
    return {
      uuid: variant.uuid,
      sku: variant.sku,
      size: variant.size,
      color: variant.color,
      barcode: variant.barcode,
      price: this.toNumber(variant.price),
      cost: 0,
      active: variant.active,
    };
  }

  private toNumber(value: number | string): number {
    return typeof value === 'number' ? value : Number(value);
  }
}
