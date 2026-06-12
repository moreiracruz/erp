import { computed, Injectable, signal } from '@angular/core';

import { CartItem } from '../catalog/models';

@Injectable({ providedIn: 'root' })
export class CartService {
  readonly items = signal<CartItem[]>([]);

  readonly totalItems = computed(() =>
    this.items().reduce((sum, item) => sum + item.quantity, 0)
  );

  readonly subtotal = computed(() =>
    this.items().reduce((sum, item) => sum + item.price * item.quantity, 0)
  );

  readonly shippingCost = computed(() =>
    this.subtotal() >= 299 ? 0 : 15.90
  );

  readonly total = computed(() =>
    this.subtotal() + this.shippingCost()
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

  updateQuantity(variantUuid: string, quantity: number): void {
    if (quantity <= 0) {
      this.removeItem(variantUuid);
      return;
    }

    this.items.update((items) =>
      items.map((item) =>
        item.variantUuid === variantUuid
          ? { ...item, quantity }
          : item
      )
    );
  }

  removeItem(variantUuid: string): void {
    this.items.update((items) => items.filter((i) => i.variantUuid !== variantUuid));
  }

  clear(): void {
    this.items.set([]);
  }
}
