import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UploadZoneComponent } from './upload-zone.component';

/**
 * Creates a mock FileList-like object from an array of files.
 */
function createMockFileList(files: File[]): FileList {
  const fileList = Object.create(FileList.prototype);
  for (let i = 0; i < files.length; i++) {
    fileList[i] = files[i];
  }
  Object.defineProperty(fileList, 'length', { value: files.length });
  fileList.item = (index: number) => files[index] ?? null;
  fileList[Symbol.iterator] = function* () {
    for (const file of files) yield file;
  };
  return fileList;
}

/** Helper to create a fake input change event with files */
function createInputEvent(files: File[]): Event {
  const input = document.createElement('input');
  input.type = 'file';
  Object.defineProperty(input, 'files', { value: createMockFileList(files) });
  const event = new Event('change', { bubbles: true });
  Object.defineProperty(event, 'target', { value: input });
  return event;
}

/** Helper to create a mock DragEvent */
function createDragEvent(type: string, files?: File[]): Event {
  const event = new Event(type, { bubbles: true, cancelable: true });
  Object.defineProperty(event, 'preventDefault', { value: vi.fn() });
  Object.defineProperty(event, 'stopPropagation', { value: vi.fn() });
  if (files) {
    Object.defineProperty(event, 'dataTransfer', {
      value: { files: createMockFileList(files) },
    });
  }
  return event;
}

describe('UploadZoneComponent', () => {
  let component: UploadZoneComponent;
  let fixture: ComponentFixture<UploadZoneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UploadZoneComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(UploadZoneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('File Validation', () => {
    it('should accept a valid JPEG file and emit via filesSelected', () => {
      const emitSpy = vi.spyOn(component.filesSelected, 'emit');
      const file = new File(['content'], 'photo.jpg', { type: 'image/jpeg' });
      Object.defineProperty(file, 'size', { value: 1024 * 1024 }); // 1MB

      component.onFileSelected(createInputEvent([file]));

      expect(emitSpy).toHaveBeenCalledWith([file]);
    });

    it('should accept a valid PNG file', () => {
      const emitSpy = vi.spyOn(component.filesSelected, 'emit');
      const file = new File(['content'], 'image.png', { type: 'image/png' });
      Object.defineProperty(file, 'size', { value: 2 * 1024 * 1024 }); // 2MB

      component.onFileSelected(createInputEvent([file]));

      expect(emitSpy).toHaveBeenCalledWith([file]);
    });

    it('should accept a valid WebP file', () => {
      const emitSpy = vi.spyOn(component.filesSelected, 'emit');
      const file = new File(['content'], 'image.webp', { type: 'image/webp' });
      Object.defineProperty(file, 'size', { value: 500_000 });

      component.onFileSelected(createInputEvent([file]));

      expect(emitSpy).toHaveBeenCalledWith([file]);
    });

    it('should reject a file with invalid type and show error', () => {
      const emitSpy = vi.spyOn(component.filesSelected, 'emit');
      const file = new File(['content'], 'readme.txt', { type: 'text/plain' });
      Object.defineProperty(file, 'size', { value: 1024 });

      component.onFileSelected(createInputEvent([file]));

      expect(emitSpy).not.toHaveBeenCalled();
      fixture.detectChanges();

      const errorList = fixture.nativeElement.querySelector('.upload-zone__errors');
      expect(errorList).toBeTruthy();
      expect(errorList.textContent).toContain('readme.txt');
      expect(errorList.textContent).toContain('Tipo de arquivo não aceito');
    });

    it('should reject a file exceeding 5MB and show error', () => {
      const emitSpy = vi.spyOn(component.filesSelected, 'emit');
      const file = new File(['content'], 'large.jpg', { type: 'image/jpeg' });
      Object.defineProperty(file, 'size', { value: 6 * 1024 * 1024 }); // 6MB

      component.onFileSelected(createInputEvent([file]));

      expect(emitSpy).not.toHaveBeenCalled();
      fixture.detectChanges();

      const errorList = fixture.nativeElement.querySelector('.upload-zone__errors');
      expect(errorList).toBeTruthy();
      expect(errorList.textContent).toContain('large.jpg');
      expect(errorList.textContent).toContain('5MB');
    });

    it('should not emit invalid files but emit valid ones from a mixed batch', () => {
      const emitSpy = vi.spyOn(component.filesSelected, 'emit');
      const validFile = new File(['content'], 'ok.png', { type: 'image/png' });
      Object.defineProperty(validFile, 'size', { value: 1024 });
      const invalidFile = new File(['content'], 'bad.pdf', { type: 'application/pdf' });
      Object.defineProperty(invalidFile, 'size', { value: 1024 });

      component.onFileSelected(createInputEvent([validFile, invalidFile]));

      expect(emitSpy).toHaveBeenCalledWith([validFile]);
      fixture.detectChanges();

      const errorList = fixture.nativeElement.querySelector('.upload-zone__errors');
      expect(errorList).toBeTruthy();
      expect(errorList.textContent).toContain('bad.pdf');
    });
  });

  describe('Progress Display', () => {
    it('should show progress bar when uploading is true', () => {
      fixture.componentRef.setInput('uploading', true);
      fixture.componentRef.setInput('uploadProgress', 50);
      fixture.detectChanges();

      const progressBar = fixture.nativeElement.querySelector('[role="progressbar"]');
      expect(progressBar).toBeTruthy();
      expect(progressBar.getAttribute('aria-valuenow')).toBe('50');

      const percentageText = fixture.nativeElement.querySelector('.upload-zone__progress-percentage');
      expect(percentageText?.textContent).toContain('50%');
    });

    it('should not show progress bar when uploading is false', () => {
      fixture.componentRef.setInput('uploading', false);
      fixture.detectChanges();

      const progressBar = fixture.nativeElement.querySelector('[role="progressbar"]');
      expect(progressBar).toBeNull();
    });
  });

  describe('Preview Display', () => {
    it('should show preview image when previewUrl is set', () => {
      fixture.componentRef.setInput('previewUrl', 'http://example.com/thumb.jpg');
      fixture.detectChanges();

      const previewImg = fixture.nativeElement.querySelector('.upload-zone__preview-image') as HTMLImageElement;
      expect(previewImg).toBeTruthy();
      expect(previewImg.src).toBe('http://example.com/thumb.jpg');
    });

    it('should not show preview when previewUrl is null', () => {
      fixture.componentRef.setInput('previewUrl', null);
      fixture.detectChanges();

      const previewImg = fixture.nativeElement.querySelector('.upload-zone__preview-image');
      expect(previewImg).toBeNull();
    });
  });

  describe('Drag and Drop', () => {
    it('should set dragOver on dragover and clear on dragleave', () => {
      // Call methods directly since jsdom doesn't have DragEvent
      component.onDragOver(createDragEvent('dragover') as unknown as DragEvent);
      fixture.detectChanges();

      const zone = fixture.nativeElement.querySelector('.upload-zone') as HTMLElement;
      expect(zone.classList.contains('upload-zone--drag-over')).toBe(true);

      component.onDragLeave(createDragEvent('dragleave') as unknown as DragEvent);
      fixture.detectChanges();

      expect(zone.classList.contains('upload-zone--drag-over')).toBe(false);
    });

    it('should process files on drop event', () => {
      const emitSpy = vi.spyOn(component.filesSelected, 'emit');
      const file = new File(['content'], 'dropped.jpg', { type: 'image/jpeg' });
      Object.defineProperty(file, 'size', { value: 1024 });

      const dropEvent = createDragEvent('drop', [file]);
      component.onDrop(dropEvent as unknown as DragEvent);

      expect(emitSpy).toHaveBeenCalledWith([file]);
    });
  });
});
