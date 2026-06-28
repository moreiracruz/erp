import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface StockResponse {
  varianteUuid: string;
  physicalStock: number;
  reservedStock: number;
  availableStock: number;
}

export interface StockOperationCommand {
  quantity: number;
  actorUuid: string;
}

export interface InventoryMovement {
  uuid: string;
  operationType: string;
  quantity: number;
  occurredAt: string;
  actorUuid: string | null;
}

@Injectable({ providedIn: 'root' })
export class InventoryHttpAdapter {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/inventory`;

  constructor(private readonly http: HttpClient) {}

  getStock(variantUuid: string): Observable<StockResponse> {
    return this.http.get<StockResponse>(`${this.baseUrl}/variants/${variantUuid}/stock`);
  }

  registerEntry(variantUuid: string, command: StockOperationCommand): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/variants/${variantUuid}/entries`, command);
  }

  registerWithdrawal(variantUuid: string, command: StockOperationCommand): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/variants/${variantUuid}/withdrawals`, command);
  }

  listMovements(variantUuid: string): Observable<InventoryMovement[]> {
    return this.http.get<InventoryMovement[]>(`${this.baseUrl}/variants/${variantUuid}/movements`);
  }
}
