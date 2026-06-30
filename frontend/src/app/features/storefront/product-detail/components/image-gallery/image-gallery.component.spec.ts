import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImageGalleryComponent } from './image-gallery.component';
import { ProductImage } from '../../../../../core/models';

describe('ImageGalleryComponent', () => {
  let component: ImageGalleryComponent;
  let fixture: ComponentFixture<ImageGalleryComponent>;

  const mockImages: ProductImage[] = [
    {
      id: 1,
      filename: 'img1',
      originalName: 'vestido_frente.jpg',
      contentType: 'image/jpeg',
      fileSize: 200000,
      sortOrder: 0,
      main: true,
      createdAt: '2024-01-15T10:00:00Z',
      thumbnailUrl: '/uploads/products/uuid1/img1_thumb.jpg',
      cardUrl: '/uploads/products/uuid1/img1_card.jpg',
      fullUrl: '/uploads/products/uuid1/img1_full.jpg',
    },
    {
      id: 2,
      filename: 'img2',
      originalName: 'vestido_costas.jpg',
      contentType: 'image/jpeg',
      fileSize: 180000,
      sortOrder: 1,
      main: false,
      createdAt: '2024-01-15T10:01:00Z',
      thumbnailUrl: '/uploads/products/uuid1/img2_thumb.jpg',
      cardUrl: '/uploads/products/uuid1/img2_card.jpg',
      fullUrl: '/uploads/products/uuid1/img2_full.jpg',
    },
    {
      id: 3,
      filename: 'img3',
      originalName: 'vestido_detalhe.jpg',
      contentType: 'image/jpeg',
      fileSize: 150000,
      sortOrder: 2,
      main: false,
      createdAt: '2024-01-15T10:02:00Z',
      thumbnailUrl: '/uploads/products/uuid1/img3_thumb.jpg',
      cardUrl: '/uploads/products/uuid1/img3_card.jpg',
      fullUrl: '/uploads/products/uuid1/img3_full.jpg',
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImageGalleryComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ImageGalleryComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    // Clean up any preload links added to document.head
    document.querySelectorAll('link[rel="preload"][as="image"]').forEach((el) => el.remove());
  });

  describe('placeholder display', () => {
    it('should display placeholder when images array is empty', () => {
      fixture.componentRef.setInput('images', []);
      fixture.detectChanges();

      const img: HTMLImageElement = fixture.nativeElement.querySelector(
        '.image-gallery__primary-img'
      );
      expect(img).toBeTruthy();
      expect(img.getAttribute('src')).toBe('assets/images/product-placeholder.webp');
      expect(img.classList.contains('image-gallery__primary-img--placeholder')).toBe(true);
    });
  });

  describe('primary image display', () => {
    it('should display primary image using fullUrl of active image', () => {
      fixture.componentRef.setInput('images', mockImages);
      fixture.componentRef.setInput('currentIndex', 0);
      fixture.detectChanges();

      const img: HTMLImageElement = fixture.nativeElement.querySelector(
        '.image-gallery__primary-img'
      );
      expect(img).toBeTruthy();
      expect(img.getAttribute('src')).toBe(mockImages[0].fullUrl);
    });
  });

  describe('thumbnail display', () => {
    it('should display thumbnails using thumbnailUrl', () => {
      fixture.componentRef.setInput('images', mockImages);
      fixture.componentRef.setInput('currentIndex', 0);
      fixture.detectChanges();

      const thumbnails = fixture.nativeElement.querySelectorAll('.image-gallery__thumbnail img');
      expect(thumbnails.length).toBe(3);
      expect(thumbnails[0].getAttribute('src')).toBe(mockImages[0].thumbnailUrl);
      expect(thumbnails[1].getAttribute('src')).toBe(mockImages[1].thumbnailUrl);
      expect(thumbnails[2].getAttribute('src')).toBe(mockImages[2].thumbnailUrl);
    });

    it('should not display thumbnails when there is only one image', () => {
      fixture.componentRef.setInput('images', [mockImages[0]]);
      fixture.componentRef.setInput('currentIndex', 0);
      fixture.detectChanges();

      const thumbnailSection = fixture.nativeElement.querySelector('.image-gallery__thumbnails');
      expect(thumbnailSection).toBeNull();
    });
  });

  describe('thumbnail interaction', () => {
    it('should emit imageSelect when thumbnail is clicked', () => {
      fixture.componentRef.setInput('images', mockImages);
      fixture.componentRef.setInput('currentIndex', 0);
      fixture.detectChanges();

      const emitSpy = vi.spyOn(component.imageSelect, 'emit');

      const thumbnails = fixture.nativeElement.querySelectorAll('.image-gallery__thumbnail');
      thumbnails[1].click();
      fixture.detectChanges();

      expect(emitSpy).toHaveBeenCalledWith(1);
    });

    it('should update active image on thumbnail click', () => {
      fixture.componentRef.setInput('images', mockImages);
      fixture.componentRef.setInput('currentIndex', 1);
      fixture.detectChanges();

      const img: HTMLImageElement = fixture.nativeElement.querySelector(
        '.image-gallery__primary-img'
      );
      expect(img.getAttribute('src')).toBe(mockImages[1].fullUrl);
    });
  });

  describe('swipe navigation', () => {
    it('should handle swipe left (next image)', () => {
      fixture.componentRef.setInput('images', mockImages);
      fixture.componentRef.setInput('currentIndex', 0);
      fixture.detectChanges();

      const emitSpy = vi.spyOn(component.imageSelect, 'emit');
      const primaryArea = fixture.nativeElement.querySelector('.image-gallery__primary');

      // Simulate swipe left (start at 200, end at 100 → diff = 100 > threshold)
      const touchStartEvent = new TouchEvent('touchstart', {
        changedTouches: [{ clientX: 200 } as Touch],
      });
      const touchEndEvent = new TouchEvent('touchend', {
        changedTouches: [{ clientX: 100 } as Touch],
      });

      primaryArea.dispatchEvent(touchStartEvent);
      primaryArea.dispatchEvent(touchEndEvent);
      fixture.detectChanges();

      expect(emitSpy).toHaveBeenCalledWith(1);
    });

    it('should handle swipe right (previous image)', () => {
      fixture.componentRef.setInput('images', mockImages);
      fixture.componentRef.setInput('currentIndex', 1);
      fixture.detectChanges();

      const emitSpy = vi.spyOn(component.imageSelect, 'emit');
      const primaryArea = fixture.nativeElement.querySelector('.image-gallery__primary');

      // Simulate swipe right (start at 100, end at 200 → diff = -100 < -threshold)
      const touchStartEvent = new TouchEvent('touchstart', {
        changedTouches: [{ clientX: 100 } as Touch],
      });
      const touchEndEvent = new TouchEvent('touchend', {
        changedTouches: [{ clientX: 200 } as Touch],
      });

      primaryArea.dispatchEvent(touchStartEvent);
      primaryArea.dispatchEvent(touchEndEvent);
      fixture.detectChanges();

      expect(emitSpy).toHaveBeenCalledWith(0);
    });

    it('should wrap to first image when swiping left at the last image', () => {
      fixture.componentRef.setInput('images', mockImages);
      fixture.componentRef.setInput('currentIndex', 2);
      fixture.detectChanges();

      const emitSpy = vi.spyOn(component.imageSelect, 'emit');
      const primaryArea = fixture.nativeElement.querySelector('.image-gallery__primary');

      const touchStartEvent = new TouchEvent('touchstart', {
        changedTouches: [{ clientX: 200 } as Touch],
      });
      const touchEndEvent = new TouchEvent('touchend', {
        changedTouches: [{ clientX: 100 } as Touch],
      });

      primaryArea.dispatchEvent(touchStartEvent);
      primaryArea.dispatchEvent(touchEndEvent);
      fixture.detectChanges();

      expect(emitSpy).toHaveBeenCalledWith(0);
    });
  });

  describe('preload links', () => {
    it('should preload adjacent images after view init', () => {
      fixture.componentRef.setInput('images', mockImages);
      fixture.componentRef.setInput('currentIndex', 1);
      fixture.detectChanges();

      // selectImage triggers preloadAdjacentImages which updates internal links
      component.selectImage(1);

      // Access the internal preloadLinks array
      const preloadLinks = (component as unknown as { preloadLinks: HTMLLinkElement[] }).preloadLinks;
      const hrefs = preloadLinks.map((link) => link.getAttribute('href'));

      // At index 1, adjacent images are index 0 and index 2
      expect(hrefs).toContain(mockImages[0].fullUrl);
      expect(hrefs).toContain(mockImages[2].fullUrl);
      expect(preloadLinks.length).toBe(2);
    });

    it('should preload only next image when at first position', () => {
      fixture.componentRef.setInput('images', mockImages);
      fixture.componentRef.setInput('currentIndex', 0);
      fixture.detectChanges();

      // selectImage triggers preloadAdjacentImages
      component.selectImage(0);

      const preloadLinks = (component as unknown as { preloadLinks: HTMLLinkElement[] }).preloadLinks;
      const hrefs = preloadLinks.map((link) => link.getAttribute('href'));

      expect(hrefs).toContain(mockImages[1].fullUrl);
      expect(hrefs).not.toContain(mockImages[2].fullUrl);
      expect(preloadLinks.length).toBe(1);
    });

    it('should clean up preload links on destroy', () => {
      fixture.componentRef.setInput('images', mockImages);
      fixture.componentRef.setInput('currentIndex', 1);
      fixture.detectChanges();

      // Trigger preloading
      component.selectImage(1);

      const preloadLinks = (component as unknown as { preloadLinks: HTMLLinkElement[] }).preloadLinks;
      expect(preloadLinks.length).toBeGreaterThan(0);

      // Spy on remove to verify cleanup
      const removeSpy = vi.fn();
      preloadLinks.forEach((link) => {
        link.remove = removeSpy;
      });

      fixture.destroy();

      expect(removeSpy).toHaveBeenCalled();
    });
  });

  describe('keyboard navigation', () => {
    it('should navigate to next image on ArrowRight key', () => {
      fixture.componentRef.setInput('images', mockImages);
      fixture.componentRef.setInput('currentIndex', 0);
      fixture.detectChanges();

      const emitSpy = vi.spyOn(component.imageSelect, 'emit');
      const gallery = fixture.nativeElement.querySelector('.image-gallery');

      gallery.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowRight' }));
      fixture.detectChanges();

      expect(emitSpy).toHaveBeenCalledWith(1);
    });

    it('should navigate to previous image on ArrowLeft key', () => {
      fixture.componentRef.setInput('images', mockImages);
      fixture.componentRef.setInput('currentIndex', 1);
      fixture.detectChanges();

      const emitSpy = vi.spyOn(component.imageSelect, 'emit');
      const gallery = fixture.nativeElement.querySelector('.image-gallery');

      gallery.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowLeft' }));
      fixture.detectChanges();

      expect(emitSpy).toHaveBeenCalledWith(0);
    });

    it('should wrap to last image when pressing ArrowLeft at first image', () => {
      fixture.componentRef.setInput('images', mockImages);
      fixture.componentRef.setInput('currentIndex', 0);
      fixture.detectChanges();

      const emitSpy = vi.spyOn(component.imageSelect, 'emit');
      const gallery = fixture.nativeElement.querySelector('.image-gallery');

      gallery.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowLeft' }));
      fixture.detectChanges();

      expect(emitSpy).toHaveBeenCalledWith(2);
    });
  });
});
