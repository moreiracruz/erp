import { Component, ChangeDetectionStrategy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

interface Address {
  id: string;
  street: string;
  number: string;
  complement: string;
  neighborhood: string;
  city: string;
  state: string;
  cep: string;
}

@Component({
  selector: 'app-addresses',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './addresses.component.html',
  styleUrls: ['./addresses.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddressesComponent {
  readonly showForm = signal(false);
  readonly editingId = signal<string | null>(null);

  readonly addresses = signal<Address[]>([
    {
      id: '1',
      street: 'Rua das Flores',
      number: '123',
      complement: 'Apto 4B',
      neighborhood: 'Jardim Primavera',
      city: 'São Paulo',
      state: 'SP',
      cep: '01234-567',
    },
    {
      id: '2',
      street: 'Av. Paulista',
      number: '1000',
      complement: '',
      neighborhood: 'Bela Vista',
      city: 'São Paulo',
      state: 'SP',
      cep: '01310-100',
    },
  ]);

  form!: FormGroup;

  constructor(private fb: FormBuilder) {
    this.initForm();
  }

  trackById(_: number, address: Address): string {
    return address.id;
  }

  openForm(): void {
    this.editingId.set(null);
    this.form.reset();
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.editingId.set(null);
    this.form.reset();
  }

  editAddress(address: Address): void {
    this.editingId.set(address.id);
    this.form.patchValue(address);
    this.showForm.set(true);
  }

  deleteAddress(id: string): void {
    this.addresses.update(list => list.filter(a => a.id !== id));
  }

  onSave(): void {
    if (this.form.invalid) return;

    const formValue = this.form.getRawValue();
    const editId = this.editingId();

    if (editId) {
      this.addresses.update(list =>
        list.map(a => (a.id === editId ? { ...a, ...formValue } : a)),
      );
    } else {
      const newAddress: Address = {
        id: crypto.randomUUID(),
        ...formValue,
      };
      this.addresses.update(list => [...list, newAddress]);
    }

    this.closeForm();
  }

  private initForm(): void {
    this.form = this.fb.group({
      street: ['', [Validators.required]],
      number: ['', [Validators.required]],
      complement: [''],
      neighborhood: ['', [Validators.required]],
      city: ['', [Validators.required]],
      state: ['', [Validators.required]],
      cep: ['', [Validators.required]],
    });
  }
}
