import { Component } from '@angular/core';

@Component({
  selector: 'app-home',
  standalone: true,
  template: `
    <section class="hero">
      <h1>Reino & Flor</h1>
      <p>Elegância que conta histórias</p>
    </section>
  `,
  styles: [`
    .hero {
      min-height: 80vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
    }
    h1 {
      font-family: 'Playfair Display', serif;
      font-size: 3.5rem;
      color: #3B2A1A;
    }
    p {
      font-size: 1.25rem;
      color: #7A7268;
      margin-top: 1rem;
    }
  `]
})
export class HomeComponent {}
