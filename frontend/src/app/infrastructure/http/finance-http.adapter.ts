import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface FinanceEntryResponse {
  uuid: string;
  type: string;
  amount: number | string;
  paymentMethod: string | null;
  description: string;
  category: string | null;
  competenceDate: string;
}

export interface RegisterExpenseCommand {
  amount: number;
  paymentMethod: string;
  description: string;
  category: string | null;
  competenceDate: string;
  actorUuid: string;
}

export interface CashFlowReport {
  from: string;
  to: string;
  totalRevenue: number | string;
  totalExpense: number | string;
  balance: number | string;
  entries: FinanceEntryResponse[];
}

@Injectable({ providedIn: 'root' })
export class FinanceHttpAdapter {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/finance`;

  constructor(private readonly http: HttpClient) {}

  registerExpense(command: RegisterExpenseCommand): Observable<FinanceEntryResponse> {
    return this.http.post<FinanceEntryResponse>(`${this.baseUrl}/expenses`, command);
  }

  getCashFlow(from: string, to: string): Observable<CashFlowReport> {
    const params = new HttpParams().set('from', from).set('to', to);
    return this.http.get<CashFlowReport>(`${this.baseUrl}/cash-flow`, { params });
  }

  getEntry(uuid: string): Observable<FinanceEntryResponse> {
    return this.http.get<FinanceEntryResponse>(`${this.baseUrl}/entries/${uuid}`);
  }
}
