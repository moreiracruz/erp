import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  input,
  output,
  signal,
  OnDestroy,
  ViewChild,
  AfterViewInit,
} from '@angular/core';

import { ProductImage } from '../../../../../core/models';

@Component({
  selector: 'app-image-gallery',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './image-gallery.component.html',
  styleUrls: ['./image-gallery.component.scss'],
})
export class ImageGalleryComponent implements AfterViewInit, OnDestroy {
  readonly images = input.required<ProductImage[]>();
  readonly currentIndex = input<number>(0);
  readonly imageSelect = output<number>();

  @ViewChild('primaryImage') primaryImageRef!: ElementRef<HTMLElement>;

  protected readonly internalIndex = signal(0);
  readonly placeholderUrl = 'assets/images/product-placeholder.webp';

  private touchStartX = 0;
  private touchEndX = 0;
  private readonly SWIPE_THRESHOLD = 50;
  private preloadLinks: HTMLLinkElement[] = [];

  ngAfterViewInit(): void {
    this.internalIndex.set(this.currentIndex());
    this.preloadAdjacentImages();
  }

  ngOnDestroy(): void {
    this.removePreloadLinks();
  }

  get activeIndex(): number {
    return this.currentIndex();
  }

  selectImage(index: number): void {
    this.imageSelect.emit(index);
    this.internalIndex.set(index);
    this.preloadAdjacentImages();
  }

  onKeyDown(event: KeyboardEvent): void {
    const imgs = this.images();
    if (!imgs.length) return;

    if (event.key === 'ArrowLeft') {
      event.preventDefault();
      const newIndex = this.activeIndex > 0 ? this.activeIndex - 1 : imgs.length - 1;
      this.selectImage(newIndex);
    } else if (event.key === 'ArrowRight') {
      event.preventDefault();
      const newIndex = this.activeIndex < imgs.length - 1 ? this.activeIndex + 1 : 0;
      this.selectImage(newIndex);
    }
  }

  onTouchStart(event: TouchEvent): void {
    this.touchStartX = event.changedTouches[0].clientX;
  }

  onTouchEnd(event: TouchEvent): void {
    this.touchEndX = event.changedTouches[0].clientX;
    this.handleSwipe();
  }

  getAltText(index: number): string {
    const imgs = this.images();
    if (!imgs.length) return 'Imagem do produto indisponível';
    return `Produto imagem ${index + 1} de ${imgs.length}`;
  }

  private handleSwipe(): void {
    const diff = this.touchStartX - this.touchEndX;
    const imgs = this.images();
    if (!imgs.length) return;

    if (Math.abs(diff) < this.SWIPE_THRESHOLD) return;

    if (diff > 0) {
      // Swipe left → next
      const newIndex = this.activeIndex < imgs.length - 1 ? this.activeIndex + 1 : 0;
      this.selectImage(newIndex);
    } else {
      // Swipe right → prev
      const newIndex = this.activeIndex > 0 ? this.activeIndex - 1 : imgs.length - 1;
      this.selectImage(newIndex);
    }
  }

  private preloadAdjacentImages(): void {
    this.removePreloadLinks();
    const imgs = this.images();
    const idx = this.activeIndex;

    const indicesToPreload: number[] = [];
    if (idx > 0) indicesToPreload.push(idx - 1);
    if (idx < imgs.length - 1) indicesToPreload.push(idx + 1);

    indicesToPreload.forEach((i) => {
      const link = document.createElement('link');
      link.rel = 'preload';
      link.as = 'image';
      link.href = imgs[i].fullUrl;
      document.head.appendChild(link);
      this.preloadLinks.push(link);
    });
  }

  private removePreloadLinks(): void {
    this.preloadLinks.forEach((link) => link.remove());
    this.preloadLinks = [];
  }
}
