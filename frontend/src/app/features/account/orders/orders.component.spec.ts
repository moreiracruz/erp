import { describe, it, expect, beforeEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import * as fc from 'fast-check';
import { OrdersComponent } from './orders.component';

/**
 * Property 2: Order status label mapping is total (bijective)
 * Validates: Requirements 2.2
 */
describe('OrdersComponent - Property 2: Order status label mapping is total', () => {
  let component: OrdersComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdersComponent],
    }).compileComponents();

    const fixture = TestBed.createComponent(OrdersComponent);
    component = fixture.componentInstance;
  });

  it('getStatusLabel returns a non-empty Portuguese label for any valid status', () => {
    const validLabels = new Set(['Processando', 'Enviado', 'Entregue']);

    fc.assert(
      fc.property(
        fc.constantFrom('processing' as const, 'shipped' as const, 'delivered' as const),
        (status) => {
          const label = component.getStatusLabel(status);
          // Label must be a non-empty string
          expect(label).toBeTruthy();
          expect(typeof label).toBe('string');
          expect(label.length).toBeGreaterThan(0);
          // Label must be from the known set
          expect(validLabels.has(label)).toBe(true);
        },
      ),
      { numRuns: 100 },
    );
  });

  it('mapping is bijective: all 3 statuses map to 3 unique labels', () => {
    const statuses = ['processing', 'shipped', 'delivered'] as const;
    const labels = statuses.map((s) => component.getStatusLabel(s));
    const uniqueLabels = new Set(labels);

    // All labels must be unique (bijective mapping)
    expect(uniqueLabels.size).toBe(3);
    // All labels must be from the expected set
    expect(uniqueLabels).toEqual(new Set(['Processando', 'Enviado', 'Entregue']));
  });
});

/**
 * Property 3: Toggle expand is its own inverse
 * Validates: Requirements 2.3
 */
describe('OrdersComponent - Property 3: Toggle expand is its own inverse', () => {
  let component: OrdersComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdersComponent],
    }).compileComponents();

    const fixture = TestBed.createComponent(OrdersComponent);
    component = fixture.componentInstance;
  });

  it('calling toggleOrder(id) twice returns expandedOrderId to its original value', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 1 }),
        fc.boolean(),
        (orderId, startExpanded) => {
          // Initial state is either null (collapsed) or orderId (expanded to this order)
          const initialState = startExpanded ? orderId : null;

          // Set initial state
          component.expandedOrderId.set(initialState);

          // Toggle twice with the same ID
          component.toggleOrder(orderId);
          component.toggleOrder(orderId);

          // Should be back to original state
          expect(component.expandedOrderId()).toBe(initialState);
        },
      ),
      { numRuns: 100 },
    );
  });
});

/**
 * Task 2.3: Unit tests for OrdersComponent
 * Validates: Requirements 2.1, 2.3
 */
describe('OrdersComponent - Unit Tests', () => {
  let component: OrdersComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdersComponent],
    }).compileComponents();

    const fixture = TestBed.createComponent(OrdersComponent);
    component = fixture.componentInstance;
  });

  it('formatCurrency produces pt-BR BRL format', () => {
    const result = component.formatCurrency(459.9);
    // pt-BR BRL format: R$ 459,90 (with non-breaking space possible)
    expect(result).toContain('R$');
    expect(result).toContain('459');
    expect(result).toContain('90');
  });

  it('formatDate produces pt-BR date string', () => {
    const result = component.formatDate('2024-12-15');
    // pt-BR date: 15/12/2024
    expect(result).toBe('15/12/2024');
  });

  it('toggleOrder with matching ID sets expandedOrderId to null (collapse)', () => {
    component.expandedOrderId.set('order-1');
    component.toggleOrder('order-1');
    expect(component.expandedOrderId()).toBeNull();
  });

  it('toggleOrder with different ID sets expandedOrderId to new ID (expand)', () => {
    component.expandedOrderId.set('order-1');
    component.toggleOrder('order-2');
    expect(component.expandedOrderId()).toBe('order-2');
  });
});
