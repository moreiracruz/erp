import { Component, ChangeDetectionStrategy, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

interface UserProfile {
  fullName: string;
  email: string;
  phone: string;
  cpf: string;
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfileComponent implements OnInit {
  readonly editing = signal(false);
  readonly saving = signal(false);
  readonly successMsg = signal<string | null>(null);

  readonly profile = signal<UserProfile>({
    fullName: 'Maria Silva',
    email: 'maria@email.com',
    phone: '(11) 99999-0000',
    cpf: '123.456.789-00',
  });

  readonly maskedCpf = computed(() => {
    const cpf = this.profile().cpf;
    if (!cpf || cpf.length < 2) return '—';
    const lastTwo = cpf.replace(/\D/g, '').slice(-2);
    return `***.***.***-${lastTwo}`;
  });

  form!: FormGroup;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      fullName: [this.profile().fullName, [Validators.required]],
      email: [{ value: this.profile().email, disabled: true }],
      phone: [this.profile().phone],
    });
  }

  toggleEdit(): void {
    this.editing.update(v => !v);
    this.successMsg.set(null);
    if (this.editing()) {
      this.form.patchValue({
        fullName: this.profile().fullName,
        phone: this.profile().phone,
      });
    }
  }

  onSave(): void {
    if (this.form.invalid) return;

    this.saving.set(true);
    // Mock save – simulate API call
    setTimeout(() => {
      const { fullName, phone } = this.form.getRawValue();
      this.profile.update(p => ({ ...p, fullName, phone }));
      this.saving.set(false);
      this.editing.set(false);
      this.successMsg.set('Perfil atualizado com sucesso!');
    }, 800);
  }
}
