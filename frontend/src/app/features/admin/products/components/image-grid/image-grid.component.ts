import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';
import { CdkDragDrop, CdkDrag, CdkDragPlaceholder, CdkDropList, moveItemInArray } from '@angular/cdk/drag-drop';

import { ProductImage } from '../../../../../core/models';

@Component({
  selector: 'app-image-grid',
  standalone: true,
  imports: [CdkDrag, CdkDragPlaceholder, CdkDropList],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './image-grid.component.html',
  styleUrls: ['./image-grid.component.scss'],
})
export class ImageGridComponent {
  readonly images = input.required<ProductImage[]>();

  readonly reorder = output<number[]>();
  readonly setMain = output<number>();
  readonly delete = output<number>();

  readonly confirmingDeleteId = signal<number | null>(null);

  onDrop(event: CdkDragDrop<ProductImage[]>): void {
    const items = [...this.images()];
    moveItemInArray(items, event.previousIndex, event.currentIndex);
    this.reorder.emit(items.map((img) => img.id));
  }

  onSetMain(imageId: number): void {
    this.setMain.emit(imageId);
  }

  onDeleteClick(imageId: number): void {
    this.confirmingDeleteId.set(imageId);
  }

  confirmDelete(imageId: number): void {
    this.delete.emit(imageId);
    this.confirmingDeleteId.set(null);
  }

  cancelDelete(): void {
    this.confirmingDeleteId.set(null);
  }
}
