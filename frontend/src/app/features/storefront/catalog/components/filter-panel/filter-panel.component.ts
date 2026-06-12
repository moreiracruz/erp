import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';

import { FilterState, DEFAULT_FILTERS, FilterCounts } from '../../models';

@Component({
  selector: 'app-filter-panel',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './filter-panel.component.html',
  styleUrls: ['./filter-panel.component.scss'],
})
export class FilterPanelComponent {
  readonly filters = input.required<FilterState>();
  readonly counts = input.required<FilterCounts>();
  readonly open = input<boolean>(false);

  readonly filterChange = output<FilterState>();

  protected readonly expandedSections = signal<Record<string, boolean>>({
    category: true,
    size: true,
    color: true,
    price: true,
  });

  protected priceMin = '';
  protected priceMax = '';

  toggleSection(section: string): void {
    this.expandedSections.update((sections) => ({
      ...sections,
      [section]: !sections[section],
    }));
  }

  isSectionExpanded(section: string): boolean {
    return this.expandedSections()[section] ?? false;
  }

  toggleCategory(category: string): void {
    const current = this.filters().categories;
    const updated = current.includes(category)
      ? current.filter((c) => c !== category)
      : [...current, category];

    this.filterChange.emit({ ...this.filters(), categories: updated });
  }

  toggleSize(size: string): void {
    const current = this.filters().sizes;
    const updated = current.includes(size)
      ? current.filter((s) => s !== size)
      : [...current, size];

    this.filterChange.emit({ ...this.filters(), sizes: updated });
  }

  toggleColor(color: string): void {
    const current = this.filters().colors;
    const updated = current.includes(color)
      ? current.filter((c) => c !== color)
      : [...current, color];

    this.filterChange.emit({ ...this.filters(), colors: updated });
  }

  applyPriceRange(): void {
    const min = parseFloat(this.priceMin);
    const max = parseFloat(this.priceMax);

    if (!isNaN(min) && !isNaN(max) && min <= max) {
      this.filterChange.emit({
        ...this.filters(),
        priceRange: { min, max },
      });
    }
  }

  clearPriceRange(): void {
    this.priceMin = '';
    this.priceMax = '';
    this.filterChange.emit({ ...this.filters(), priceRange: null });
  }

  clearAllFilters(): void {
    this.priceMin = '';
    this.priceMax = '';
    this.filterChange.emit(DEFAULT_FILTERS);
  }

  isCategorySelected(category: string): boolean {
    return this.filters().categories.includes(category);
  }

  isSizeSelected(size: string): boolean {
    return this.filters().sizes.includes(size);
  }

  isColorSelected(color: string): boolean {
    return this.filters().colors.includes(color);
  }

  hasActiveFilters(): boolean {
    const f = this.filters();
    return (
      f.categories.length > 0 ||
      f.sizes.length > 0 ||
      f.colors.length > 0 ||
      f.priceRange !== null
    );
  }

  getCategoryKeys(): string[] {
    return Object.keys(this.counts().categories);
  }

  getSizeKeys(): string[] {
    return Object.keys(this.counts().sizes);
  }

  getColorKeys(): string[] {
    return Object.keys(this.counts().colors);
  }
}
