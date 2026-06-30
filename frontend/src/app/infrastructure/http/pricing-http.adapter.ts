import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CampaignResponse {
  uuid: string;
  name: string;
  type: string;
  targetType: string;
  targetUuid: string | null;
  targetCategory: string | null;
  discountValue: number | string;
  cashbackPct: number | string;
  startsAt: string;
  endsAt: string;
  active: boolean;
}

export interface CouponResponse {
  uuid: string;
  code: string;
  discountType: string;
  discountValue: number | string;
  maxUses: number | null;
  usedCount: number;
  startsAt: string;
  endsAt: string;
  active: boolean;
}

export interface DiscountItemLine {
  varianteUuid: string;
  quantity: number;
  unitPrice: number;
}

export interface DiscountQuery {
  saleUuid: string;
  items: DiscountItemLine[];
  subtotal: number;
  couponCode: string | null;
}

export interface DiscountResult {
  discountAmount: number | string;
  appliedCampaignUuid: string | null;
  appliedCouponCode: string | null;
}

@Injectable({ providedIn: 'root' })
export class PricingHttpAdapter {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/pricing`;

  constructor(private readonly http: HttpClient) {}

  listCampaigns(): Observable<CampaignResponse[]> {
    return this.http.get<CampaignResponse[]>(`${this.baseUrl}/campaigns`);
  }

  createCampaign(command: unknown): Observable<CampaignResponse> {
    return this.http.post<CampaignResponse>(`${this.baseUrl}/campaigns`, command);
  }

  deactivateCampaign(uuid: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/campaigns/${uuid}/deactivate`, {});
  }

  createCoupon(command: unknown): Observable<CouponResponse> {
    return this.http.post<CouponResponse>(`${this.baseUrl}/coupons`, command);
  }

  calculateDiscount(query: DiscountQuery): Observable<DiscountResult> {
    return this.http.post<DiscountResult>(`${this.baseUrl}/calculate`, query);
  }

  confirmCouponUsage(code: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/coupons/${encodeURIComponent(code)}/confirm`, {});
  }
}
