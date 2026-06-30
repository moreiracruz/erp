import { ChangeDetectionStrategy, Component, computed, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CurrencyPipe } from '@angular/common';

import { Product, ProductImage, ProductSummary } from '../../../core/models';
import { ImagePort } from '../../../core/ports';
import { CatalogService } from '../catalog/services/catalog.service';
import { CartService } from '../services/cart.service';
import { BreadcrumbSegment, CartItem, SelectedVariant } from '../catalog/models';
import { buildBreadcrumbs, getRelatedProducts, getDefaultVariant, getVariantPrice } from '../catalog/utils';
import { BreadcrumbComponent } from '../shared/components/breadcrumb/breadcrumb.component';
import { ImageGalleryComponent } from './components/image-gallery/image-gallery.component';
import { VariantSelectorComponent } from './components/variant-selector/variant-selector.component';
import { SizeGuideModalComponent } from './components/size-guide-modal/size-guide-modal.component';
import { AddToCartButtonComponent } from './components/add-to-cart-button/add-to-cart-button.component';
import { RelatedProductsComponent } from './components/related-products/related-products.component';

@Component({
  selector: 'app-product-detail-page',
  standalone: true,
  imports: [
    RouterLink,
    CurrencyPipe,
    BreadcrumbComponent,
    ImageGalleryComponent,
    VariantSelectorComponent,
    SizeGuideModalComponent,
    AddToCartButtonComponent,
    RelatedProductsComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './product-detail-page.component.html',
  styleUrls: ['./product-detail-page.component.scss'],
})
export class ProductDetailPageComponent implements OnInit {
  readonly product = signal<Product | null>(null);
  readonly selectedVariant = signal<SelectedVariant>({ size: null, color: null, variant: null });
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);
  readonly addingToCart = signal<boolean>(false);
  readonly currentImageIndex = signal(0);
  readonly sizeGuideOpen = signal(false);

  readonly breadcrumbs = computed<BreadcrumbSegment[]>(() => {
    const prod = this.product();
    return prod ? buildBreadcrumbs(prod) : [];
  });

  readonly relatedProducts = signal<ProductSummary[]>([]);

  readonly currentPrice = computed(() => {
    const sel = this.selectedVariant();
    const prod = this.product();
    if (!prod || !sel.size || !sel.color) return null;
    return getVariantPrice(prod.variants, sel.size, sel.color);
  });

  readonly productImages = signal<ProductImage[]>([]);

  readonly addToCartDisabled = computed(() => {
    const sel = this.selectedVariant();
    return !sel.variant;
  });

  constructor(
    private readonly catalogService: CatalogService,
    private readonly cartService: CartService,
    private readonly imagePort: ImagePort,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    const uuid = this.route.snapshot.paramMap.get('uuid');
    if (!uuid) {
      this.error.set('Produto não encontrado.');
      this.loading.set(false);
      return;
    }

    this.loadProduct(uuid);
  }

  onVariantChange(selected: SelectedVariant): void {
    this.selectedVariant.set(selected);
  }

  onImageSelect(index: number): void {
    this.currentImageIndex.set(index);
  }

  onAddToCart(): void {
    const prod = this.product();
    const sel = this.selectedVariant();
    if (!prod || !sel.variant) return;

    this.addingToCart.set(true);

    const imgs = this.productImages();
    const imageUrl = imgs.length > 0 ? imgs[0].thumbnailUrl : 'assets/images/product-placeholder.webp';

    const cartItem: CartItem = {
      productUuid: prod.uuid,
      variantUuid: sel.variant.uuid,
      productName: prod.name,
      size: sel.variant.size,
      color: sel.variant.color,
      price: sel.variant.price,
      quantity: 1,
      imageUrl,
    };

    this.cartService.addItem(cartItem);

    // Simulate a brief loading state
    setTimeout(() => {
      this.addingToCart.set(false);
    }, 500);
  }

  openSizeGuide(): void {
    this.sizeGuideOpen.set(true);
  }

  closeSizeGuide(): void {
    this.sizeGuideOpen.set(false);
  }

  private loadProduct(uuid: string): void {
    this.loading.set(true);
    this.error.set(null);

    this.catalogService.getProductByUuid(uuid).subscribe({
      next: (product) => {
        this.product.set(product);
        this.selectedVariant.set(getDefaultVariant(product.variants));
        this.loading.set(false);
        this.loadRelatedProducts(product);
        this.loadProductImages(uuid);
      },
      error: (err) => {
        if (err.status === 404) {
          this.error.set('Produto não encontrado.');
        } else {
          this.error.set('Ocorreu um erro ao carregar o produto. Tente novamente.');
        }
        this.loading.set(false);
      },
    });
  }

  private loadProductImages(uuid: string): void {
    this.imagePort.listByProduct(uuid).subscribe({
      next: (images) => {
        // Sort by sortOrder, main image first
        const sorted = [...images].sort((a, b) => {
          if (a.main && !b.main) return -1;
          if (!a.main && b.main) return 1;
          return a.sortOrder - b.sortOrder;
        });
        this.productImages.set(sorted);

        // Set current index to main image
        const mainIndex = sorted.findIndex((img) => img.main);
        if (mainIndex >= 0) {
          this.currentImageIndex.set(mainIndex);
        }
      },
      error: () => {
        // On error, leave productImages empty — gallery shows placeholder
        this.productImages.set([]);
      },
    });
  }

  private loadRelatedProducts(product: Product): void {
    // Use the catalog products to find related products
    const catalogProducts = this.catalogService.products();
    this.relatedProducts.set(
      getRelatedProducts(catalogProducts, product.uuid, product.category)
    );
  }
}
