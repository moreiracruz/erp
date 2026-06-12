import { computed, Injectable, signal } from '@angular/core';

import { CartItem } from '../catalog/models';

@Injectable({ providedIn: 'root' })
export class CartService {
  readonly items = signal<CartItem[]>([]);

  readonly totalItems = computed(() =>
    this.items().reduce((sum, item) => sum + item.quantity, 0)
  );

  private adding = false;

  addItem(item: CartItem): void {
    if (this.adding) return;
    this.adding = true;

    try {
      const current = this.items();
      const existingIndex = current.findIndex((i) => i.variantUuid === item.variantUuid);

      if (existingIndex >= 0) {
        const updated = [...current];
        updated[existingIndex] = {
          ...updated[existingIndex],
          quantity: updated[existingIndex].quantity + item.quantity,
        };
        this.items.set(updated);
      } else {
        this.items.set([...current, item]);
      }
    } finally {
      this.adding = false;
    }
  }

  removeItem(variantUuid: string): void {
    this.items.update((items) => items.filter((i) => i.variantUuid !== variantUuid));
  }

  clear(): void {
    this.items.set([]);
  }
}
