import { Observable } from 'rxjs';
import { ProductImage } from '../models';

/**
 * Port defining product image management operations.
 */
export abstract class ImagePort {
  abstract listByProduct(uuid: string): Observable<ProductImage[]>;
  abstract upload(uuid: string, files: File[]): Observable<ProductImage[]>;
  abstract delete(uuid: string, imageId: number): Observable<void>;
  abstract reorder(uuid: string, imageIds: number[]): Observable<ProductImage[]>;
  abstract setMain(uuid: string, imageId: number): Observable<ProductImage>;
}
