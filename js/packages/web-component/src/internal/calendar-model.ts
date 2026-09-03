/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import { compareBsDates, getBsMonth, getBsYearRange, getTotalDaysInBsMonth } from '@nepali-date-picker/core';
import type { CalendarDate } from '../types.js';

/** Inclusive supported Bikram Sambat year range, resolved once from the engine. */
export const YEAR_RANGE = getBsYearRange();

/** Weekday (1 = Sunday .. 7 = Saturday) of a Bikram Sambat date, without a full calendar lookup. */
export function weekdayOf(date: CalendarDate): number {
  const info = getBsMonth(date.year, date.month);
  return ((info.firstDayOfMonth - 1 + date.dayOfMonth - 1) % 7) + 1;
}

/** Number of empty leading cells and the day numbers for a month grid. */
export function monthGrid(year: number, month: number): { leading: number; days: number[] } {
  const info = getBsMonth(year, month);
  const days: number[] = [];
  for (let d = 1; d <= info.totalDaysInMonth; d += 1) days.push(d);
  return { leading: info.daysFromStartOfWeekToFirstOfMonth, days };
}

/** Clamp a day number to a month's length. */
export function clampDay(year: number, month: number, day: number): number {
  return Math.min(day, getTotalDaysInBsMonth(year, month));
}

/** Chronological comparison: negative if `a` is earlier, `0` if equal, positive if later. */
export function compare(a: CalendarDate, b: CalendarDate): number {
  return compareBsDates(a.year, a.month, a.dayOfMonth, b.year, b.month, b.dayOfMonth);
}

/** Whether a date is within the supported range and any `min` / `max` bounds. */
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

/** Step a `year`/`month` by whole months, clamped to the supported year range. `null` if out of range. */
export function stepMonth(year: number, month: number, delta: number): { year: number; month: number } | null {
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
  if (y < YEAR_RANGE.first || y > YEAR_RANGE.last) return null;
  return { year: y, month: m };
}
