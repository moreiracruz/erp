import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { ProductImageSectionComponent } from './product-image-section.component';
import { ImagePort } from '../../../../../core/ports/image.port';
import { ProductImage } from '../../../../../core/models';

interface MockImagePort {
  listByProduct: ReturnType<typeof vi.fn>;
  upload: ReturnType<typeof vi.fn>;
  delete: ReturnType<typeof vi.fn>;
  reorder: ReturnType<typeof vi.fn>;
  setMain: ReturnType<typeof vi.fn>;
}

describe('ProductImageSectionComponent', () => {
  let component: ProductImageSectionComponent;
  let fixture: ComponentFixture<ProductImageSectionComponent>;
  let imagePortSpy: MockImagePort;

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
  ];

  beforeEach(async () => {
    const spy: MockImagePort = {
      listByProduct: vi.fn().mockReturnValue(of(mockImages)),
      upload: vi.fn().mockReturnValue(of(mockImages)),
      delete: vi.fn().mockReturnValue(of(undefined)),
      reorder: vi.fn().mockReturnValue(of(mockImages)),
      setMain: vi.fn().mockReturnValue(of(mockImages[0])),
    };

    await TestBed.configureTestingModule({
      imports: [ProductImageSectionComponent],
      providers: [{ provide: ImagePort, useValue: spy }],
    }).compileComponents();

    imagePortSpy = TestBed.inject(ImagePort) as unknown as MockImagePort;
    fixture = TestBed.createComponent(ProductImageSectionComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('productUuid', 'test-uuid-123');
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('Rendering', () => {
    it('should render the upload zone', () => {
      const uploadZone = fixture.nativeElement.querySelector('app-upload-zone');
      expect(uploadZone).toBeTruthy();
    });

    it('should render image grid when images are loaded', () => {
      const imageGrid = fixture.nativeElement.querySelector('app-image-grid');
      expect(imageGrid).toBeTruthy();
    });

    it('should not render image grid when no images exist', () => {
      (imagePortSpy.listByProduct as unknown as ReturnType<typeof vi.fn>).mockReturnValue(of([]));
      component.ngOnInit();
      fixture.detectChanges();

      const imageGrid = fixture.nativeElement.querySelector('app-image-grid');
      expect(imageGrid).toBeNull();
    });
  });

  describe('Image Loading', () => {
    it('should call listByProduct on init with the product UUID', () => {
      expect(imagePortSpy.listByProduct).toHaveBeenCalledWith('test-uuid-123');
    });

    it('should populate images signal with loaded data', () => {
      expect(component.images()).toEqual(mockImages);
    });
  });

  describe('Error Handling', () => {
    it('should show error toast when upload fails', () => {
      (imagePortSpy.upload as unknown as ReturnType<typeof vi.fn>).mockReturnValue(
        throwError(() => ({ error: { error: 'Erro ao enviar imagens' } })),
      );

      component.onFilesSelected([new File(['x'], 'test.jpg', { type: 'image/jpeg' })]);
      fixture.detectChanges();

      const toast = fixture.nativeElement.querySelector('.product-image-section__toast');
      expect(toast).toBeTruthy();
      expect(toast.textContent).toContain('Erro ao enviar imagens');
    });

    it('should show error toast when listByProduct fails', () => {
      (imagePortSpy.listByProduct as unknown as ReturnType<typeof vi.fn>).mockReturnValue(
        throwError(() => ({ error: { error: 'Erro ao carregar imagens' } })),
      );

      component.ngOnInit();
      fixture.detectChanges();

      const toast = fixture.nativeElement.querySelector('.product-image-section__toast');
      expect(toast).toBeTruthy();
      expect(toast.textContent).toContain('Erro ao carregar imagens');
    });

    it('should show error toast when delete fails', () => {
      (imagePortSpy.delete as unknown as ReturnType<typeof vi.fn>).mockReturnValue(
        throwError(() => ({ error: { error: 'Erro ao excluir imagem' } })),
      );

      component.onDelete(1);
      fixture.detectChanges();

      const toast = fixture.nativeElement.querySelector('.product-image-section__toast');
      expect(toast).toBeTruthy();
      expect(toast.textContent).toContain('Erro ao excluir imagem');
    });

    it('should dismiss toast when dismiss button is clicked', () => {
      (imagePortSpy.upload as unknown as ReturnType<typeof vi.fn>).mockReturnValue(
        throwError(() => ({ error: { error: 'Upload error' } })),
      );

      component.onFilesSelected([new File(['x'], 'test.jpg', { type: 'image/jpeg' })]);
      fixture.detectChanges();

      const dismissBtn = fixture.nativeElement.querySelector('.product-image-section__toast-dismiss') as HTMLElement;
      dismissBtn.click();
      fixture.detectChanges();

      const toast = fixture.nativeElement.querySelector('.product-image-section__toast');
      expect(toast).toBeNull();
    });
  });
});
