import { describe, it, expect } from 'vitest';
import {
  convertAdToBs,
  convertBsToAd,
  getBsYearRange,
  getTotalDaysInBsMonth,
  localizeDigits,
} from '@nepali-date-picker/core';

// Guards the boundary between the web component and the compiled Kotlin engine: if the npm package
// is mis-wired or the export shape drifts, these fail before any DOM test runs.
describe('@nepali-date-picker/core integration', () => {
  it('round-trips a known BS/AD pair', () => {
    const ad = convertBsToAd(2081, 5, 24);
    expect([ad.year, ad.month, ad.dayOfMonth]).toEqual([2024, 9, 9]);
    const bs = convertAdToBs(2024, 9, 9);
    expect([bs.year, bs.month, bs.dayOfMonth]).toEqual([2081, 5, 24]);
  });

  it('exposes the supported year range', () => {
    const range = getBsYearRange();
    expect([range.first, range.last]).toEqual([1970, 2100]);
  });

  it('reports month lengths and localizes digits', () => {
    expect(getTotalDaysInBsMonth(2081, 1)).toBe(31);
    expect(localizeDigits('2081', 'devanagari')).toBe('२०८१');
  });
});
