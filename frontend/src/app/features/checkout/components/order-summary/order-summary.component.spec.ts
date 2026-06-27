import { ComponentFixture, TestBed } from '@angular/core/testing';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';

import { OrderSummaryComponent } from './order-summary.component';
import { CartItem } from '../../../storefront/catalog/models';

registerLocaleData(localePt, 'pt-BR');

describe('OrderSummaryComponent', () => {
  let component: OrderSummaryComponent;
  let fixture: ComponentFixture<OrderSummaryComponent>;

  const mockItems: CartItem[] = [
    { productUuid: '1', variantUuid: 'v1', productName: 'Vestido Floral', size: 'M', color: 'Preto', price: 100, quantity: 2 },
    { productUuid: '2', variantUuid: 'v2', productName: 'Blusa Seda', size: 'G', color: 'Azul', price: 50, quantity: 3 },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrderSummaryComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(OrderSummaryComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('items', mockItems);
    fixture.componentRef.setInput('subtotal', 350);
    fixture.componentRef.setInput('shippingCost', 25);
    fixture.componentRef.setInput('total', 375);
    fixture.detectChanges();
  });

  describe('rendering inputs (requirement 3.1)', () => {
    it('should render all cart items', () => {
      const items = fixture.nativeElement.querySelectorAll('.order-summary__item');
      expect(items.length).toBe(2);
    });

    it('should display item names', () => {
      const names = fixture.nativeElement.querySelectorAll('.order-summary__item-name');
      expect(names[0].textContent).toContain('Vestido Floral');
      expect(names[1].textContent).toContain('Blusa Seda');
    });

    it('should display item details with size, color, and quantity', () => {
      const details = fixture.nativeElement.querySelectorAll('.order-summary__item-details');
      expect(details[0].textContent).toContain('M / Preto × 2');
      expect(details[1].textContent).toContain('G / Azul × 3');
    });

    it('should display subtotal', () => {
      const rows = fixture.nativeElement.querySelectorAll('.order-summary__row');
      const subtotalRow = rows[0];
      expect(subtotalRow.textContent).toContain('Subtotal');
      expect(subtotalRow.textContent).toContain('350');
    });

    it('should display shipping cost', () => {
      const rows = fixture.nativeElement.querySelectorAll('.order-summary__row');
      const shippingRow = rows[1];
      expect(shippingRow.textContent).toContain('Frete');
      expect(shippingRow.textContent).toContain('25');
    });

    it('should display "Grátis" when shipping cost is 0', () => {
      fixture.componentRef.setInput('shippingCost', 0);
      fixture.detectChanges();

      const rows = fixture.nativeElement.querySelectorAll('.order-summary__row');
      const shippingRow = rows[1];
      expect(shippingRow.textContent).toContain('Grátis');
    });

    it('should display total', () => {
      const totalRow = fixture.nativeElement.querySelector('.order-summary__row--total');
      expect(totalRow.textContent).toContain('Total');
      expect(totalRow.textContent).toContain('375');
    });
  });

  describe('itemCount computed', () => {
    it('should compute itemCount as sum of all item quantities', () => {
      // mockItems: quantity 2 + quantity 3 = 5
      expect(component['itemCount']()).toBe(5);
    });

    it('should update itemCount when items change', () => {
      const newItems: CartItem[] = [
        { productUuid: '3', variantUuid: 'v3', productName: 'Calça', size: 'P', color: 'Branco', price: 80, quantity: 1 },
      ];
      fixture.componentRef.setInput('items', newItems);
      fixture.detectChanges();

      expect(component['itemCount']()).toBe(1);
    });

    it('should display itemCount in the toggle button', () => {
      const toggle = fixture.nativeElement.querySelector('.order-summary__toggle-label');
      expect(toggle.textContent).toContain('5');
    });

    it('should use singular "item" when itemCount is 1', () => {
      const singleItem: CartItem[] = [
        { productUuid: '1', variantUuid: 'v1', productName: 'Item', size: 'M', color: 'Preto', price: 100, quantity: 1 },
      ];
      fixture.componentRef.setInput('items', singleItem);
      fixture.detectChanges();

      const toggle = fixture.nativeElement.querySelector('.order-summary__toggle-label');
      expect(toggle.textContent).toContain('1 item');
      expect(toggle.textContent).not.toContain('itens');
    });

    it('should use plural "itens" when itemCount is greater than 1', () => {
      const toggle = fixture.nativeElement.querySelector('.order-summary__toggle-label');
      expect(toggle.textContent).toContain('5 itens');
    });
  });

  describe('toggleCollapse (requirement 3.3)', () => {
    it('should start with collapsed signal as true', () => {
      expect(component['collapsed']()).toBe(true);
    });

    it('should toggle collapsed to false on first call', () => {
      component.toggleCollapse();
      expect(component['collapsed']()).toBe(false);
    });

    it('should toggle collapsed back to true on second call', () => {
      component.toggleCollapse();
      component.toggleCollapse();
      expect(component['collapsed']()).toBe(true);
    });

    it('should apply collapsed CSS class when collapsed is true', () => {
      const content = fixture.nativeElement.querySelector('.order-summary__content');
      expect(content.classList.contains('order-summary__content--collapsed')).toBe(true);
    });

    it('should remove collapsed CSS class when collapsed is false', () => {
      component.toggleCollapse();
      fixture.detectChanges();

      const content = fixture.nativeElement.querySelector('.order-summary__content');
      expect(content.classList.contains('order-summary__content--collapsed')).toBe(false);
    });
  });
});
