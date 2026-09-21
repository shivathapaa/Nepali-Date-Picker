/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import {
  compareBsDates,
  convertAdToBs,
  convertBsToAd,
  getAdCalendarsInBsMonth,
  getAdMonth,
  getAdMonthName,
  getAdYearRangeForBsYears,
  getBsCalendarsInAdMonth,
  getBsMonth,
  getBsMonthName,
  getBsYearRange,
  getTotalDaysInAdMonth,
  getTotalDaysInBsMonth,
  isAdDateConvertible,
  localizeDigits,
} from '@nepali-date-picker/core';
import type { CalendarDate, CalendarSystem, NepaliLanguage } from '../types.js';
import { digitScript } from '../utils.js';

/** Inclusive supported Bikram Sambat year range, resolved once from the engine. */
export const YEAR_RANGE = getBsYearRange();

/** The Gregorian years covering the same span of real days as [YEAR_RANGE]. */
export const AD_YEAR_RANGE = getAdYearRangeForBsYears(YEAR_RANGE.first, YEAR_RANGE.last);

/** Years a grid in [system] can page over. */
export function yearRangeOf(system: CalendarSystem): { first: number; last: number } {
  return system === 'ad' ? AD_YEAR_RANGE : YEAR_RANGE;
}

/** Weekday (1 = Sunday .. 7 = Saturday) of a date, without a full calendar lookup. */
export function weekdayOf(date: CalendarDate, system: CalendarSystem = 'bs'): number {
  const info = system === 'ad' ? getAdMonth(date.year, date.month) : getBsMonth(date.year, date.month);
  return ((info.firstDayOfMonth - 1 + date.dayOfMonth - 1) % 7) + 1;
}

/** Number of empty leading cells and the day numbers for a month grid in [system]. */
export function monthGrid(system: CalendarSystem, year: number, month: number): { leading: number; days: number[] } {
  const info = system === 'ad' ? getAdMonth(year, month) : getBsMonth(year, month);
  const days: number[] = [];
  for (let d = 1; d <= info.totalDaysInMonth; d += 1) days.push(d);
  return { leading: info.daysFromStartOfWeekToFirstOfMonth, days };
}

/**
 * The Bikram Sambat date behind every day of a month in [system], indexed by day number minus one.
 *
 * A Gregorian month is converted in a single pass rather than one call per cell, and days before
 * the conversion anchor (AD 1913-04-13) come back as `null`.
 */
export function canonicalDatesInMonth(system: CalendarSystem, year: number, month: number): (CalendarDate | null)[] {
  if (system === 'bs') {
    const { days } = monthGrid('bs', year, month);
    return days.map((dayOfMonth) => ({ year, month, dayOfMonth }));
  }
  return getBsCalendarsInAdMonth(year, month).map((date) =>
    date ? { year: date.year, month: date.month, dayOfMonth: date.dayOfMonth } : null,
  );
}

/**
 * Every day of a month in [system] as its counterpart in the *other* calendar, indexed by day
 * number minus one.
 *
 * What a dual-date grid draws as the small number in each cell. Like [canonicalDatesInMonth] the
 * whole month is converted in a single pass, and days with no counterpart, only possible before the
 * conversion anchor (AD 1913-04-13), come back as `null`.
 */
export function secondaryDatesInMonth(
  system: CalendarSystem,
  year: number,
  month: number,
): (CalendarDate | null)[] {
  // A Gregorian month's counterpart is its Bikram Sambat equivalent, which is also its canonical
  // date, so that pass is already written.
  if (system === 'ad') return canonicalDatesInMonth('ad', year, month);
  return getAdCalendarsInBsMonth(year, month).map((date) => ({
    year: date.year,
    month: date.month,
    dayOfMonth: date.dayOfMonth,
  }));
}

/**
 * The months of the *other* calendar that a month in [system] straddles, with their year. Reads
 * "Sep/Oct 2026" under a Bikram Sambat month and "Bhadra/Asoj 2083" under a Gregorian one.
 *
 * Takes the [secondary] dates of the month, as returned by [secondaryDatesInMonth]. Gives a single
 * month name when the straddled months coincide, and `null` when the month has no counterpart at
 * all, which only happens at the very start of the conversion table.
 */
export function secondaryMonthLabel(
  system: CalendarSystem,
  secondary: (CalendarDate | null)[],
  language: NepaliLanguage,
): string | null {
  const present = secondary.filter((date): date is CalendarDate => date !== null);
  if (present.length === 0) return null;
  const first = present[0];
  const last = present[present.length - 1];

  const nameOf = (month: number): string =>
    system === 'ad'
      ? getBsMonthName(month, 'short', language)
      : getAdMonthName(month, 'short', language);
  // The year of the month the grid ends in, so a straddling month reads as the one it runs into.
  const year = localizeDigits(String(last.year), digitScript(language));

  return first.month === last.month
    ? `${nameOf(first.month)} ${year}`
    : `${nameOf(first.month)}/${nameOf(last.month)} ${year}`;
}

/** A Bikram Sambat date rewritten in [system], or `null` when it has no equivalent there. */
export function fromCanonical(system: CalendarSystem, date: CalendarDate): CalendarDate | null {
  if (system === 'bs') return date;
  try {
    const ad = convertBsToAd(date.year, date.month, date.dayOfMonth);
    return { year: ad.year, month: ad.month, dayOfMonth: ad.dayOfMonth };
  } catch {
    return null;
  }
}

/**
 * A date read in [system] as its Bikram Sambat equivalent, or `null` when it has none.
 *
 * The mirror of [fromCanonical]. Every element stores and reports Bikram Sambat, so anything typed
 * or picked in another calendar passes through here first.
 */
export function toCanonical(system: CalendarSystem, date: CalendarDate): CalendarDate | null {
  if (system === 'bs') return date;
  if (!isAdDateConvertible(date.year, date.month, date.dayOfMonth)) return null;
  const bs = convertAdToBs(date.year, date.month, date.dayOfMonth);
  return { year: bs.year, month: bs.month, dayOfMonth: bs.dayOfMonth };
}

/** Clamp a day number to a month's length in [system]. */
export function clampDay(system: CalendarSystem, year: number, month: number, day: number): number {
  const total = system === 'ad' ? getTotalDaysInAdMonth(year, month) : getTotalDaysInBsMonth(year, month);
  return Math.min(day, total);
}

/** Chronological comparison of two Bikram Sambat dates: negative, zero, or positive. */
export function compare(a: CalendarDate, b: CalendarDate): number {
  return compareBsDates(a.year, a.month, a.dayOfMonth, b.year, b.month, b.dayOfMonth);
}

/** Whether a Bikram Sambat date is within the supported range and any `min` / `max` bounds. */
export function isSelectable(date: CalendarDate, min: CalendarDate | null, max: CalendarDate | null): boolean {
  if (date.year < YEAR_RANGE.first || date.year > YEAR_RANGE.last) return false;
  if (min && compare(date, min) < 0) return false;
  if (max && compare(date, max) > 0) return false;
  return true;
}

/** Whether a date falls strictly inside a `[start, end]` selection (for range shading). */
export function withinRange(date: CalendarDate, start: CalendarDate | null, end: CalendarDate | null): boolean {
  if (!start || !end) return false;
  return compare(date, start) >= 0 && compare(date, end) <= 0;
}

/** Step a `year`/`month` by whole months in [system], clamped to its year range. `null` if out of range. */
export function stepMonth(
  system: CalendarSystem,
  year: number,
  month: number,
  delta: number,
): { year: number; month: number } | null {
  const range = yearRangeOf(system);
  let y = year;
  let m = month + delta;
  while (m > 12) {
    m -= 12;
    y += 1;
  }
  while (m < 1) {
    m += 12;
    y -= 1;
  }
  if (y < range.first || y > range.last) return null;
  return { year: y, month: m };
}
