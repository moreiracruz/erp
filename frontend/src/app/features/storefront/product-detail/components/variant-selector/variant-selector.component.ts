import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { CurrencyPipe } from '@angular/common';

import { Variant } from '../../../../../core/models';
import { SelectedVariant } from '../../../catalog/models';
import {
  getAvailableSizes,
  getAvailableColors,
  getDisabledSizes,
  getDisabledColors,
  getVariantPrice,
} from '../../../catalog/utils';

@Component({
  selector: 'app-variant-selector',
  standalone: true,
  imports: [CurrencyPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './variant-selector.component.html',
  styleUrls: ['./variant-selector.component.scss'],
})
export class VariantSelectorComponent {
  readonly variants = input.required<Variant[]>();
  readonly selected = input.required<SelectedVariant>();
  readonly variantChange = output<SelectedVariant>();

  readonly availableSizes = computed(() => getAvailableSizes(this.variants()));
  readonly availableColors = computed(() => getAvailableColors(this.variants()));

  readonly disabledSizes = computed(() => {
    const sel = this.selected();
    return sel.color ? getDisabledSizes(this.variants(), sel.color) : [];
  });

  readonly disabledColors = computed(() => {
    const sel = this.selected();
    return sel.size ? getDisabledColors(this.variants(), sel.size) : [];
  });

  readonly announcementText = computed(() => {
    const sel = this.selected();
    if (!sel.size || !sel.color) return '';
    const price = getVariantPrice(this.variants(), sel.size, sel.color);
    if (price === null) return '';
    return `Tamanho ${sel.size}, Cor ${sel.color} selecionados - R$ ${price.toFixed(2).replace('.', ',')}`;
  });

  selectSize(size: string): void {
    if (this.disabledSizes().includes(size)) return;
    const sel = this.selected();
    const newColor = sel.color;
    const variant = this.findVariant(size, newColor);
    this.variantChange.emit({ size, color: newColor, variant });
  }

  selectColor(color: string): void {
    if (this.disabledColors().includes(color)) return;
    const sel = this.selected();
    const newSize = sel.size;
    const variant = this.findVariant(newSize, color);
    this.variantChange.emit({ size: newSize, color, variant });
  }

  isSizeDisabled(size: string): boolean {
    return this.disabledSizes().includes(size);
  }

  isColorDisabled(color: string): boolean {
    return this.disabledColors().includes(color);
  }

  private findVariant(size: string | null, color: string | null): Variant | null {
    if (!size || !color) return null;
    return this.variants().find((v) => v.active && v.size === size && v.color === color) ?? null;
  }
}
