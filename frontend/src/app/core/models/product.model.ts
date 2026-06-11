export interface Product {
  uuid: string;
  name: string;
  brand: string;
  category: string;
  active: boolean;
  variants: Variant[];
  createdAt: string;
}

export interface Variant {
  uuid: string;
  sku: string;
  size: string;
  color: string;
  barcode: string;
  price: number;
  cost: number;
  active: boolean;
}

export interface ProductSummary {
  uuid: string;
  name: string;
  brand: string;
  category: string;
  imageUrl?: string;
  minPrice: number;
  maxPrice: number;
}
