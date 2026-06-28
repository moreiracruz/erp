import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ConsignorResponse {
  uuid: string;
  name: string;
  document: string | null;
  email: string | null;
  phone: string | null;
  active: boolean;
  createdAt: string;
}

export interface ConsignorCommand {
  name: string;
  document: string | null;
  email: string | null;
  phone: string | null;
}

export interface ConsignedItemResponse {
  uuid: string;
  contratoUuid: string;
  varianteUuid: string;
  quantity: number;
  remainingQuantity: number;
  soldQuantity: number;
  settledQuantity: number;
  returnedQuantity: number;
  status: string;
  receivedAt: string;
  soldSaleUuid: string | null;
}

export interface ConsignmentContractResponse {
  uuid: string;
  consignorUuid: string;
  code: string;
  status: string;
  openedAt: string;
  closedAt: string | null;
  items: ConsignedItemResponse[];
}

export interface SettlementResponse {
  uuid: string;
  contratoUuid: string;
  totalAmount: number | string;
  notes: string | null;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class ConsignmentHttpAdapter {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/consignments`;

  constructor(private readonly http: HttpClient) {}

  createConsignor(command: ConsignorCommand): Observable<ConsignorResponse> {
    return this.http.post<ConsignorResponse>(`${this.baseUrl}/consignors`, command);
  }

  listConsignors(): Observable<ConsignorResponse[]> {
    return this.http.get<ConsignorResponse[]>(`${this.baseUrl}/consignors`);
  }

  updateConsignor(uuid: string, command: ConsignorCommand): Observable<ConsignorResponse> {
    return this.http.put<ConsignorResponse>(`${this.baseUrl}/consignors/${uuid}`, command);
  }

  deactivateConsignor(uuid: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/consignors/${uuid}/deactivate`);
  }

  openContract(consignorUuid: string, code: string): Observable<ConsignmentContractResponse> {
    return this.http.post<ConsignmentContractResponse>(`${this.baseUrl}/contracts`, { consignorUuid, code });
  }

  listContracts(status?: string, consignorUuid?: string): Observable<ConsignmentContractResponse[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    if (consignorUuid) params = params.set('consignorUuid', consignorUuid);
    return this.http.get<ConsignmentContractResponse[]>(`${this.baseUrl}/contracts`, { params });
  }

  getContract(uuid: string): Observable<ConsignmentContractResponse> {
    return this.http.get<ConsignmentContractResponse>(`${this.baseUrl}/contracts/${uuid}`);
  }

  receiveItems(contractUuid: string, items: Array<{ varianteUuid: string; quantity: number }>): Observable<ConsignmentContractResponse> {
    return this.http.post<ConsignmentContractResponse>(`${this.baseUrl}/contracts/${contractUuid}/items`, { items });
  }

  returnItems(contractUuid: string, items: Array<{ itemUuid: string; quantity: number }>): Observable<ConsignmentContractResponse> {
    return this.http.post<ConsignmentContractResponse>(`${this.baseUrl}/contracts/${contractUuid}/returns`, { items });
  }

  settle(
    contractUuid: string,
    notes: string | null,
    items: Array<{ itemUuid: string; quantity: number; manualAmount: number }>,
  ): Observable<SettlementResponse> {
    return this.http.post<SettlementResponse>(`${this.baseUrl}/contracts/${contractUuid}/settlements`, { notes, items });
  }

  closeContract(contractUuid: string): Observable<ConsignmentContractResponse> {
    return this.http.post<ConsignmentContractResponse>(`${this.baseUrl}/contracts/${contractUuid}/close`, {});
  }
}
