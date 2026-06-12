import { Component, ChangeDetectionStrategy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

interface OrderItem {
  productName: string;
  quantity: number;
  price: number;
}

interface Order {
  id: string;
  orderNumber: string;
  date: string;
  status: 'processing' | 'shipped' | 'delivered';
  total: number;
  items: OrderItem[];
}

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrdersComponent {
  readonly orders = signal<Order[]>([
    {
      id: '1',
      orderNumber: '#RF-2024001',
      date: '2024-12-15',
      status: 'delivered',
      total: 459.9,
      items: [
        { productName: 'Vestido Midi Floral', quantity: 1, price: 289.9 },
        { productName: 'Brinco Pérola', quantity: 2, price: 85.0 },
      ],
    },
    {
      id: '2',
      orderNumber: '#RF-2024002',
      date: '2025-01-03',
      status: 'shipped',
      total: 189.9,
      items: [
        { productName: 'Blusa Renda Off-White', quantity: 1, price: 189.9 },
      ],
    },
    {
      id: '3',
      orderNumber: '#RF-2024003',
      date: '2025-01-10',
      status: 'processing',
      total: 649.8,
      items: [
        { productName: 'Saia Longa Linho', quantity: 1, price: 329.9 },
        { productName: 'Sandália Rasteira Dourada', quantity: 1, price: 319.9 },
      ],
    },
  ]);

  readonly expandedOrderId = signal<string | null>(null);

  getStatusLabel(status: Order['status']): string {
    const labels: Record<Order['status'], string> = {
      processing: 'Processando',
      shipped: 'Enviado',
      delivered: 'Entregue',
    };
    return labels[status];
  }

  toggleOrder(orderId: string): void {
    this.expandedOrderId.update(current =>
      current === orderId ? null : orderId,
    );
  }

  formatCurrency(value: number): string {
    return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr + 'T00:00:00').toLocaleDateString('pt-BR');
  }
}
