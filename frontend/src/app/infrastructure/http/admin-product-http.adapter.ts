import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AdminProduct {
  id: string;
  name: string;
  brand: string;
  category: string;
  price: number;
  cost: number;
  active: boolean;
}

export interface AdminProductCommand {
  name: string;
  brand: string;
  category: string;
}

interface ProdutoResponse {
  uuid: string;
  name: string;
  brand: string;
  category: string;
  active: boolean;
}

interface CatalogProductResponse extends ProdutoResponse {
  minPrice: number | string;
  maxPrice: number | string;
}

@Injectable({ providedIn: 'root' })
export class AdminProductHttpAdapter {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/products`;

  constructor(private readonly http: HttpClient) {}

  list(): Observable<AdminProduct[]> {
    return this.http
      .get<CatalogProductResponse[]>(`${this.baseUrl}/catalog`)
      .pipe(map((products) => products.map((product) => this.toAdminProduct(product))));
  }

  create(command: AdminProductCommand): Observable<AdminProduct> {
    return this.http
      .post<ProdutoResponse>(this.baseUrl, command)
      .pipe(map((product) => this.toAdminProduct(product)));
  }

  update(uuid: string, command: AdminProductCommand): Observable<AdminProduct> {
    return this.http
      .put<ProdutoResponse>(`${this.baseUrl}/${uuid}`, command)
      .pipe(map((product) => this.toAdminProduct(product)));
  }

  deactivate(uuid: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${uuid}/deactivate`);
  }

  private toAdminProduct(product: ProdutoResponse | CatalogProductResponse): AdminProduct {
    const price = 'minPrice' in product ? this.toNumber(product.minPrice) : 0;
    return {
      id: product.uuid,
      name: product.name,
      brand: product.brand,
      category: product.category,
      price,
      cost: 0,
      active: product.active,
    };
  }

  private toNumber(value: number | string): number {
    return typeof value === 'number' ? value : Number(value);
  }
}
