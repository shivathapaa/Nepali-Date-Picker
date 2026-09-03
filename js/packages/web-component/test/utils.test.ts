import { describe, it, expect } from 'vitest';
import { parseIso, toIso, sameDate } from '../src/utils.js';

describe('utils', () => {
  it('parses dash and slash formats', () => {
    expect(parseIso('2081-05-24')).toEqual({ year: 2081, month: 5, dayOfMonth: 24 });
    expect(parseIso('2081/5/4')).toEqual({ year: 2081, month: 5, dayOfMonth: 4 });
  });

  it('folds Devanagari digits before parsing', () => {
    expect(parseIso('२०८१-०५-२४')).toEqual({ year: 2081, month: 5, dayOfMonth: 24 });
  });

  it('rejects empty and malformed input', () => {
    expect(parseIso('')).toBeNull();
    expect(parseIso(null)).toBeNull();
    expect(parseIso('not-a-date')).toBeNull();
    expect(parseIso('2081-13-01')).toBeNull();
    expect(parseIso('2081-05-40')).toBeNull();
  });

  it('formats zero-padded ISO', () => {
    expect(toIso({ year: 2081, month: 5, dayOfMonth: 4 })).toBe('2081-05-04');
    expect(toIso({ year: 2081, month: 12, dayOfMonth: 30 })).toBe('2081-12-30');
  });

  it('compares dates structurally, tolerating null', () => {
    const a = { year: 2081, month: 5, dayOfMonth: 24 };
    expect(sameDate(a, { ...a })).toBe(true);
    expect(sameDate(a, { ...a, dayOfMonth: 25 })).toBe(false);
    expect(sameDate(null, null)).toBe(true);
    expect(sameDate(a, null)).toBe(false);
  });
});
