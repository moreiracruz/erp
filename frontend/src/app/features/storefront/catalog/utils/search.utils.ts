import { MIN_SEARCH_LENGTH } from '../models';

/**
 * Determines whether a search input meets the minimum length requirement
 * to trigger an API search call.
 */
export function shouldTriggerSearch(input: string): boolean {
  return input.trim().length >= MIN_SEARCH_LENGTH;
}
