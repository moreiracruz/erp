import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  input,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

import { ProductImage } from '../../../../../core/models';
import { ImagePort } from '../../../../../core/ports/image.port';
import { UploadZoneComponent } from '../upload-zone/upload-zone.component';
import { ImageGridComponent } from '../image-grid/image-grid.component';

@Component({
  selector: 'app-product-image-section',
  standalone: true,
  imports: [UploadZoneComponent, ImageGridComponent],
  templateUrl: './product-image-section.component.html',
  styleUrls: ['./product-image-section.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductImageSectionComponent implements OnInit {
  readonly productUuid = input.required<string>();

  private readonly imagePort = inject(ImagePort);
  private readonly destroyRef = inject(DestroyRef);

  readonly images = signal<ProductImage[]>([]);
  readonly uploading = signal(false);
  readonly uploadProgress = signal(0);
  readonly toastMessage = signal<string | null>(null);

  private toastTimeout: ReturnType<typeof setTimeout> | null = null;

  ngOnInit(): void {
    this.loadImages();
  }

  onFilesSelected(files: File[]): void {
    this.uploading.set(true);
    this.uploadProgress.set(0);

    this.imagePort
      .upload(this.productUuid(), files)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.uploading.set(false);
          this.uploadProgress.set(0);
        }),
      )
      .subscribe({
        next: () => this.loadImages(),
        error: (err) => this.showToast(err?.error?.detail || err?.error?.error || 'Erro ao enviar imagens'),
      });
  }

  onReorder(imageIds: number[]): void {
    this.imagePort
      .reorder(this.productUuid(), imageIds)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => this.images.set(updated),
        error: (err) => this.showToast(err?.error?.detail || err?.error?.error || 'Erro ao reordenar imagens'),
      });
  }

  onSetMain(imageId: number): void {
    this.imagePort
      .setMain(this.productUuid(), imageId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.loadImages(),
        error: (err) => this.showToast(err?.error?.detail || err?.error?.error || 'Erro ao definir imagem principal'),
      });
  }

  onDelete(imageId: number): void {
    this.imagePort
      .delete(this.productUuid(), imageId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.loadImages(),
        error: (err) => this.showToast(err?.error?.detail || err?.error?.error || 'Erro ao excluir imagem'),
      });
  }

  dismissToast(): void {
    this.toastMessage.set(null);
    if (this.toastTimeout) {
      clearTimeout(this.toastTimeout);
      this.toastTimeout = null;
    }
  }

  private loadImages(): void {
    this.imagePort
      .listByProduct(this.productUuid())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (images) => this.images.set(images),
        error: (err) => this.showToast(err?.error?.detail || err?.error?.error || 'Erro ao carregar imagens'),
      });
  }

  private showToast(message: string): void {
    if (this.toastTimeout) {
      clearTimeout(this.toastTimeout);
    }
    this.toastMessage.set(message);
    this.toastTimeout = setTimeout(() => {
      this.toastMessage.set(null);
      this.toastTimeout = null;
    }, 5000);
  }
}
