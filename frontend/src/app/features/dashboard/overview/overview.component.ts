import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

type DateRange = 'today' | 'week' | 'month';

interface KpiCard {
  label: string;
  value: string;
  icon: string;
  trend?: string;
}

interface RecentSale {
  id: string;
  customer: string;
  total: number;
  status: 'Finalizada' | 'Pendente' | 'Cancelada';
  date: string;
}

interface LowStockAlert {
  name: string;
  currentStock: number;
  threshold: number;
}

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OverviewComponent {
  readonly selectedRange = signal<DateRange>('today');

  readonly kpis = signal<KpiCard[]>([
    { label: 'Vendas Hoje', value: 'R$ 4.850,00', icon: '💰', trend: '+12%' },
    { label: 'Pedidos Hoje', value: '23', icon: '📦', trend: '+5%' },
    { label: 'Ticket Médio', value: 'R$ 210,87', icon: '🎫', trend: '+3%' },
    { label: 'Produtos em Estoque', value: '1.247', icon: '📊' },
  ]);

  readonly chartData = signal([
    { label: 'Seg', value: 65 },
    { label: 'Ter', value: 80 },
    { label: 'Qua', value: 45 },
    { label: 'Qui', value: 90 },
    { label: 'Sex', value: 100 },
    { label: 'Sáb', value: 75 },
    { label: 'Dom', value: 30 },
  ]);

  readonly recentSales = signal<RecentSale[]>([
    { id: 'V-001', customer: 'Maria Silva', total: 459.90, status: 'Finalizada', date: '2024-01-15 14:30' },
    { id: 'V-002', customer: 'João Santos', total: 189.00, status: 'Finalizada', date: '2024-01-15 13:45' },
    { id: 'V-003', customer: 'Ana Oliveira', total: 720.50, status: 'Pendente', date: '2024-01-15 12:20' },
    { id: 'V-004', customer: 'Carlos Lima', total: 95.00, status: 'Finalizada', date: '2024-01-15 11:10' },
    { id: 'V-005', customer: 'Fernanda Costa', total: 310.00, status: 'Cancelada', date: '2024-01-15 10:05' },
  ]);

  readonly lowStockAlerts = signal<LowStockAlert[]>([
    { name: 'Vestido Floral P', currentStock: 2, threshold: 5 },
    { name: 'Blusa Renda M', currentStock: 1, threshold: 3 },
    { name: 'Saia Midi G', currentStock: 3, threshold: 5 },
    { name: 'Calça Linho PP', currentStock: 0, threshold: 3 },
  ]);

  selectRange(range: DateRange): void {
    this.selectedRange.set(range);
  }

  getMaxChartValue(): number {
    return Math.max(...this.chartData().map((d) => d.value));
  }

  getBarHeight(value: number): number {
    const max = this.getMaxChartValue();
    return max > 0 ? (value / max) * 100 : 0;
  }
}
