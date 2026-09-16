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

/** Display language: `en` for English text and Latin digits, `ne` for Nepali text and Devanagari digits. */
export type NepaliLanguage = 'en' | 'ne';

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
 * The `change` event of the single-date elements. Named so that `addEventListener` callbacks can be
 * annotated without spelling out the `CustomEvent` wrapper.
 */
export type NepaliDatePickerChangeEvent = CustomEvent<NepaliDatePickerChangeDetail>;

/** The `change` event of the range elements. */
export type NepaliDateRangeChangeEvent = CustomEvent<NepaliDateRangeChangeDetail>;

/** The `invalid` event of the text field elements. */
export type NepaliDateFieldInvalidEvent = CustomEvent<NepaliDateFieldInvalidDetail>;
