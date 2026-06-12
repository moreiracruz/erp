import { ChangeDetectionStrategy, Component, input } from '@angular/core';

import { ProductSummary } from '../../../../../core/models';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';

@Component({
  selector: 'app-related-products',
  standalone: true,
  imports: [ProductCardComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './related-products.component.html',
  styleUrls: ['./related-products.component.scss'],
})
export class RelatedProductsComponent {
  readonly products = input.required<ProductSummary[]>();
}
