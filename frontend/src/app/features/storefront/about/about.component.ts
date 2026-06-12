import { Component } from '@angular/core';

@Component({
  selector: 'app-about',
  standalone: true,
  template: `
    <section class="about-page">
      <div class="container">
        <h1>Sobre a Reino &amp; Flor</h1>
        <p>Nascida da paixão por moda feminina e elegância, a Reino &amp; Flor traz peças exclusivas que celebram a feminilidade com sofisticação e conforto.</p>
        <p>Cada peça é selecionada com carinho, priorizando qualidade, design atemporal e materiais nobres.</p>
      </div>
    </section>
  `,
  styles: [`
    .about-page { padding: 4rem 1.5rem; max-width: 700px; margin: 0 auto; }
    h1 { font-family: 'Playfair Display', serif; font-size: 2.5rem; color: #3B2A1A; margin-bottom: 1.5rem; }
    p { font-size: 1.1rem; color: #7A7268; line-height: 1.8; margin-bottom: 1rem; }
  `]
})
export class AboutComponent {}
