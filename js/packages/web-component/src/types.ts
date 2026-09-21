/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

/** A calendar date as year / month / day, with 1-based month and day. */
export interface CalendarDate {
  year: number;
  month: number;
  dayOfMonth: number;
}

/**
 * What kind of thing an event is, which is what the calendar colours it by.
 *
 * `governmentPublic` and `religious` and `regional` close the day by default; `observance` does
 * not. An entry's own `closesOffices` has the final say either way.
 */
export type NepaliEventKind = 'governmentPublic' | 'religious' | 'regional' | 'observance';

/**
 * One thing on one Bikram Sambat day: a public holiday, a festival, a programme, a meeting.
 *
 * Something that runs longer than a day is still one entry: give it `endDate` or `days` and the
 * calendar marks every day of the span the same way.
 *
 * @property date `YYYY-MM-DD` in Bikram Sambat. The first day when the entry is a span.
 * @property name What to announce for the day; several names on one day are joined.
 * @property kind Defaults to `observance`, the kind that claims the least.
 * @property closesOffices Whether the institution is shut for this. Defaults to what `kind` means.
 * @property color A CSS colour for this entry's dot, overriding the kind's own.
 * @property indicate Whether the day draws a dot for this. Off by default, so a holiday colours the
 *   day without spending one of the three dot slots an app's own events need.
 * @property endDate Last day of the span, `YYYY-MM-DD` in Bikram Sambat and included in it. Wins
 *   over `days`. An end before `date` marks the one day, since refusing to draw would hide the
 *   entry altogether.
 * @property days How many days the span covers, counting the first. Ignored when `endDate` is set.
 *   A span is capped at 366 days, so a mistyped value cannot fill the grid for a year.
 * @property id An identifier your app correlates back to its own record, carried through untouched
 *   and handed back with `day-select` and `event-select`. Days of one span share it, which is what
 *   makes the span a single line of a month's list.
 * @property payload Anything else you want back with the entry, as an opaque string: JSON, a URL,
 *   an identifier list. Nothing here parses it. A calendar showing pictures keeps its image URLs in
 *   here and fetches them itself when an entry is tapped.
 */
export interface NepaliEventInput {
  date: string;
  name?: string;
  kind?: NepaliEventKind;
  closesOffices?: boolean;
  color?: string;
  indicate?: boolean;
  endDate?: string;
  days?: number;
  id?: string;
  payload?: string;
}

/** Display language: `en` for English text and Latin digits, `ne` for Nepali text and Devanagari digits. */
export type NepaliLanguage = 'en' | 'ne';

/**
 * Which calendar a grid displays: `bs` for Bikram Sambat, `ad` for Gregorian.
 *
 * Only the display changes. A selected date is always reported in Bikram Sambat, so switching keeps
 * the same day selected.
 */
export type CalendarSystem = 'bs' | 'ad';

/**
 * Payload of the `change` event dispatched when the user picks a date.
 *
 * @property bs The selected date in the Bikram Sambat calendar.
 * @property ad The same instant in the Gregorian calendar.
 * @property bsIso The Bikram Sambat date as a zero-padded `YYYY-MM-DD` string.
 * @property adIso The Gregorian date as a zero-padded `YYYY-MM-DD` string.
 * @property formatted A human-readable Bikram Sambat date in the picker's current language.
 */
export interface NepaliDatePickerChangeDetail {
  bs: CalendarDate;
  ad: CalendarDate;
  bsIso: string;
  adIso: string;
  formatted: string;
}

/**
 * Payload of the `change` event dispatched by range elements. Fields are `null` until that end of the
 * range has been chosen (picking a start clears any previous end).
 *
 * @property start The selected start date in the Bikram Sambat calendar.
 * @property end The selected end date in the Bikram Sambat calendar.
 * @property startBsIso / endBsIso The dates as zero-padded `YYYY-MM-DD` Bikram Sambat strings.
 * @property startAdIso / endAdIso The Gregorian equivalents as `YYYY-MM-DD` strings.
 */
export interface NepaliDateRangeChangeDetail {
  start: CalendarDate | null;
  end: CalendarDate | null;
  startBsIso: string | null;
  endBsIso: string | null;
  startAdIso: string | null;
  endAdIso: string | null;
}

/**
 * Payload of the `invalid` event dispatched by the text field elements when what the user typed
 * cannot be accepted.
 *
 * @property message The rejection reason, already localized to the element's current language.
 */
export interface NepaliDateFieldInvalidDetail {
  message: string;
}

/**
 * Payload of the `day-select` event dispatched by `<nepali-calendar>` when a day is clicked.
 *
 * @property bs The day in the Bikram Sambat calendar.
 * @property ad The same day in the Gregorian calendar.
 * @property bsIso / adIso The two dates as zero-padded `YYYY-MM-DD` strings.
 * @property isWeeklyOff Whether the week closes the day.
 * @property isNonWorking Whether the institution is shut, for either reason.
 * @property events Everything named on the day, strongest kind first, each with the `id` and
 *   `payload` it was given.
 */
export interface NepaliDaySelectDetail {
  bs: CalendarDate;
  ad: CalendarDate;
  bsIso: string;
  adIso: string;
  isWeeklyOff: boolean;
  isNonWorking: boolean;
  events: NepaliEventInput[];
}

/**
 * Payload of the `event-select` event dispatched by `<nepali-calendar>` when a line of the month's
 * list is clicked.
 *
 * @property event The entry behind the line, `id` and `payload` untouched. For a span it is the
 *   first day of the run in the displayed month.
 * @property firstBsIso / lastBsIso The days the line covers, in the displayed month.
 */
export interface NepaliEventSelectDetail {
  event: NepaliEventInput;
  firstBsIso: string;
  lastBsIso: string;
}

/** The `day-select` event of `<nepali-calendar>`. */
export type NepaliDaySelectEvent = CustomEvent<NepaliDaySelectDetail>;

/** The `event-select` event of `<nepali-calendar>`. */
export type NepaliEventSelectEvent = CustomEvent<NepaliEventSelectDetail>;

/**
 * The `change` event of the single-date elements. Named so that `addEventListener` callbacks can be
 * annotated without spelling out the `CustomEvent` wrapper.
 */
export type NepaliDatePickerChangeEvent = CustomEvent<NepaliDatePickerChangeDetail>;

/** The `change` event of the range elements. */
export type NepaliDateRangeChangeEvent = CustomEvent<NepaliDateRangeChangeDetail>;

/** The `invalid` event of the text field elements. */
export type NepaliDateFieldInvalidEvent = CustomEvent<NepaliDateFieldInvalidDetail>;
