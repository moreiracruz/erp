import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';

describe('fast-check smoke test', () => {
  it('should run a trivial property test', () => {
    fc.assert(
      fc.property(fc.integer(), (n) => {
        expect(n + 0).toBe(n);
      }),
      { numRuns: 100 }
    );
  });
});
