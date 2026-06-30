import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  output,
  signal,
} from '@angular/core';

export interface FileValidationError {
  fileName: string;
  message: string;
}

const ACCEPTED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
const MAX_FILE_SIZE = 5_242_880; // 5MB

@Component({
  selector: 'app-upload-zone',
  standalone: true,
  templateUrl: './upload-zone.component.html',
  styleUrls: ['./upload-zone.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UploadZoneComponent {
  /** Whether an upload is currently in progress */
  readonly uploading = input<boolean>(false);

  /** Upload progress percentage (0-100) */
  readonly uploadProgress = input<number>(0);

  /** Server-side error messages to display */
  readonly errors = input<string[]>([]);

  /** Thumbnail URL to display after successful upload */
  readonly previewUrl = input<string | null>(null);

  /** Emits validated files ready for upload */
  readonly filesSelected = output<File[]>();

  protected readonly dragOver = signal(false);
  protected readonly validationErrors = signal<FileValidationError[]>([]);

  protected readonly hasErrors = computed(
    () => this.validationErrors().length > 0 || this.errors().length > 0,
  );

  protected readonly acceptedExtensions = '.jpg,.jpeg,.png,.webp';

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.dragOver.set(true);
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.dragOver.set(false);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.dragOver.set(false);

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.processFiles(Array.from(files));
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.processFiles(Array.from(input.files));
      input.value = '';
    }
  }

  private processFiles(files: File[]): void {
    const errors: FileValidationError[] = [];
    const validFiles: File[] = [];

    for (const file of files) {
      const typeValid = ACCEPTED_TYPES.includes(file.type);
      const sizeValid = file.size <= MAX_FILE_SIZE;

      if (!typeValid) {
        errors.push({
          fileName: file.name,
          message: `Tipo de arquivo não aceito. Aceitos: JPEG, PNG, WebP.`,
        });
      } else if (!sizeValid) {
        errors.push({
          fileName: file.name,
          message: `Arquivo excede o tamanho máximo de 5MB.`,
        });
      } else {
        validFiles.push(file);
      }
    }

    this.validationErrors.set(errors);

    if (validFiles.length > 0) {
      this.filesSelected.emit(validFiles);
    }
  }
}
