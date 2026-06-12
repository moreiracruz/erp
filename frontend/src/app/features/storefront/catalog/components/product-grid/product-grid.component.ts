import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  inject,
  input,
  output,
  viewChild,
} from '@angular/core';

import { ProductSummary } from '../../../../../core/models';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';
import { ShimmerPlaceholderComponent } from '../../../shared/components/shimmer-placeholder/shimmer-placeholder.component';
import { SCROLL_THRESHOLD_PX } from '../../models';

@Component({
  selector: 'app-product-grid',
  standalone: true,
  imports: [ProductCardComponent, ShimmerPlaceholderComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './product-grid.component.html',
  styleUrls: ['./product-grid.component.scss'],
})
export class ProductGridComponent implements AfterViewInit {
  readonly products = input.required<ProductSummary[]>();
  readonly loading = input<boolean>(false);
  readonly hasMore = input<boolean>(true);

  readonly loadMore = output<void>();
  readonly productNavigate = output<string>();

  protected readonly sentinel = viewChild<ElementRef<HTMLDivElement>>('sentinel');

  private readonly destroyRef = inject(DestroyRef);
  private observer: IntersectionObserver | null = null;

  ngAfterViewInit(): void {
    this.setupIntersectionObserver();
  }

  onProductNavigate(uuid: string): void {
    this.productNavigate.emit(uuid);
  }

  private setupIntersectionObserver(): void {
    const sentinelEl = this.sentinel()?.nativeElement;
    if (!sentinelEl) return;

    this.observer = new IntersectionObserver(
      (entries) => {
        const entry = entries[0];
        if (entry.isIntersecting && !this.loading() && this.hasMore()) {
          this.loadMore.emit();
        }
      },
      { rootMargin: `${SCROLL_THRESHOLD_PX}px` }
    );

    this.observer.observe(sentinelEl);

    this.destroyRef.onDestroy(() => {
      this.observer?.disconnect();
    });
  }
}
