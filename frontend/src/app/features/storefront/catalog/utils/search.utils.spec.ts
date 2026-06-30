// Feature: storefront-catalog, Property 13: Search minimum length gate
import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import { shouldTriggerSearch } from './search.utils';
import { MIN_SEARCH_LENGTH } from '../models';

/**
 * Validates: Requirements 9.2
 *
 * Property 13: Search minimum length gate
 * For any input string, the search function SHALL trigger an API call
 * if and only if the trimmed input length is >= 3 characters.
 */

describe('shouldTriggerSearch - Property 13: Search minimum length gate', () => {
  it('should return true iff trimmed input length >= MIN_SEARCH_LENGTH', () => {
    fc.assert(
      fc.property(fc.string(), (input) => {
        const result = shouldTriggerSearch(input);
        const trimmedLength = input.trim().length;

        if (trimmedLength >= MIN_SEARCH_LENGTH) {
          expect(result).toBe(true);
        } else {
          expect(result).toBe(false);
        }
      }),
      { numRuns: 100 }
    );
  });

  it('should not trigger for strings whose trimmed length < 3', () => {
    const shortAfterTrimArb = fc
      .string({ minLength: 0, maxLength: 20 })
      .filter((s: string) => s.trim().length < MIN_SEARCH_LENGTH);

    fc.assert(
      fc.property(shortAfterTrimArb, (input) => {
        expect(shouldTriggerSearch(input)).toBe(false);
      }),
      { numRuns: 100 }
    );
  });

  it('should trigger for strings whose trimmed length >= 3', () => {
    const longAfterTrimArb = fc
      .string({ minLength: MIN_SEARCH_LENGTH, maxLength: 100 })
      .filter((s: string) => s.trim().length >= MIN_SEARCH_LENGTH);

    fc.assert(
      fc.property(longAfterTrimArb, (input) => {
        expect(shouldTriggerSearch(input)).toBe(true);
      }),
      { numRuns: 100 }
    );
  });
});
