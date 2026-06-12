import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="unauthorized-page">
      <div class="unauthorized-card">
        <h1>Acesso não autorizado</h1>
        <p>Você não tem permissão para acessar esta página.</p>
        <a routerLink="/" class="btn-home">Voltar ao início</a>
      </div>
    </div>
  `,
  styles: [`
    @use '../../../../styles/variables' as *;

    .unauthorized-page {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      padding: 1.5rem;
      background-color: #FDFBF7;
    }

    .unauthorized-card {
      width: 100%;
      max-width: 440px;
      background: #FFFFFF;
      border-radius: 12px;
      padding: 2.5rem;
      box-shadow: 0 4px 12px rgba(59, 42, 26, 0.08);
      text-align: center;
    }

    h1 {
      font-family: 'Playfair Display', serif;
      font-size: 2rem;
      font-weight: 700;
      color: #3B2A1A;
      margin: 0 0 0.5rem;
    }

    p {
      font-family: 'Inter', sans-serif;
      font-size: 0.875rem;
      color: #7A7268;
      margin: 0 0 2rem;
    }

    .btn-home {
      display: inline-block;
      padding: 0.75rem 1.5rem;
      font-family: 'Inter', sans-serif;
      font-size: 0.875rem;
      font-weight: 600;
      letter-spacing: 0.05em;
      text-transform: uppercase;
      text-decoration: none;
      color: #3B2A1A;
      background-color: #C6A052;
      border-radius: 8px;
      transition: background-color 150ms ease;
    }

    .btn-home:hover {
      background-color: #b08d42;
    }

    .btn-home:focus {
      outline: 2px solid #C6A052;
      outline-offset: 2px;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UnauthorizedComponent {}
