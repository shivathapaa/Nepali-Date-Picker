/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import { toLatinDigits } from '@nepali-date-picker/core';
import type { CalendarDate } from './types.js';

/** Zero-pad a positive integer to at least two digits. */
function pad2(value: number): string {
  return value < 10 ? `0${value}` : String(value);
}

/** Format a date as a zero-padded `YYYY-MM-DD` string (Latin digits). */
export function toIso(date: CalendarDate): string {
  return `${date.year}-${pad2(date.month)}-${pad2(date.dayOfMonth)}`;
}

/**
 * Parse a `YYYY-MM-DD` (or `YYYY/MM/DD`) date string into a [CalendarDate].
 *
 * Devanagari digits are accepted and folded to Latin first. Returns `null` for empty or malformed
 * input; range validity against the calendar is the caller's responsibility.
 */
export function parseIso(value: string | null | undefined): CalendarDate | null {
  if (!value) return null;
  const normalized = toLatinDigits(value.trim());
  const match = /^(\d{1,4})[-/](\d{1,2})[-/](\d{1,2})$/.exec(normalized);
  if (!match) return null;
  const year = Number(match[1]);
  const month = Number(match[2]);
  const dayOfMonth = Number(match[3]);
  if (month < 1 || month > 12 || dayOfMonth < 1 || dayOfMonth > 32) return null;
  return { year, month, dayOfMonth };
}

/** Structural equality for two dates, tolerating `null`. */
export function sameDate(a: CalendarDate | null, b: CalendarDate | null): boolean {
  if (a === null || b === null) return a === b;
  return a.year === b.year && a.month === b.month && a.dayOfMonth === b.dayOfMonth;
}
