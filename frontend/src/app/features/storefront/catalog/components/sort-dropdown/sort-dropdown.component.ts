import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

import { SortOption } from '../../models';

@Component({
  selector: 'app-sort-dropdown',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './sort-dropdown.component.html',
  styleUrls: ['./sort-dropdown.component.scss'],
})
export class SortDropdownComponent {
  readonly currentSort = input.required<SortOption>();
  readonly sortChange = output<SortOption>();

  protected readonly sortOptions: { value: SortOption; label: string }[] = [
    { value: 'newest', label: 'Mais recentes' },
    { value: 'price-asc', label: 'Preço: menor primeiro' },
    { value: 'price-desc', label: 'Preço: maior primeiro' },
    { value: 'popularity', label: 'Popularidade' },
  ];

  onSortChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value as SortOption;
    this.sortChange.emit(value);
  }
}
