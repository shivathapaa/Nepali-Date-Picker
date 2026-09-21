/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import { getWeekdayName } from '@nepali-date-picker/core';
import type { CalendarDate, NepaliEventInput, NepaliLanguage } from '../types.js';
import { eventKindOf, kindPriority, parseIso, toIso } from '../utils.js';
import type { CalendarController } from './calendar-controller.js';
import { weekdayOf } from './calendar-model.js';

export { eventKindOf } from '../utils.js';

/** One line of a month's list: an entry and the days of that month it runs over. */
export interface EventRow {
  event: NepaliEventInput;
  first: CalendarDate;
  firstIso: string;
  lastIso: string;
  firstDay: number;
  lastDay: number;
}

/**
 * Everything named in one Bikram Sambat month, in day order and, within a day, strongest kind
 * first. Takes the whole table the controller holds rather than the grid, so a Gregorian view still
 * lists the Bikram Sambat month the header names.
 */
export function monthEventRows(
  controller: CalendarController,
  viewYear: number,
  viewMonth: number,
): NepaliEventInput[] {
  const month = controller.system === 'bs' ? { year: viewYear, month: viewMonth } : canonicalMonthOf(controller);
  return controller
    .allEvents()
    .filter((event) => {
      const date = parseIso(event.date);
      return date !== null && date.year === month.year && date.month === month.month;
    })
    .sort((a, b) => {
      const left = parseIso(a.date);
      const right = parseIso(b.date);
      if (left === null || right === null) return 0;
      if (left.dayOfMonth !== right.dayOfMonth) return left.dayOfMonth - right.dayOfMonth;
      return kindPriority(eventKindOf(a)) - kindPriority(eventKindOf(b));
    });
}

/**
 * [events] as list rows, with the consecutive days of one entry gathered into a single row.
 *
 * Days are gathered when they share a non-empty `id` and follow one another, which is the shape a
 * span expands to. An entry without an id stays one row per day, since nothing says those days are
 * the same thing.
 */
export function collapseEventRows(events: NepaliEventInput[]): EventRow[] {
  const rows: EventRow[] = [];
  const openRowOf = new Map<string, number>();

  for (const event of events) {
    const date = parseIso(event.date);
    if (date === null) continue;
    const id = event.id;
    const openIndex = id === undefined || id === '' ? undefined : openRowOf.get(id);
    const open = openIndex === undefined ? undefined : rows[openIndex];
    if (open !== undefined && openIndex !== undefined && open.lastDay + 1 === date.dayOfMonth) {
      rows[openIndex] = { ...open, lastIso: toIso(date), lastDay: date.dayOfMonth };
      continue;
    }
    rows.push({
      event,
      first: date,
      firstIso: toIso(date),
      lastIso: toIso(date),
      firstDay: date.dayOfMonth,
      lastDay: date.dayOfMonth,
    });
    if (id !== undefined && id !== '') openRowOf.set(id, rows.length - 1);
  }
  return rows;
}

/** The short weekday name of a Bikram Sambat date, for the date column of a line. */
export function weekdayLabel(date: CalendarDate, language: NepaliLanguage): string {
  return getWeekdayName(weekdayOf(date), 'medium', language);
}

/** The Bikram Sambat month a Gregorian view sits on, which is the one its header names. */
function canonicalMonthOf(controller: CalendarController): { year: number; month: number } {
  const canonical = controller.canonicalOf({
    year: controller.viewYear,
    month: controller.viewMonth,
    dayOfMonth: 1,
  });
  if (canonical === null) return { year: controller.viewYear, month: controller.viewMonth };
  return { year: canonical.year, month: canonical.month };
}
