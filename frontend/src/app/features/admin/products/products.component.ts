import { ChangeDetectionStrategy, Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface AdminProduct {
  id: string;
  name: string;
  brand: string;
  category: string;
  price: number;
  cost: number;
  active: boolean;
}

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './products.component.html',
  styleUrl: './products.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductsComponent {
  readonly searchQuery = signal('');
  readonly showForm = signal(false);
  readonly editingProduct = signal<AdminProduct | null>(null);

  readonly formData = signal<Omit<AdminProduct, 'id' | 'active'>>({
    name: '',
    brand: '',
    category: '',
    price: 0,
    cost: 0,
  });

  readonly products = signal<AdminProduct[]>([
    { id: '1', name: 'Vestido Floral Midi', brand: 'Reino & Flor', category: 'Vestidos', price: 289.90, cost: 120.00, active: true },
    { id: '2', name: 'Blusa Renda Manga Longa', brand: 'Reino & Flor', category: 'Blusas', price: 159.90, cost: 65.00, active: true },
    { id: '3', name: 'Saia Midi Plissada', brand: 'Reino & Flor', category: 'Saias', price: 199.90, cost: 80.00, active: true },
    { id: '4', name: 'Calça Pantalona Linho', brand: 'Reino & Flor', category: 'Calças', price: 249.90, cost: 95.00, active: true },
    { id: '5', name: 'Jaqueta Couro Eco', brand: 'Parceiro A', category: 'Jaquetas', price: 399.90, cost: 180.00, active: false },
    { id: '6', name: 'Camiseta Basic Algodão', brand: 'Reino & Flor', category: 'Camisetas', price: 79.90, cost: 30.00, active: true },
    { id: '7', name: 'Shorts Alfaiataria', brand: 'Reino & Flor', category: 'Shorts', price: 149.90, cost: 55.00, active: true },
  ]);

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
    if (editing) {
      this.products.set(
        this.products().map((p) =>
          p.id === editing.id ? { ...p, ...data } : p,
        ),
      );
    } else {
      const newProduct: AdminProduct = {
        id: crypto.randomUUID(),
        ...data,
        active: true,
      };
      this.products.set([...this.products(), newProduct]);
    }

    this.closeForm();
  }

  toggleActive(product: AdminProduct): void {
    this.products.set(
      this.products().map((p) =>
        p.id === product.id ? { ...p, active: !p.active } : p,
      ),
    );
  }

  updateFormField(field: keyof Omit<AdminProduct, 'id' | 'active'>, value: string | number): void {
    this.formData.set({ ...this.formData(), [field]: value });
  }
}
