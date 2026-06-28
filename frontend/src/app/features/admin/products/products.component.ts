import { ChangeDetectionStrategy, Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ProductImageSectionComponent } from './components/product-image-section/product-image-section.component';
import { AdminProduct, AdminProductHttpAdapter } from '../../../infrastructure/http/admin-product-http.adapter';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule, ProductImageSectionComponent],
  templateUrl: './products.component.html',
  styleUrl: './products.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductsComponent implements OnInit {
  readonly searchQuery = signal('');
  readonly showForm = signal(false);
  readonly editingProduct = signal<AdminProduct | null>(null);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly formData = signal<Omit<AdminProduct, 'id' | 'active'>>({
    name: '',
    brand: '',
    category: '',
    price: 0,
    cost: 0,
  });

  readonly products = signal<AdminProduct[]>([]);

  constructor(private readonly productAdapter: AdminProductHttpAdapter) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  readonly filteredProducts = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) return this.products();
    return this.products().filter(
      (p) =>
        p.name.toLowerCase().includes(query) ||
        p.brand.toLowerCase().includes(query) ||
        p.category.toLowerCase().includes(query),
    );
  });

  openNewForm(): void {
    this.editingProduct.set(null);
    this.formData.set({ name: '', brand: '', category: '', price: 0, cost: 0 });
    this.showForm.set(true);
  }

  openEditForm(product: AdminProduct): void {
    this.editingProduct.set(product);
    this.formData.set({
      name: product.name,
      brand: product.brand,
      category: product.category,
      price: product.price,
      cost: product.cost,
    });
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.editingProduct.set(null);
  }

  saveProduct(): void {
    const data = this.formData();
    if (!data.name.trim()) return;

    const editing = this.editingProduct();
    const command = {
      name: data.name.trim(),
      brand: data.brand.trim(),
      category: data.category.trim(),
    };
    this.loading.set(true);
    this.errorMessage.set(null);

    if (editing) {
      this.productAdapter.update(editing.id, command).subscribe({
        next: (updated) => {
          this.products.set(this.products().map((p) => (p.id === editing.id ? updated : p)));
          this.loading.set(false);
          this.closeForm();
        },
        error: () => this.handleError('Não foi possível atualizar o produto.'),
      });
    } else {
      this.productAdapter.create(command).subscribe({
        next: (created) => {
          this.products.set([...this.products(), created]);
          this.loading.set(false);
          this.closeForm();
        },
        error: () => this.handleError('Não foi possível criar o produto.'),
      });
    }
  }

  toggleActive(product: AdminProduct): void {
    if (!product.active) return;

    this.loading.set(true);
    this.errorMessage.set(null);
    this.productAdapter.deactivate(product.id).subscribe({
      next: () => {
        this.products.set(this.products().map((p) => (p.id === product.id ? { ...p, active: false } : p)));
        this.loading.set(false);
      },
      error: () => this.handleError('Não foi possível desativar o produto.'),
    });
  }

  updateFormField(field: keyof Omit<AdminProduct, 'id' | 'active'>, value: string | number): void {
    this.formData.set({ ...this.formData(), [field]: value });
  }

  private loadProducts(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.productAdapter.list().subscribe({
      next: (products) => {
        this.products.set(products);
        this.loading.set(false);
      },
      error: () => this.handleError('Não foi possível carregar os produtos.'),
    });
  }

  private handleError(message: string): void {
    this.errorMessage.set(message);
    this.loading.set(false);
  }
}
