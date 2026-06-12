import { ChangeDetectionStrategy, Component, computed, input, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';

import { CartItem } from '../../../storefront/catalog/models';

@Component({
  selector: 'app-order-summary',
  standalone: true,
  imports: [CurrencyPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './order-summary.component.html',
  styleUrls: ['./order-summary.component.scss'],
})
export class OrderSummaryComponent {
  readonly items = input.required<CartItem[]>();
  readonly subtotal = input.required<number>();
  readonly shippingCost = input.required<number>();
  readonly total = input.required<number>();

  protected readonly collapsed = signal(true);

  protected readonly itemCount = computed(() =>
    this.items().reduce((sum, item) => sum + item.quantity, 0)
  );

  toggleCollapse(): void {
    this.collapsed.update((v) => !v);
  }
}
