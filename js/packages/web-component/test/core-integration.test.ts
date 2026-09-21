import { describe, it, expect } from 'vitest';
import {
  convertAdToBs,
  convertBsToAd,
  createCalendarPolicy,
  createDetailedEvent,
  createEvent,
  formatBsDateText,
  getBsYearRange,
  getTotalDaysInBsMonth,
  localizeDigits,
  parseBsDateText,
} from '@nepali-date-picker/core';
import { parseIso, toIso } from '../src/utils.js';

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

  it('answers what a day is under a policy', () => {
    const policy = createCalendarPolicy(
      [7],
      [createEvent(2082, 6, 3, 'Constitution Day', 'governmentPublic')],
    );

    const status = policy.statusOf(2082, 6, 3);
    expect(status.isNonWorking).toBe(true);
    expect(status.primaryKind).toBe('governmentPublic');
    expect(Array.from(status.names)).toEqual(['Constitution Day']);
    expect(policy.eventsIn(2082, 6)).toHaveLength(1);
  });

  it('keeps an event that does not close the day out of the arithmetic', () => {
    const policy = createCalendarPolicy(
      [],
      [createDetailedEvent(2082, 6, 3, 'Programme', 'religious', false, 'evt-1', '{"a":1}')],
    );

    expect(policy.isNonWorkingDay(2082, 6, 3)).toBe(false);
    expect(policy.workingDaysBetween(2082, 6, 1, 2082, 6, 8)).toBe(7);

    const [event] = Array.from(policy.eventsOn(2082, 6, 3));
    expect(event.id).toBe('evt-1');
    expect(event.payload).toBe('{"a":1}');
  });

  it('walks working days around a weekly rule', () => {
    const school = createCalendarPolicy([7, 1], []);
    expect(school.workingDaysBetween(2082, 1, 1, 2082, 1, 15)).toBe(10);
    expect(school.monthStatus(2082, 1)).toHaveLength(getTotalDaysInBsMonth(2082, 1));
  });
});

// The component carries its own dependency-free YYYY-MM-DD helpers rather than routing attribute
// values through the engine. These hold that shortcut to the engine's definition, which is in turn
// the shape the Kotlin `SimpleDateSerializer` reads and writes.
describe('YYYY-MM-DD wire format parity', () => {
  const dates = [
    { year: 1970, month: 1, dayOfMonth: 1 },
    { year: 2081, month: 12, dayOfMonth: 30 },
    { year: 2082, month: 2, dayOfMonth: 14 },
    { year: 2082, month: 6, dayOfMonth: 3 },
    { year: 2100, month: 12, dayOfMonth: 31 },
  ];

  it('formats the same string the engine does', () => {
    for (const date of dates) {
      expect(toIso(date)).toBe(
        formatBsDateText(date.year, date.month, date.dayOfMonth, 'yyyy-mm-dd', 'latin'),
      );
    }
  });

  it('parses what the engine parses', () => {
    for (const date of dates) {
      const text = toIso(date);
      const fromEngine = parseBsDateText(text, 'yyyy-mm-dd');
      expect(fromEngine).not.toBeNull();
      expect(parseIso(text)).toEqual({
        year: fromEngine!.year,
        month: fromEngine!.month,
        dayOfMonth: fromEngine!.dayOfMonth,
      });
    }
  });

  it('rejects the same out-of-range months and days', () => {
    for (const text of ['2082-13-01', '2082-00-01', '2082-02-33', '2082-02-00']) {
      expect(parseIso(text)).toBeNull();
      expect(parseBsDateText(text, 'yyyy-mm-dd')).toBeNull();
    }
  });

  it('accepts unpadded and slash-delimited input the engine turns down', () => {
    // Attribute values are typed by hand, so the component's parser is the more forgiving of the
    // two. Everything the engine accepts it accepts; the extra latitude never reaches the wire.
    for (const text of ['2082-2-14', '2082/02/14']) {
      expect(parseIso(text)).toEqual({ year: 2082, month: 2, dayOfMonth: 14 });
      expect(parseBsDateText(text, 'yyyy-mm-dd')).toBeNull();
    }
  });

  it('folds Devanagari digits the same way', () => {
    const fromEngine = parseBsDateText('२०८२-०२-१४', 'yyyy-mm-dd');
    expect(parseIso('२०८२-०२-१४')).toEqual({ year: 2082, month: 2, dayOfMonth: 14 });
    expect([fromEngine!.year, fromEngine!.month, fromEngine!.dayOfMonth]).toEqual([2082, 2, 14]);
  });
});
