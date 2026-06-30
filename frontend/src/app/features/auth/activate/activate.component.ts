import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-activate',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './activate.component.html',
  styleUrls: ['./activate.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ActivateComponent implements OnInit {
  form!: FormGroup;
  loading = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  private token = '';

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    this.form = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
    });
  }

  submit(): void {
    const password = this.form.get('password')?.value ?? '';
    const confirmPassword = this.form.get('confirmPassword')?.value ?? '';
    if (this.form.invalid || password !== confirmPassword || !this.token) {
      this.errorMessage.set(!this.token ? 'Token de ativação ausente.' : 'Informe senhas válidas e iguais.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.authService.activate(this.token, password).subscribe({
      next: () => {
        this.loading.set(false);
        this.successMessage.set('Conta ativada. Entre com sua nova senha.');
        setTimeout(() => this.router.navigate(['/auth/login']), 1200);
      },
      error: (message: string) => {
        this.loading.set(false);
        this.errorMessage.set(message);
      },
    });
  }
}
