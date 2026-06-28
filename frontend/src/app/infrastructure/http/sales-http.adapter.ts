import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type PosPaymentMethod = 'PIX' | 'DINHEIRO' | 'CREDITO';

export interface ProductVariantLookup {
  uuid: string;
  produtoUuid: string;
  sku: string;
  size: string;
  color: string;
  barcode: string;
  price: number | string;
  cost: number | string;
  active: boolean;
}

export interface SaleItemResponse {
  varianteUuid: string;
  sku: string;
  quantity: number;
  unitPrice: number | string;
  lineTotal: number | string;
}

export interface SaleResponse {
  uuid: string;
  operatorUuid: string;
  terminalId: string;
  clienteUuid: string | null;
  status: string;
  paymentMethod: string | null;
  subtotal: number | string;
  discountAmount: number | string;
  taxAmount: number | string;
  total: number | string;
  changeAmount: number | string;
  items: SaleItemResponse[];
  createdAt: string;
}

export interface FinalizationResponse {
  uuid: string;
  subtotal: number | string;
  discountAmount: number | string;
  taxAmount: number | string;
  total: number | string;
  changeAmount: number | string;
  paymentMethod: string;
}

@Injectable({ providedIn: 'root' })
export class SalesHttpAdapter {
  private readonly productsUrl = `${environment.apiUrl}/api/v1/products`;
  private readonly salesUrl = `${environment.apiUrl}/api/v1/sales`;

  constructor(private readonly http: HttpClient) {}

  findVariantByBarcode(barcode: string): Observable<ProductVariantLookup> {
    return this.http.get<ProductVariantLookup>(
      `${this.productsUrl}/variants/by-barcode/${encodeURIComponent(barcode)}`,
    );
  }

  openSale(terminalId: string): Observable<SaleResponse> {
    return this.http.post<SaleResponse>(this.salesUrl, { terminalId, clienteUuid: null });
  }

  addItem(saleUuid: string, barcode: string, quantity: number): Observable<SaleResponse> {
    return this.http.post<SaleResponse>(`${this.salesUrl}/${saleUuid}/items`, { barcode, quantity });
  }

  finalizeSale(
    saleUuid: string,
    paymentMethod: PosPaymentMethod,
    amountPaid: number,
    expectedTotal: number,
  ): Observable<FinalizationResponse> {
    return this.http.post<FinalizationResponse>(`${this.salesUrl}/${saleUuid}/finalize`, {
      paymentMethod,
      amountPaid,
      couponCode: null,
      expectedTotal,
    });
  }

  cancelSale(saleUuid: string, reason: string): Observable<void> {
    return this.http.post<void>(`${this.salesUrl}/${saleUuid}/cancel`, { reason });
  }
}
