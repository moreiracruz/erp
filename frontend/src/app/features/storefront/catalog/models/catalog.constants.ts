/** Number of products loaded per page/batch. */
export const PAGE_SIZE = 20;

/** Distance (px) from the bottom of the grid to trigger next page load. */
export const SCROLL_THRESHOLD_PX = 200;

/** Duration (ms) for which product list responses are cached. */
export const CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes

/** Debounce time (ms) before triggering a search API call. */
export const SEARCH_DEBOUNCE_MS = 300;

/** Minimum trimmed input length required to trigger a search. */
export const MIN_SEARCH_LENGTH = 3;
