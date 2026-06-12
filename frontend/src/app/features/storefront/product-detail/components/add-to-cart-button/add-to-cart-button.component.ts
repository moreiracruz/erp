import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-add-to-cart-button',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './add-to-cart-button.component.html',
  styleUrls: ['./add-to-cart-button.component.scss'],
})
export class AddToCartButtonComponent {
  readonly disabled = input<boolean>(false);
  readonly loading = input<boolean>(false);
  readonly addToCart = output<void>();

  onClick(): void {
    if (!this.disabled() && !this.loading()) {
      this.addToCart.emit();
    }
  }
}
