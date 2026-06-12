import { Component, OnInit, ChangeDetectionStrategy, signal, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent implements OnInit, AfterViewInit {
  @ViewChild('emailInput') emailInput!: ElementRef<HTMLInputElement>;

  form!: FormGroup;
  loading = signal(false);
  errorMessage = signal<string | null>(null);
  showPassword = signal(false);

  private returnUrl: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      rememberMe: [false],
    });

    this.returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');

    const message = this.route.snapshot.queryParamMap.get('message');
    if (message === 'session_expired') {
      this.errorMessage.set('Sua sessão expirou. Faça login novamente.');
    }
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.emailInput?.nativeElement.focus());
  }

  togglePasswordVisibility(): void {
    this.showPassword.update(v => !v);
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    const { email, password, rememberMe } = this.form.value;

    this.authService.login(email, password, rememberMe).subscribe({
      next: () => {
        this.loading.set(false);
        const target = this.returnUrl ?? this.authService.getDefaultRouteForRole(this.authService.userRole());
        this.router.navigateByUrl(target);
      },
      error: (msg: string) => {
        this.loading.set(false);
        this.errorMessage.set(msg);
      },
    });
  }
}
