import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { BreadcrumbSegment } from '../../../../storefront/catalog/models';

@Component({
  selector: 'app-breadcrumb',
  standalone: true,
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.scss'],
})
export class BreadcrumbComponent {
  readonly segments = input.required<BreadcrumbSegment[]>();

  isLast(index: number): boolean {
    return index === this.segments().length - 1;
  }
}
