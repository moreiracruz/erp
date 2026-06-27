import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ImagePort } from '../../core/ports';
import { ProductImage } from '../../core/models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ImageHttpAdapter extends ImagePort {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/products`;

  constructor(private http: HttpClient) {
    super();
  }

  listByProduct(uuid: string): Observable<ProductImage[]> {
    return this.http.get<ProductImage[]>(`${this.baseUrl}/${uuid}/images`);
  }

  upload(uuid: string, files: File[]): Observable<ProductImage[]> {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    return this.http.post<ProductImage[]>(`${this.baseUrl}/${uuid}/images`, formData);
  }

  delete(uuid: string, imageId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${uuid}/images/${imageId}`);
  }

  reorder(uuid: string, imageIds: number[]): Observable<ProductImage[]> {
    return this.http.put<ProductImage[]>(`${this.baseUrl}/${uuid}/images/reorder`, { imageIds });
  }

  setMain(uuid: string, imageId: number): Observable<ProductImage> {
    return this.http.put<ProductImage>(`${this.baseUrl}/${uuid}/images/${imageId}/main`, {});
  }
}
