import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProductPort } from '../../core/ports';
import { Product, ProductSummary } from '../../core/models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProductHttpAdapter extends ProductPort {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/products`;

  constructor(private http: HttpClient) {
    super();
  }

  getAll(): Observable<ProductSummary[]> {
    return this.http.get<ProductSummary[]>(this.baseUrl);
  }

  getByUuid(uuid: string): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${uuid}`);
  }

  search(query: string): Observable<ProductSummary[]> {
    return this.http.get<ProductSummary[]>(this.baseUrl, {
      params: { q: query },
    });
  }

  getByCategory(category: string): Observable<ProductSummary[]> {
    return this.http.get<ProductSummary[]>(this.baseUrl, {
      params: { category },
    });
  }
}
