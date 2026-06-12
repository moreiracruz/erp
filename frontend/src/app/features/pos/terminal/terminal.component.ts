import { ChangeDetectionStrategy, Component, signal, computed, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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

  readonly subtotal = computed(() =>
    this.items().reduce((sum, item) => sum + item.price * item.qty, 0),
  );

  readonly total = computed(() => this.subtotal() - this.discount());

  private timerInterval: ReturnType<typeof setInterval> | null = null;

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

    // Mock: simulate finding a product by barcode or name
    const mockProduct: PosItem = {
      id: crypto.randomUUID(),
      name: query.length > 8 ? 'Produto ' + query.slice(0, 6) : query,
      barcode: query.length > 8 ? query : '',
      price: +(Math.random() * 200 + 20).toFixed(2),
      qty: 1,
    };

    const current = this.items();
    const existing = current.find(
      (i) => i.barcode === query || i.name.toLowerCase() === query.toLowerCase(),
    );

    if (existing) {
      this.items.set(
        current.map((i) => (i.id === existing.id ? { ...i, qty: i.qty + 1 } : i)),
      );
    } else {
      this.items.set([...current, mockProduct]);
    }

    this.searchQuery.set('');
  }

  removeItem(id: string): void {
    this.items.set(this.items().filter((i) => i.id !== id));
  }

  updateQty(id: string, qty: number): void {
    if (qty < 1) {
      this.removeItem(id);
      return;
    }
    this.items.set(this.items().map((i) => (i.id === id ? { ...i, qty } : i)));
  }

  selectPayment(method: PaymentMethod): void {
    this.selectedPayment.set(method);
  }

  finalizeSale(): void {
    if (this.items().length === 0) return;
    // In production, this would call a sale service
    alert(`Venda finalizada!\nTotal: R$ ${this.total().toFixed(2)}\nPagamento: ${this.selectedPayment()}`);
    this.items.set([]);
    this.discount.set(0);
  }

  cancelSale(): void {
    this.items.set([]);
    this.discount.set(0);
    this.searchQuery.set('');
  }

  private updateTime(): void {
    this.currentTime.set(
      new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
    );
  }
}
