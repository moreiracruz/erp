import { ChangeDetectionStrategy, Component, signal, computed, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SalesHttpAdapter, SaleResponse, PosPaymentMethod } from '../../../infrastructure/http/sales-http.adapter';

interface PosItem {
  id: string;
  name: string;
  barcode: string;
  price: number;
  qty: number;
}

type PaymentMethod = 'PIX' | 'Dinheiro' | 'Cartão';

@Component({
  selector: 'app-terminal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './terminal.component.html',
  styleUrl: './terminal.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TerminalComponent implements OnInit, OnDestroy {
  readonly operatorName = signal('Operador');
  readonly terminalId = signal('PDV-01');
  readonly currentTime = signal('');
  readonly searchQuery = signal('');
  readonly items = signal<PosItem[]>([]);
  readonly discount = signal(0);
  readonly selectedPayment = signal<PaymentMethod>('Dinheiro');
  readonly saleUuid = signal<string | null>(null);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly subtotal = computed(() =>
    this.items().reduce((sum, item) => sum + item.price * item.qty, 0),
  );

  readonly total = computed(() => this.subtotal() - this.discount());

  private timerInterval: ReturnType<typeof setInterval> | null = null;

  constructor(private readonly salesAdapter: SalesHttpAdapter) {}

  ngOnInit(): void {
    this.updateTime();
    this.timerInterval = setInterval(() => this.updateTime(), 1000);
  }

  ngOnDestroy(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
  }

  @HostListener('window:keydown', ['$event'])
  handleKeyboard(event: KeyboardEvent): void {
    if (event.key === 'F2') {
      event.preventDefault();
      document.getElementById('pos-search')?.focus();
    } else if (event.key === 'F5') {
      event.preventDefault();
      this.finalizeSale();
    } else if (event.key === 'Escape') {
      event.preventDefault();
      this.cancelSale();
    }
  }

  searchProduct(): void {
    const query = this.searchQuery().trim();
    if (!query) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    this.ensureOpenSale((saleUuid) => {
      this.salesAdapter.addItem(saleUuid, query, 1).subscribe({
        next: (sale) => {
          this.applySale(sale);
          this.searchQuery.set('');
          this.loading.set(false);
        },
        error: () => this.handleError('Produto não encontrado ou estoque insuficiente.'),
      });
    });
  }

  removeItem(id: string): void {
    this.errorMessage.set('Remoção de item ainda não está disponível no backend.');
  }

  updateQty(id: string, qty: number): void {
    this.errorMessage.set('Altere quantidade lendo o produto novamente no PDV.');
  }

  selectPayment(method: PaymentMethod): void {
    this.selectedPayment.set(method);
  }

  finalizeSale(): void {
    if (this.items().length === 0) return;
    const saleUuid = this.saleUuid();
    if (!saleUuid) return;

    this.loading.set(true);
    this.errorMessage.set(null);
    const total = this.total();
    this.salesAdapter.finalizeSale(saleUuid, this.toApiPayment(this.selectedPayment()), total, total).subscribe({
      next: () => {
        this.items.set([]);
        this.discount.set(0);
        this.saleUuid.set(null);
        this.loading.set(false);
      },
      error: () => this.handleError('Não foi possível finalizar a venda.'),
    });
  }

  cancelSale(): void {
    const saleUuid = this.saleUuid();
    if (!saleUuid) {
      this.resetSale();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.salesAdapter.cancelSale(saleUuid, 'Cancelada no PDV').subscribe({
      next: () => {
        this.resetSale();
        this.loading.set(false);
      },
      error: () => this.handleError('Não foi possível cancelar a venda.'),
    });
  }

  private updateTime(): void {
    this.currentTime.set(
      new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
    );
  }

  private ensureOpenSale(next: (saleUuid: string) => void): void {
    const existingSaleUuid = this.saleUuid();
    if (existingSaleUuid) {
      next(existingSaleUuid);
      return;
    }

    this.salesAdapter.openSale(this.terminalId()).subscribe({
      next: (sale) => {
        this.saleUuid.set(sale.uuid);
        next(sale.uuid);
      },
      error: () => this.handleError('Não foi possível abrir a venda.'),
    });
  }

  private applySale(sale: SaleResponse): void {
    this.saleUuid.set(sale.uuid);
    this.discount.set(this.toNumber(sale.discountAmount));
    this.items.set(sale.items.map((item) => ({
      id: item.varianteUuid,
      name: item.sku,
      barcode: '',
      price: this.toNumber(item.unitPrice),
      qty: item.quantity,
    })));
  }

  private resetSale(): void {
    this.items.set([]);
    this.discount.set(0);
    this.searchQuery.set('');
    this.saleUuid.set(null);
  }

  private handleError(message: string): void {
    this.errorMessage.set(message);
    this.loading.set(false);
  }

  private toApiPayment(method: PaymentMethod): PosPaymentMethod {
    if (method === 'PIX') return 'PIX';
    if (method === 'Cartão') return 'CREDITO';
    return 'DINHEIRO';
  }

  private toNumber(value: number | string): number {
    return typeof value === 'number' ? value : Number(value);
  }
}
