import { Variant } from '../../../../core/models';
import { SelectedVariant } from '../models';

/**
 * Returns distinct sizes from active variants, preserving insertion order.
 */
export function getAvailableSizes(variants: Variant[]): string[] {
  const seen = new Set<string>();
  const sizes: string[] = [];

  for (const v of variants) {
    if (v.active && !seen.has(v.size)) {
      seen.add(v.size);
      sizes.push(v.size);
    }
  }

  return sizes;
}

/**
 * Returns distinct colors from active variants, preserving insertion order.
 */
export function getAvailableColors(variants: Variant[]): string[] {
  const seen = new Set<string>();
  const colors: string[] = [];

  for (const v of variants) {
    if (v.active && !seen.has(v.color)) {
      seen.add(v.color);
      colors.push(v.color);
    }
  }

  return colors;
}

/**
 * Returns colors for which no active variant with the given selectedSize exists.
 * These colors should be shown as disabled in the UI.
 */
export function getDisabledColors(variants: Variant[], selectedSize: string): string[] {
  const allColors = getAvailableColors(variants);
  const availableColors = new Set(
    variants
      .filter((v) => v.active && v.size === selectedSize)
      .map((v) => v.color)
  );

  return allColors.filter((color) => !availableColors.has(color));
}

/**
 * Returns sizes for which no active variant with the given selectedColor exists.
 * These sizes should be shown as disabled in the UI.
 */
export function getDisabledSizes(variants: Variant[], selectedColor: string): string[] {
  const allSizes = getAvailableSizes(variants);
  const availableSizes = new Set(
    variants
      .filter((v) => v.active && v.color === selectedColor)
      .map((v) => v.size)
  );

  return allSizes.filter((size) => !availableSizes.has(size));
}

/**
 * Returns the price of the active variant matching the given size and color,
 * or null if no such variant exists.
 */
export function getVariantPrice(variants: Variant[], size: string, color: string): number | null {
  const variant = variants.find((v) => v.active && v.size === size && v.color === color);
  return variant ? variant.price : null;
}

/**
 * Returns the default variant selection: size and color of the first active variant.
 * If no active variants exist, returns nulls.
 */
export function getDefaultVariant(variants: Variant[]): SelectedVariant {
  const firstActive = variants.find((v) => v.active);

  if (!firstActive) {
    return { size: null, color: null, variant: null };
  }

  return {
    size: firstActive.size,
    color: firstActive.color,
    variant: firstActive,
  };
}
