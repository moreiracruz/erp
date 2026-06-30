import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ImageGridComponent } from './image-grid.component';
import { ProductImage } from '../../../../../core/models';

describe('ImageGridComponent', () => {
  let component: ImageGridComponent;
  let fixture: ComponentFixture<ImageGridComponent>;

  const mockImages: ProductImage[] = [
    {
      id: 1,
      filename: 'img1',
      originalName: 'photo1.jpg',
      contentType: 'image/jpeg',
      fileSize: 100000,
      sortOrder: 0,
      main: true,
      createdAt: '2024-01-01T00:00:00Z',
      thumbnailUrl: '/uploads/products/uuid1/img1_thumb.jpg',
      cardUrl: '/uploads/products/uuid1/img1_card.jpg',
      fullUrl: '/uploads/products/uuid1/img1_full.jpg',
    },
    {
      id: 2,
      filename: 'img2',
      originalName: 'photo2.png',
      contentType: 'image/png',
      fileSize: 200000,
      sortOrder: 1,
      main: false,
      createdAt: '2024-01-02T00:00:00Z',
      thumbnailUrl: '/uploads/products/uuid1/img2_thumb.png',
      cardUrl: '/uploads/products/uuid1/img2_card.png',
      fullUrl: '/uploads/products/uuid1/img2_full.png',
    },
    {
      id: 3,
      filename: 'img3',
      originalName: 'photo3.webp',
      contentType: 'image/webp',
      fileSize: 150000,
      sortOrder: 2,
      main: false,
      createdAt: '2024-01-03T00:00:00Z',
      thumbnailUrl: '/uploads/products/uuid1/img3_thumb.webp',
      cardUrl: '/uploads/products/uuid1/img3_card.webp',
      fullUrl: '/uploads/products/uuid1/img3_full.webp',
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImageGridComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ImageGridComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('images', mockImages);
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('Image Display', () => {
    it('should display all images as thumbnails', () => {
      const thumbnails = fixture.nativeElement.querySelectorAll('.image-grid__thumbnail');
      expect(thumbnails.length).toBe(3);
      expect((thumbnails[0] as HTMLImageElement).src).toContain('img1_thumb.jpg');
      expect((thumbnails[1] as HTMLImageElement).src).toContain('img2_thumb.png');
      expect((thumbnails[2] as HTMLImageElement).src).toContain('img3_thumb.webp');
    });

    it('should show filled star (svg with fill) for the main image', () => {
      const mainButtons = fixture.nativeElement.querySelectorAll('.image-grid__btn--main');
      const mainButton = mainButtons[0] as HTMLElement;
      expect(mainButton.classList.contains('image-grid__btn--active')).toBe(true);
      // The first image (main) has svg with fill="currentColor"
      const svg = mainButton.querySelector('svg');
      expect(svg?.getAttribute('fill')).toBe('currentColor');
    });

    it('should show outline star (svg with stroke) for non-main images', () => {
      const mainButtons = fixture.nativeElement.querySelectorAll('.image-grid__btn--main');
      const nonMainButton = mainButtons[1] as HTMLElement;
      expect(nonMainButton.classList.contains('image-grid__btn--active')).toBe(false);
      // Non-main images have svg with fill="none" stroke="currentColor"
      const svg = nonMainButton.querySelector('svg');
      expect(svg?.getAttribute('fill')).toBe('none');
      expect(svg?.getAttribute('stroke')).toBe('currentColor');
    });
  });

  describe('Set Main', () => {
    it('should emit setMain with image ID when star button is clicked', () => {
      const emitSpy = vi.spyOn(component.setMain, 'emit');
      const mainButtons = fixture.nativeElement.querySelectorAll('.image-grid__btn--main');
      (mainButtons[1] as HTMLElement).click();

      expect(emitSpy).toHaveBeenCalledWith(2);
    });
  });

  describe('Delete with Confirmation', () => {
    it('should show confirmation overlay when delete button is clicked', () => {
      const deleteButtons = fixture.nativeElement.querySelectorAll('.image-grid__btn--delete');
      (deleteButtons[0] as HTMLElement).click();
      fixture.detectChanges();

      const overlay = fixture.nativeElement.querySelector('.image-grid__confirm-overlay');
      expect(overlay).toBeTruthy();
      expect(overlay.textContent).toContain('Excluir?');
    });

    it('should emit delete with image ID when confirmed', () => {
      const emitSpy = vi.spyOn(component.delete, 'emit');

      // Click delete button for the first image
      const deleteButtons = fixture.nativeElement.querySelectorAll('.image-grid__btn--delete');
      (deleteButtons[0] as HTMLElement).click();
      fixture.detectChanges();

      // Click "Sim" (yes) button
      const confirmBtn = fixture.nativeElement.querySelector('.image-grid__confirm-btn--yes') as HTMLElement;
      confirmBtn.click();

      expect(emitSpy).toHaveBeenCalledWith(1);
    });

    it('should hide confirmation overlay when cancelled', () => {
      // Click delete button to show overlay
      const deleteButtons = fixture.nativeElement.querySelectorAll('.image-grid__btn--delete');
      (deleteButtons[0] as HTMLElement).click();
      fixture.detectChanges();

      expect(fixture.nativeElement.querySelector('.image-grid__confirm-overlay')).toBeTruthy();

      // Click "Não" (no) button
      const cancelBtn = fixture.nativeElement.querySelector('.image-grid__confirm-btn--no') as HTMLElement;
      cancelBtn.click();
      fixture.detectChanges();

      expect(fixture.nativeElement.querySelector('.image-grid__confirm-overlay')).toBeNull();
    });

    it('should not emit delete when cancelled', () => {
      const emitSpy = vi.spyOn(component.delete, 'emit');

      const deleteButtons = fixture.nativeElement.querySelectorAll('.image-grid__btn--delete');
      (deleteButtons[1] as HTMLElement).click();
      fixture.detectChanges();

      const cancelBtn = fixture.nativeElement.querySelector('.image-grid__confirm-btn--no') as HTMLElement;
      cancelBtn.click();

      expect(emitSpy).not.toHaveBeenCalled();
    });
  });

  describe('Reorder', () => {
    it('should emit reorder with new ID order after drop', () => {
      const emitSpy = vi.spyOn(component.reorder, 'emit');

      // Simulate CdkDragDrop by calling onDrop directly
      component.onDrop({
        previousIndex: 0,
        currentIndex: 2,
        item: {} as any,
        container: {} as any,
        previousContainer: {} as any,
        isPointerOverContainer: true,
        distance: { x: 0, y: 0 },
        dropPoint: { x: 0, y: 0 },
        event: new MouseEvent('drop'),
      });

      // Moving index 0 to index 2: [1,2,3] -> [2,3,1]
      expect(emitSpy).toHaveBeenCalledWith([2, 3, 1]);
    });
  });
});
