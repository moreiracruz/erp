import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CurrencyPipe } from '@angular/common';

import { CartService } from '../../storefront/services/cart.service';

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [CurrencyPipe, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './cart-page.component.html',
  styleUrls: ['./cart-page.component.scss'],
})
export class CartPageComponent {
  private readonly cartService = inject(CartService);
  private readonly router = inject(Router);

  protected readonly items = this.cartService.items;
  protected readonly subtotal = this.cartService.subtotal;
  protected readonly isEmpty = computed(() => this.items().length === 0);

  increment(variantUuid: string): void {
    const item = this.items().find((i) => i.variantUuid === variantUuid);
    if (item) {
      this.cartService.updateQuantity(variantUuid, item.quantity + 1);
    }
  }

  decrement(variantUuid: string): void {
    const item = this.items().find((i) => i.variantUuid === variantUuid);
    if (item && item.quantity > 1) {
      this.cartService.updateQuantity(variantUuid, item.quantity - 1);
    }
  }

  remove(variantUuid: string): void {
    this.cartService.removeItem(variantUuid);
  }

  goToCheckout(): void {
    this.router.navigate(['/checkout']);
  }
}
