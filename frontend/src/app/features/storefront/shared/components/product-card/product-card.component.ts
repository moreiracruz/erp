import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';

import { ProductSummary } from '../../../../../core/models';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CurrencyPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './product-card.component.html',
  styleUrls: ['./product-card.component.scss'],
})
export class ProductCardComponent {
  readonly product = input.required<ProductSummary>();
  readonly quickView = output<ProductSummary>();
  readonly navigate = output<string>();

  protected readonly imageLoaded = signal(false);
  protected readonly showOverlay = signal(false);

  onCardClick(): void {
    this.navigate.emit(this.product().uuid);
  }

  onMouseEnter(): void {
    this.showOverlay.set(true);
  }

  onMouseLeave(): void {
    this.showOverlay.set(false);
  }

  onQuickView(event: Event): void {
    event.stopPropagation();
    this.quickView.emit(this.product());
  }

  onImageLoad(): void {
    this.imageLoaded.set(true);
  }
}
