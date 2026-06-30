import { ComponentFixture, TestBed } from '@angular/core/testing';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';

import { ProductCardComponent } from './product-card.component';
import { ProductSummary } from '../../../../../core/models';

registerLocaleData(localePt, 'pt-BR');

/**
 * Unit tests for ProductCardComponent.
 *
 * Validates: Requirements 1.2, 1.3, 11.3, 13.2
 */
describe('ProductCardComponent', () => {
  let component: ProductCardComponent;
  let fixture: ComponentFixture<ProductCardComponent>;

  const mockProduct: ProductSummary = {
    uuid: 'test-uuid-001',
    name: 'Vestido Floral',
    brand: 'Reino & Flor',
    category: 'Vestidos',
    imageUrl: 'https://example.com/images/vestido.jpg',
    minPrice: 289.9,
    maxPrice: 299.9,
  };

  const mockProductNoImage: ProductSummary = {
    uuid: 'test-uuid-002',
    name: 'Blusa Seda',
    brand: 'Reino & Flor',
    category: 'Blusas',
    imageUrl: undefined,
    minPrice: 189.9,
    maxPrice: 189.9,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductCardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductCardComponent);
    component = fixture.componentInstance;
  });

  describe('product data rendering (Requirement 1.2)', () => {
    it('should display the product name', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      const name = fixture.nativeElement.querySelector('.product-card__name');
      expect(name).toBeTruthy();
      expect(name.textContent).toContain('Vestido Floral');
    });

    it('should display the product price formatted in BRL', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      const price = fixture.nativeElement.querySelector('.product-card__price');
      expect(price).toBeTruthy();
      expect(price.textContent).toContain('R$');
      expect(price.textContent).toContain('289,90');
    });

    it('should display product image with correct src', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      const img: HTMLImageElement = fixture.nativeElement.querySelector('.product-card__image');
      expect(img).toBeTruthy();
      expect(img.src).toContain('vestido.jpg');
    });

    it('should set alt text to product name', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      const img: HTMLImageElement = fixture.nativeElement.querySelector('.product-card__image');
      expect(img.alt).toBe('Vestido Floral');
    });
  });

  describe('hover overlay (Requirement 1.3)', () => {
    it('should not show overlay initially', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      const overlay = fixture.nativeElement.querySelector('.product-card__overlay');
      expect(overlay).toBeNull();
    });

    it('should show overlay on mouseenter', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      const article = fixture.nativeElement.querySelector('.product-card');
      article.dispatchEvent(new Event('mouseenter'));
      fixture.detectChanges();

      const overlay = fixture.nativeElement.querySelector('.product-card__overlay');
      expect(overlay).toBeTruthy();
    });

    it('should hide overlay on mouseleave', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      const article = fixture.nativeElement.querySelector('.product-card');
      article.dispatchEvent(new Event('mouseenter'));
      fixture.detectChanges();

      expect(fixture.nativeElement.querySelector('.product-card__overlay')).toBeTruthy();

      article.dispatchEvent(new Event('mouseleave'));
      fixture.detectChanges();

      expect(fixture.nativeElement.querySelector('.product-card__overlay')).toBeNull();
    });

    it('should display quick-view button in overlay', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      const article = fixture.nativeElement.querySelector('.product-card');
      article.dispatchEvent(new Event('mouseenter'));
      fixture.detectChanges();

      const btn = fixture.nativeElement.querySelector('.product-card__quick-view-btn');
      expect(btn).toBeTruthy();
      expect(btn.textContent).toContain('Ver detalhes');
    });
  });

  describe('image fallback (Requirement 11.3)', () => {
    it('should display placeholder image when imageUrl is undefined', () => {
      fixture.componentRef.setInput('product', mockProductNoImage);
      fixture.detectChanges();

      const img: HTMLImageElement = fixture.nativeElement.querySelector('.product-card__image');
      expect(img).toBeTruthy();
      expect(img.getAttribute('src')).toBe('assets/images/product-placeholder.webp');
    });

    it('should display placeholder image when imageUrl is empty string', () => {
      const productEmptyUrl: ProductSummary = { ...mockProduct, imageUrl: '' };
      fixture.componentRef.setInput('product', productEmptyUrl);
      fixture.detectChanges();

      const img: HTMLImageElement = fixture.nativeElement.querySelector('.product-card__image');
      expect(img.getAttribute('src')).toBe('assets/images/product-placeholder.webp');
    });

    it('should have loading="lazy" attribute on the img element', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      const img: HTMLImageElement = fixture.nativeElement.querySelector('.product-card__image');
      expect(img.getAttribute('loading')).toBe('lazy');
    });
  });

  describe('aria-label (Requirement 13.2)', () => {
    it('should set aria-label with product name and formatted price', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      const article = fixture.nativeElement.querySelector('.product-card');
      const ariaLabel = article.getAttribute('aria-label');
      expect(ariaLabel).toContain('Vestido Floral');
      expect(ariaLabel).toContain('R$');
      expect(ariaLabel).toContain('289,90');
    });

    it('should have role="button" for accessibility', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      const article = fixture.nativeElement.querySelector('.product-card');
      expect(article.getAttribute('role')).toBe('button');
    });

    it('should have tabindex="0" for keyboard navigation', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      const article = fixture.nativeElement.querySelector('.product-card');
      expect(article.getAttribute('tabindex')).toBe('0');
    });
  });

  describe('navigate output', () => {
    it('should emit navigate with product uuid on card click', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      let emittedUuid: string | undefined;
      component.navigate.subscribe((uuid: string) => (emittedUuid = uuid));

      const article = fixture.nativeElement.querySelector('.product-card');
      article.click();

      expect(emittedUuid).toBe('test-uuid-001');
    });

    it('should emit navigate on Enter keydown', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      let emittedUuid: string | undefined;
      component.navigate.subscribe((uuid: string) => (emittedUuid = uuid));

      const article = fixture.nativeElement.querySelector('.product-card');
      article.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
      fixture.detectChanges();

      expect(emittedUuid).toBe('test-uuid-001');
    });
  });

  describe('quickView output', () => {
    it('should emit quickView with product on quick-view button click', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      let emittedProduct: ProductSummary | undefined;
      component.quickView.subscribe((p: ProductSummary) => (emittedProduct = p));

      // Show overlay first
      const article = fixture.nativeElement.querySelector('.product-card');
      article.dispatchEvent(new Event('mouseenter'));
      fixture.detectChanges();

      const btn = fixture.nativeElement.querySelector('.product-card__quick-view-btn');
      btn.click();
      fixture.detectChanges();

      expect(emittedProduct).toEqual(mockProduct);
    });

    it('should not emit navigate when quick-view button is clicked (stopPropagation)', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      let navigateEmitted = false;
      component.navigate.subscribe(() => (navigateEmitted = true));

      // Show overlay
      const article = fixture.nativeElement.querySelector('.product-card');
      article.dispatchEvent(new Event('mouseenter'));
      fixture.detectChanges();

      const btn = fixture.nativeElement.querySelector('.product-card__quick-view-btn');
      btn.click();
      fixture.detectChanges();

      expect(navigateEmitted).toBe(false);
    });
  });

  describe('shimmer placeholder', () => {
    it('should show shimmer placeholder while image is loading', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      const shimmer = fixture.nativeElement.querySelector('.product-card__shimmer');
      expect(shimmer).toBeTruthy();
    });

    it('should hide shimmer after image loads', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      expect(fixture.nativeElement.querySelector('.product-card__shimmer')).toBeTruthy();

      const img: HTMLImageElement = fixture.nativeElement.querySelector('.product-card__image');
      img.dispatchEvent(new Event('load'));
      fixture.detectChanges();

      expect(fixture.nativeElement.querySelector('.product-card__shimmer')).toBeNull();
    });

    it('should add loaded class to image after load event', () => {
      fixture.componentRef.setInput('product', mockProduct);
      fixture.detectChanges();

      const img: HTMLImageElement = fixture.nativeElement.querySelector('.product-card__image');
      expect(img.classList.contains('product-card__image--loaded')).toBe(false);

      img.dispatchEvent(new Event('load'));
      fixture.detectChanges();

      expect(img.classList.contains('product-card__image--loaded')).toBe(true);
    });
  });
});
