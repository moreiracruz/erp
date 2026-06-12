import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  input,
  output,
  AfterViewChecked,
  ViewChild,
  OnDestroy,
} from '@angular/core';

import { SizeGuideEntry } from '../../../catalog/models';

@Component({
  selector: 'app-size-guide-modal',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './size-guide-modal.component.html',
  styleUrls: ['./size-guide-modal.component.scss'],
})
export class SizeGuideModalComponent implements AfterViewChecked, OnDestroy {
  readonly sizes = input.required<SizeGuideEntry[]>();
  readonly open = input<boolean>(false);
  readonly close = output<void>();

  @ViewChild('dialog') dialogRef!: ElementRef<HTMLElement>;
  @ViewChild('closeBtn') closeBtnRef!: ElementRef<HTMLButtonElement>;

  private previouslyFocusedElement: HTMLElement | null = null;
  private focusTrapListener: ((e: KeyboardEvent) => void) | null = null;

  ngAfterViewChecked(): void {
    if (this.open() && this.dialogRef) {
      this.setupFocusTrap();
    }
  }

  ngOnDestroy(): void {
    this.cleanupFocusTrap();
  }

  onOverlayClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('size-guide-modal__overlay')) {
      this.close.emit();
    }
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      event.preventDefault();
      this.close.emit();
    }
  }

  onCloseClick(): void {
    this.close.emit();
  }

  private setupFocusTrap(): void {
    if (this.focusTrapListener) return;

    this.previouslyFocusedElement = document.activeElement as HTMLElement;

    setTimeout(() => {
      this.closeBtnRef?.nativeElement?.focus();
    });

    this.focusTrapListener = (e: KeyboardEvent) => {
      if (e.key !== 'Tab' || !this.dialogRef) return;

      const dialog = this.dialogRef.nativeElement;
      const focusableElements = dialog.querySelectorAll<HTMLElement>(
        'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
      );

      if (focusableElements.length === 0) return;

      const firstEl = focusableElements[0];
      const lastEl = focusableElements[focusableElements.length - 1];

      if (e.shiftKey && document.activeElement === firstEl) {
        e.preventDefault();
        lastEl.focus();
      } else if (!e.shiftKey && document.activeElement === lastEl) {
        e.preventDefault();
        firstEl.focus();
      }
    };

    document.addEventListener('keydown', this.focusTrapListener);
  }

  private cleanupFocusTrap(): void {
    if (this.focusTrapListener) {
      document.removeEventListener('keydown', this.focusTrapListener);
      this.focusTrapListener = null;
    }
    this.previouslyFocusedElement?.focus();
  }
}
