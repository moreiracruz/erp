import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent {
  categories = [
    { name: 'Vestidos', slug: 'Vestidos', image: 'assets/images/cat-vestidos.jpg' },
    { name: 'Blusas', slug: 'Blusas', image: 'assets/images/cat-blusas.jpg' },
    { name: 'Saias', slug: 'Saias', image: 'assets/images/cat-saias.jpg' },
    { name: 'Acessórios', slug: 'Acessorios', image: 'assets/images/cat-acessorios.jpg' },
  ];

  featuredProducts = [
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000001', name: 'Vestido Floral Primavera', price: 289.90, image: 'assets/images/vestido_floral_primavera.png' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000002', name: 'Blusa Seda Natural', price: 189.90, image: 'assets/images/blusa_seda_natural.png' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000003', name: 'Saia Midi Linho', price: 219.90, image: 'assets/images/saia_midi_linho.png' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000004', name: 'Vestido Renda Dourada', price: 349.90, image: 'assets/images/vestido_renda_dourada.png' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000006', name: 'Blusa Bordada Artesanal', price: 159.90, image: 'assets/images/blusa_bordada_artesanal.png' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000008', name: 'Jaqueta Couro Eco', price: 399.90, image: 'assets/images/jaqueta_couro_eco.png' },
  ];
}
