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
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000001', name: 'Vestido Floral Primavera', price: 289.90, image: 'assets/images/product-1.jpg' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000002', name: 'Blusa Seda Natural', price: 189.90, image: 'assets/images/product-2.jpg' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000003', name: 'Saia Midi Linho', price: 219.90, image: 'assets/images/product-3.jpg' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000004', name: 'Vestido Renda Dourada', price: 349.90, image: 'assets/images/product-4.jpg' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000005', name: 'Conjunto Elegance', price: 459.90, image: 'assets/images/product-5.jpg' },
    { uuid: 'a1b2c3d4-1111-4000-a000-000000000006', name: 'Blusa Bordada Artesanal', price: 159.90, image: 'assets/images/product-6.jpg' },
  ];
}
