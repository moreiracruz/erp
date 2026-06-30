import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CustomerResponse {
  uuid: string;
  fullName: string;
  cpf: string;
  email: string | null;
  phone: string | null;
  birthDate: string | null;
  active: boolean;
}

export interface CustomerCommand {
  fullName: string;
  cpf: string;
  email: string | null;
  phone: string | null;
  birthDate: string | null;
}

export interface CustomerSearchParams {
  cpf?: string;
  name?: string;
  uuid?: string;
  page?: number;
  size?: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({ providedIn: 'root' })
export class CustomersHttpAdapter {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/customers`;

  constructor(private readonly http: HttpClient) {}

  create(command: CustomerCommand): Observable<CustomerResponse> {
    return this.http.post<CustomerResponse>(this.baseUrl, command);
  }

  get(uuid: string): Observable<CustomerResponse> {
    return this.http.get<CustomerResponse>(`${this.baseUrl}/${uuid}`);
  }

  update(uuid: string, command: CustomerCommand): Observable<CustomerResponse> {
    return this.http.put<CustomerResponse>(`${this.baseUrl}/${uuid}`, command);
  }

  deactivate(uuid: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${uuid}/deactivate`);
  }

  search(params: CustomerSearchParams = {}): Observable<PageResponse<CustomerResponse>> {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });
    return this.http.get<PageResponse<CustomerResponse>>(`${this.baseUrl}/search`, { params: httpParams });
  }
}
