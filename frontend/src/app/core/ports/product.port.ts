import { Observable } from 'rxjs';
import { Product, ProductSummary } from '../models';

/**
 * Port defining product catalog operations.
 */
export abstract class ProductPort {
  abstract getAll(): Observable<ProductSummary[]>;
  abstract getByUuid(uuid: string): Observable<Product>;
  abstract search(query: string): Observable<ProductSummary[]>;
  abstract getByCategory(category: string): Observable<ProductSummary[]>;
}
