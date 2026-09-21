/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import { addDaysToBsDate, getBsDaysBetween, toLatinDigits } from '@nepali-date-picker/core';
import type { CalendarDate, NepaliEventInput, NepaliEventKind, NepaliLanguage } from './types.js';

/** The numeral script a language writes its digits in, as the engine names it. */
export function digitScript(language: NepaliLanguage): 'devanagari' | 'latin' {
  return language === 'ne' ? 'devanagari' : 'latin';
}

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

const EVENT_KINDS: readonly NepaliEventKind[] = [
  'governmentPublic',
  'religious',
  'regional',
  'observance',
];

/**
 * Parse the `weekly-off-days` attribute: a comma-separated list of weekday numbers, 1 for Sunday
 * through 7 for Saturday.
 *
 * JavaScript numbers Sunday `0` and this calendar numbers it `1`, so a `0` is dropped rather than
 * quietly closing Sunday. An unparseable value yields an empty week, which closes nothing.
 */
export function parseWeeklyOffDays(value: string | null | undefined): number[] {
  if (!value) return [];
  return Array.from(
    new Set(
      value
        .split(',')
        .map((part) => Number(part.trim()))
        .filter((day) => Number.isInteger(day) && day >= 1 && day <= 7),
    ),
  ).sort((a, b) => a - b);
}

/** Longest span one entry expands to, so a mistyped `days` cannot fill the grid for a year. */
const MAX_SPAN_DAYS = 366;

/**
 * Parse the `events` attribute or property: JSON text, or an array already.
 *
 * An entry without a usable `date` is dropped, and so is anything that is not an array at all, so a
 * malformed attribute leaves the calendar plain instead of throwing inside a render.
 *
 * An entry carrying `endDate` or `days` is expanded here into one entry per day of its span, which
 * is what lets everything downstream keep reading a day at a time.
 */
export function parseEvents(value: string | NepaliEventInput[] | null | undefined): NepaliEventInput[] {
  if (!value) return [];
  let raw: unknown = value;
  if (typeof value === 'string') {
    const text = value.trim();
    if (text === '') return [];
    try {
      raw = JSON.parse(text);
    } catch {
      return [];
    }
  }
  if (!Array.isArray(raw)) return [];
  return raw
    .filter((entry): entry is NepaliEventInput => {
      if (entry === null || typeof entry !== 'object') return false;
      const date = (entry as NepaliEventInput).date;
      return typeof date === 'string' && parseIso(date) !== null;
    })
    .flatMap(expandSpan);
}

/**
 * One entry per day of an entry's span, or the entry itself when it covers a single day.
 *
 * A span that runs off the end of the supported range stops there rather than throwing, so the days
 * the calendar can draw are still marked.
 */
function expandSpan(entry: NepaliEventInput): NepaliEventInput[] {
  const start = parseIso(entry.date);
  if (start === null) return [entry];
  const length = spanLength(entry, start);
  if (length <= 1) return [entry];

  const days: NepaliEventInput[] = [dayOfSpan(entry, start)];
  for (let offset = 1; offset < length; offset += 1) {
    try {
      const moved = addDaysToBsDate(start.year, start.month, start.dayOfMonth, offset);
      days.push(dayOfSpan(entry, moved));
    } catch {
      break;
    }
  }
  return days;
}

/** How many days an entry covers: what `endDate` spells, else `days`, else one. */
function spanLength(entry: NepaliEventInput, start: CalendarDate): number {
  const end = parseIso(entry.endDate);
  if (end !== null) {
    try {
      const between = getBsDaysBetween(
        start.year,
        start.month,
        start.dayOfMonth,
        end.year,
        end.month,
        end.dayOfMonth,
      );
      return between < 0 ? 1 : Math.min(between + 1, MAX_SPAN_DAYS);
    } catch {
      return 1;
    }
  }
  const days = entry.days;
  if (typeof days !== 'number' || !Number.isInteger(days) || days <= 1) return 1;
  return Math.min(days, MAX_SPAN_DAYS);
}

/** One day of a span: everything the entry says about itself, on the day given. */
function dayOfSpan(entry: NepaliEventInput, date: CalendarDate): NepaliEventInput {
  const day: NepaliEventInput = { date: toIso(date) };
  if (entry.name !== undefined) day.name = entry.name;
  if (entry.kind !== undefined) day.kind = entry.kind;
  if (entry.closesOffices !== undefined) day.closesOffices = entry.closesOffices;
  if (entry.color !== undefined) day.color = entry.color;
  if (entry.indicate !== undefined) day.indicate = entry.indicate;
  // The app's own handles travel with every day of the span, which is what lets a list gather them
  // back into one line and an app find the record behind a tapped day.
  if (entry.id !== undefined) day.id = entry.id;
  if (entry.payload !== undefined) day.payload = entry.payload;
  return day;
}

/** The kind an entry names, falling back to `observance` for anything unrecognized. */
export function eventKindOf(event: NepaliEventInput): NepaliEventKind {
  const kind = event.kind;
  return kind !== undefined && EVENT_KINDS.includes(kind) ? kind : 'observance';
}

/** Whether an entry shuts the institution: its own flag, else what its kind usually means. */
export function closesOffices(event: NepaliEventInput): boolean {
  if (typeof event.closesOffices === 'boolean') return event.closesOffices;
  return eventKindOf(event) !== 'observance';
}

/** How strongly a kind describes a day when several land on it, lowest first. */
export function kindPriority(kind: NepaliEventKind): number {
  return EVENT_KINDS.indexOf(kind);
}
