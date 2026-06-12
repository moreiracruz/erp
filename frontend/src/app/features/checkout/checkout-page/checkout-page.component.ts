import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { CartService } from '../../storefront/services/cart.service';
import { AuthService } from '../../auth/services/auth.service';
import { OrderSummaryComponent } from '../components/order-summary/order-summary.component';

export interface PersonalData {
  name: string;
  email: string;
  phone: string;
}

export interface AddressData {
  cep: string;
  street: string;
  number: string;
  complement: string;
  neighborhood: string;
  city: string;
  state: string;
}

export type PaymentMethod = 'pix' | 'credit' | 'debit' | 'cash';

@Component({
  selector: 'app-checkout-page',
  standalone: true,
  imports: [FormsModule, OrderSummaryComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './checkout-page.component.html',
  styleUrls: ['./checkout-page.component.scss'],
})
export class CheckoutPageComponent {
  private readonly cartService = inject(CartService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  // Cart data
  protected readonly items = this.cartService.items;
  protected readonly subtotal = this.cartService.subtotal;
  protected readonly shippingCost = this.cartService.shippingCost;
  protected readonly total = this.cartService.total;

  // Step management
  protected readonly currentStep = signal(1);

  protected readonly steps = [
    { number: 1, label: 'Dados Pessoais' },
    { number: 2, label: 'Endereço' },
    { number: 3, label: 'Pagamento' },
  ];

  // Form data
  protected readonly personalData = signal<PersonalData>({
    name: '',
    email: this.authService.currentUser()?.username ?? '',
    phone: '',
  });

  protected readonly addressData = signal<AddressData>({
    cep: '',
    street: '',
    number: '',
    complement: '',
    neighborhood: '',
    city: '',
    state: '',
  });

  protected readonly paymentMethod = signal<PaymentMethod | null>(null);

  // Validation
  protected readonly isStep1Valid = computed(() => {
    const data = this.personalData();
    return data.name.trim().length > 0
      && data.email.trim().length > 0
      && data.phone.trim().length > 0;
  });

  protected readonly isStep2Valid = computed(() => {
    const data = this.addressData();
    return data.cep.trim().length > 0
      && data.street.trim().length > 0
      && data.number.trim().length > 0
      && data.neighborhood.trim().length > 0
      && data.city.trim().length > 0
      && data.state.trim().length > 0;
  });

  protected readonly isStep3Valid = computed(() => this.paymentMethod() !== null);

  protected readonly paymentOptions: { value: PaymentMethod; label: string }[] = [
    { value: 'pix', label: 'PIX' },
    { value: 'credit', label: 'Cartão de Crédito' },
    { value: 'debit', label: 'Cartão de Débito' },
    { value: 'cash', label: 'Dinheiro' },
  ];

  updatePersonalField(field: keyof PersonalData, value: string): void {
    this.personalData.update((data) => ({ ...data, [field]: value }));
  }

  updateAddressField(field: keyof AddressData, value: string): void {
    this.addressData.update((data) => ({ ...data, [field]: value }));
  }

  selectPayment(method: PaymentMethod): void {
    this.paymentMethod.set(method);
  }

  nextStep(): void {
    if (this.currentStep() < 3) {
      this.currentStep.update((s) => s + 1);
    }
  }

  prevStep(): void {
    if (this.currentStep() > 1) {
      this.currentStep.update((s) => s - 1);
    }
  }

  confirmOrder(): void {
    // In a real app, this would call an order API
    this.cartService.clear();
    this.router.navigate(['/']);
  }
}
