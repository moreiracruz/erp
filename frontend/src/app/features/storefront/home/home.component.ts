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
    { name: 'Vestidos', slug: 'vestidos', image: 'assets/images/cat-vestidos.jpg' },
    { name: 'Blusas', slug: 'blusas', image: 'assets/images/cat-blusas.jpg' },
    { name: 'Saias', slug: 'saias', image: 'assets/images/cat-saias.jpg' },
    { name: 'Acessórios', slug: 'acessorios', image: 'assets/images/cat-acessorios.jpg' },
  ];

  featuredProducts = [
    { uuid: '1', name: 'Vestido Floral Primavera', price: 289.90, image: 'assets/images/product-1.jpg' },
    { uuid: '2', name: 'Blusa Seda Natural', price: 189.90, image: 'assets/images/product-2.jpg' },
    { uuid: '3', name: 'Saia Midi Linho', price: 219.90, image: 'assets/images/product-3.jpg' },
    { uuid: '4', name: 'Vestido Renda Dourada', price: 349.90, image: 'assets/images/product-4.jpg' },
    { uuid: '5', name: 'Conjunto Elegance', price: 459.90, image: 'assets/images/product-5.jpg' },
    { uuid: '6', name: 'Blusa Bordada Artesanal', price: 159.90, image: 'assets/images/product-6.jpg' },
  ];
}
