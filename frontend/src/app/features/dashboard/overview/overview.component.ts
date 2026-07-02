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
    { label: 'Vendas Hoje', value: 'R$ 0,00', icon: '💰' },
    { label: 'Pedidos Hoje', value: '0', icon: '📦' },
    { label: 'Ticket Médio', value: 'R$ 0,00', icon: '🎫' },
    { label: 'Produtos em Estoque', value: '0', icon: '📊' },
  ]);

  readonly chartData = signal([
    { label: 'Seg', value: 0 },
    { label: 'Ter', value: 0 },
    { label: 'Qua', value: 0 },
    { label: 'Qui', value: 0 },
    { label: 'Sex', value: 0 },
    { label: 'Sáb', value: 0 },
    { label: 'Dom', value: 0 },
  ]);

  readonly recentSales = signal<RecentSale[]>([]);

  readonly lowStockAlerts = signal<LowStockAlert[]>([]);

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
