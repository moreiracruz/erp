import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { Router } from '@angular/router';
import { vi } from 'vitest';

import { CheckoutPageComponent } from './checkout-page.component';
import { AuthService } from '../../auth/services/auth.service';
import { CartService } from '../../storefront/services/cart.service';

describe('CheckoutPageComponent', () => {
  let component: CheckoutPageComponent;
  let mockRouter: { navigate: ReturnType<typeof vi.fn> };
  let mockAuthService: { currentUser: ReturnType<typeof signal> };
  let cartService: CartService;

  beforeEach(() => {
    mockRouter = { navigate: vi.fn() };
    mockAuthService = { currentUser: signal(null) };

    TestBed.configureTestingModule({
      imports: [CheckoutPageComponent],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: AuthService, useValue: mockAuthService },
      ],
    });

    cartService = TestBed.inject(CartService);
    const fixture = TestBed.createComponent(CheckoutPageComponent);
    component = fixture.componentInstance;
  });

  // Requirement 2.1 - 3-step checkout flow
  describe('step navigation', () => {
    it('initializes at step 1', () => {
      expect((component as any).currentStep()).toBe(1);
    });

    it('nextStep advances from step 1 to 2 to 3', () => {
      expect((component as any).currentStep()).toBe(1);

      component.nextStep();
      expect((component as any).currentStep()).toBe(2);

      component.nextStep();
      expect((component as any).currentStep()).toBe(3);
    });

    it('nextStep does not advance beyond step 3', () => {
      component.nextStep();
      component.nextStep();
      component.nextStep(); // should stay at 3
      expect((component as any).currentStep()).toBe(3);
    });

    it('prevStep goes back from step 3 to 2 to 1', () => {
      component.nextStep();
      component.nextStep();
      expect((component as any).currentStep()).toBe(3);

      component.prevStep();
      expect((component as any).currentStep()).toBe(2);

      component.prevStep();
      expect((component as any).currentStep()).toBe(1);
    });

    it('prevStep does not go below step 1', () => {
      component.prevStep(); // should stay at 1
      expect((component as any).currentStep()).toBe(1);
    });
  });

  // Requirement 2.3 - Step 1 validation
  describe('isStep1Valid', () => {
    it('is invalid when all fields are empty', () => {
      expect((component as any).isStep1Valid()).toBe(false);
    });

    it('is invalid when name is missing', () => {
      (component as any).personalData.set({ name: '', email: 'test@test.com', phone: '123' });
      expect((component as any).isStep1Valid()).toBe(false);
    });

    it('is invalid when email is missing', () => {
      (component as any).personalData.set({ name: 'John', email: '', phone: '123' });
      expect((component as any).isStep1Valid()).toBe(false);
    });

    it('is invalid when phone is missing', () => {
      (component as any).personalData.set({ name: 'John', email: 'test@test.com', phone: '' });
      expect((component as any).isStep1Valid()).toBe(false);
    });

    it('is valid when name, email, and phone are all provided', () => {
      (component as any).personalData.set({ name: 'John', email: 'test@test.com', phone: '123456' });
      expect((component as any).isStep1Valid()).toBe(true);
    });

    it('is invalid when fields contain only whitespace', () => {
      (component as any).personalData.set({ name: '   ', email: 'test@test.com', phone: '123' });
      expect((component as any).isStep1Valid()).toBe(false);
    });
  });

  // Requirement 2.4 - Step 2 validation
  describe('isStep2Valid', () => {
    it('is invalid when all address fields are empty', () => {
      expect((component as any).isStep2Valid()).toBe(false);
    });

    it('is invalid when cep is missing', () => {
      (component as any).addressData.set({
        cep: '', street: 'Rua A', number: '10',
        complement: '', neighborhood: 'Centro', city: 'SP', state: 'SP',
      });
      expect((component as any).isStep2Valid()).toBe(false);
    });

    it('is invalid when street is missing', () => {
      (component as any).addressData.set({
        cep: '01001-000', street: '', number: '10',
        complement: '', neighborhood: 'Centro', city: 'SP', state: 'SP',
      });
      expect((component as any).isStep2Valid()).toBe(false);
    });

    it('is invalid when number is missing', () => {
      (component as any).addressData.set({
        cep: '01001-000', street: 'Rua A', number: '',
        complement: '', neighborhood: 'Centro', city: 'SP', state: 'SP',
      });
      expect((component as any).isStep2Valid()).toBe(false);
    });

    it('is invalid when neighborhood is missing', () => {
      (component as any).addressData.set({
        cep: '01001-000', street: 'Rua A', number: '10',
        complement: '', neighborhood: '', city: 'SP', state: 'SP',
      });
      expect((component as any).isStep2Valid()).toBe(false);
    });

    it('is invalid when city is missing', () => {
      (component as any).addressData.set({
        cep: '01001-000', street: 'Rua A', number: '10',
        complement: '', neighborhood: 'Centro', city: '', state: 'SP',
      });
      expect((component as any).isStep2Valid()).toBe(false);
    });

    it('is invalid when state is missing', () => {
      (component as any).addressData.set({
        cep: '01001-000', street: 'Rua A', number: '10',
        complement: '', neighborhood: 'Centro', city: 'SP', state: '',
      });
      expect((component as any).isStep2Valid()).toBe(false);
    });

    it('is valid when all required address fields are provided', () => {
      (component as any).addressData.set({
        cep: '01001-000', street: 'Rua A', number: '10',
        complement: '', neighborhood: 'Centro', city: 'São Paulo', state: 'SP',
      });
      expect((component as any).isStep2Valid()).toBe(true);
    });
  });

  // Requirement 2.5 - Step 3 validation
  describe('isStep3Valid', () => {
    it('is invalid when no payment method is selected', () => {
      expect((component as any).isStep3Valid()).toBe(false);
    });

    it('is valid when a payment method is selected', () => {
      (component as any).paymentMethod.set('pix');
      expect((component as any).isStep3Valid()).toBe(true);
    });
  });

  // Requirement 2.3 - Pre-fill email from AuthService
  describe('pre-fill email from AuthService', () => {
    it('pre-fills email from AuthService when user is authenticated', () => {
      // Set user BEFORE creating the component
      mockAuthService.currentUser.set({ username: 'user@example.com', role: 'CLIENT' });

      // Recreate component with authenticated user
      const fixture = TestBed.createComponent(CheckoutPageComponent);
      const authComponent = fixture.componentInstance;

      expect((authComponent as any).personalData().email).toBe('user@example.com');
    });

    it('defaults email to empty string when user is not authenticated', () => {
      expect((component as any).personalData().email).toBe('');
    });
  });

  // Requirement 2.1 - confirmOrder
  describe('confirmOrder', () => {
    it('clears cart and navigates to home', () => {
      // Add an item to the cart so we can verify it gets cleared
      cartService.items.set([{
        productUuid: 'p1',
        variantUuid: 'v1',
        productName: 'Test',
        size: 'M',
        color: 'Preto',
        price: 100,
        quantity: 1,
      }]);

      component.confirmOrder();

      expect(cartService.items()).toEqual([]);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
    });
  });
});
