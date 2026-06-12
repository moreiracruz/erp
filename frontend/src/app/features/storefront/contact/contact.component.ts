import { Component } from '@angular/core';

@Component({
  selector: 'app-contact',
  standalone: true,
  template: `
    <section class="contact-page">
      <div class="container">
        <h1>Contato</h1>
        <p>WhatsApp: (11) 99999-9999</p>
        <p>E-mail: contato&#64;reinoeflor.com.br</p>
        <p>Horário: Segunda a Sábado, 9h às 18h</p>
        <p>Endereço: Rua das Flores, 123 - São Paulo/SP</p>
      </div>
    </section>
  `,
  styles: [`
    .contact-page { padding: 4rem 1.5rem; max-width: 700px; margin: 0 auto; }
    h1 { font-family: 'Playfair Display', serif; font-size: 2.5rem; color: #3B2A1A; margin-bottom: 1.5rem; }
    p { font-size: 1.1rem; color: #7A7268; line-height: 1.8; margin-bottom: 0.5rem; }
  `]
})
export class ContactComponent {}
