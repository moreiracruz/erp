import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-shimmer-placeholder',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @for (item of items(); track $index) {
      <div
        class="shimmer-placeholder"
        [style.width]="width()"
        [style.height]="height()"
        aria-hidden="true"
      ></div>
    }
  `,
  styleUrls: ['./shimmer-placeholder.component.scss'],
})
export class ShimmerPlaceholderComponent {
  readonly width = input<string>('100%');
  readonly height = input<string>('200px');
  readonly count = input<number>(1);

  protected readonly items = computed(() => Array.from({ length: this.count() }));
}
