import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  inject,
  input,
  OnChanges,
  output,
  signal,
  SimpleChanges,
  viewChild,
} from '@angular/core';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CurrencyPipe } from '@angular/common';

import { ProductSummary } from '../../../../../core/models';
import { SEARCH_DEBOUNCE_MS } from '../../models';

@Component({
  selector: 'app-search-overlay',
  standalone: true,
  imports: [CurrencyPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './search-overlay.component.html',
  styleUrls: ['./search-overlay.component.scss'],
})
export class SearchOverlayComponent implements AfterViewInit, OnChanges {
  readonly open = input<boolean>(false);
  readonly suggestions = input<ProductSummary[]>([]);

  readonly search = output<string>();
  readonly close = output<void>();

  protected readonly searchInput = viewChild<ElementRef<HTMLInputElement>>('searchInput');
  protected readonly activeIndex = signal<number>(-1);
  protected readonly searchValue = signal<string>('');

  private readonly searchSubject = new Subject<string>();
  private readonly destroyRef = inject(DestroyRef);

  constructor() {
    this.searchSubject
      .pipe(
        debounceTime(SEARCH_DEBOUNCE_MS),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((query) => {
        this.search.emit(query);
      });
  }

  ngAfterViewInit(): void {
    this.focusInputIfOpen();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['open'] && this.open()) {
      // Delay focus to next tick so the element is visible
      setTimeout(() => this.focusInputIfOpen(), 0);
    }
    if (changes['open'] && !this.open()) {
      this.activeIndex.set(-1);
      this.searchValue.set('');
    }
  }

  onInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchValue.set(value);
    this.activeIndex.set(-1);
    this.searchSubject.next(value);
  }

  onKeydown(event: KeyboardEvent): void {
    const suggestionsCount = this.suggestions().length;

    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        this.activeIndex.update((i) => (i < suggestionsCount - 1 ? i + 1 : 0));
        break;

      case 'ArrowUp':
        event.preventDefault();
        this.activeIndex.update((i) => (i > 0 ? i - 1 : suggestionsCount - 1));
        break;

      case 'Enter':
        event.preventDefault();
        if (this.activeIndex() >= 0 && this.activeIndex() < suggestionsCount) {
          this.selectSuggestion(this.suggestions()[this.activeIndex()]);
        } else {
          this.search.emit(this.searchValue());
        }
        break;

      case 'Escape':
        event.preventDefault();
        this.close.emit();
        break;
    }
  }

  onBackdropClick(): void {
    this.close.emit();
  }

  selectSuggestion(suggestion: ProductSummary): void {
    this.searchValue.set(suggestion.name);
    this.search.emit(suggestion.name);
    this.close.emit();
  }

  getActiveDescendantId(): string | null {
    return this.activeIndex() >= 0 ? `search-suggestion-${this.activeIndex()}` : null;
  }

  private focusInputIfOpen(): void {
    if (this.open()) {
      this.searchInput()?.nativeElement.focus();
    }
  }
}
