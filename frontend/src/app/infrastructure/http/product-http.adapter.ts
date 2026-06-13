import { Injectable } from '@angular/core';
import { Observable, of, delay } from 'rxjs';
import { ProductPort } from '../../core/ports';
import { Product, ProductSummary } from '../../core/models';

@Injectable({ providedIn: 'root' })
export class ProductHttpAdapter extends ProductPort {

  private readonly mockProducts: ProductSummary[] = [
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000001', name: 'Vestido Floral Primavera', brand: 'Reino & Flor', category: 'Vestidos', minPrice: 289.90, maxPrice: 299.90, imageUrl: 'assets/images/vestido_floral_primavera.png' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000002', name: 'Blusa Seda Natural', brand: 'Reino & Flor', category: 'Blusas', minPrice: 189.90, maxPrice: 189.90, imageUrl: 'assets/images/blusa_seda_natural.png' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000003', name: 'Saia Midi Linho', brand: 'Reino & Flor', category: 'Saias', minPrice: 219.90, maxPrice: 229.90, imageUrl: 'assets/images/saia_midi_linho.png' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000004', name: 'Vestido Renda Dourada', brand: 'Reino & Flor', category: 'Vestidos', minPrice: 349.90, maxPrice: 349.90, imageUrl: 'assets/images/vestido_renda_dourada.png' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000005', name: 'Conjunto Elegance', brand: 'Reino & Flor', category: 'Conjuntos', minPrice: 459.90, maxPrice: 479.90 },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000006', name: 'Blusa Bordada Artesanal', brand: 'Reino & Flor', category: 'Blusas', minPrice: 159.90, maxPrice: 159.90, imageUrl: 'assets/images/blusa_bordada_artesanal.png' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000007', name: 'Calça Pantalona Linho', brand: 'Reino & Flor', category: 'Calças', minPrice: 249.90, maxPrice: 249.90 },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000008', name: 'Jaqueta Couro Eco', brand: 'Parceira', category: 'Jaquetas', minPrice: 399.90, maxPrice: 399.90, imageUrl: 'assets/images/jaqueta_couro_eco.png' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000009', name: 'Shorts Alfaiataria', brand: 'Reino & Flor', category: 'Shorts', minPrice: 149.90, maxPrice: 149.90, imageUrl: 'assets/images/shorts_alfaiataria.png' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000010', name: 'Camiseta Basic Algodão', brand: 'Reino & Flor', category: 'Camisetas', minPrice: 79.90, maxPrice: 79.90, imageUrl: 'assets/images/camiseta_basic_algodao.png' },
  ];

  getAll(): Observable<ProductSummary[]> {
    return of(this.mockProducts).pipe(delay(300));
  }

  getByUuid(uuid: string): Observable<Product> {
    const summary = this.mockProducts.find(p => p.uuid === uuid);
    if (!summary) {
      return new Observable(sub => sub.error({ status: 404 }));
    }
    const product: Product = {
      uuid: summary.uuid,
      name: summary.name,
      brand: summary.brand,
      category: summary.category,
      active: true,
      variants: [
        { uuid: crypto.randomUUID(), sku: 'SKU-P', size: 'P', color: 'Rosa', barcode: '789100001', price: summary.minPrice, cost: summary.minPrice * 0.4, active: true },
        { uuid: crypto.randomUUID(), sku: 'SKU-M', size: 'M', color: 'Rosa', barcode: '789100002', price: summary.minPrice, cost: summary.minPrice * 0.4, active: true },
        { uuid: crypto.randomUUID(), sku: 'SKU-G', size: 'G', color: 'Bege', barcode: '789100003', price: summary.maxPrice, cost: summary.maxPrice * 0.4, active: true },
        { uuid: crypto.randomUUID(), sku: 'SKU-M2', size: 'M', color: 'Preto', barcode: '789100004', price: summary.maxPrice, cost: summary.maxPrice * 0.4, active: true },
      ],
      createdAt: new Date().toISOString(),
    };
    return of(product).pipe(delay(200));
  }

  search(query: string): Observable<ProductSummary[]> {
    const results = this.mockProducts.filter(p => p.name.toLowerCase().includes(query.toLowerCase()));
    return of(results).pipe(delay(200));
  }

  getByCategory(category: string): Observable<ProductSummary[]> {
    const results = this.mockProducts.filter(p => p.category.toLowerCase() === category.toLowerCase());
    return of(results).pipe(delay(200));
  }
}
