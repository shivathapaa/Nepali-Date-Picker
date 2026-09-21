/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import { LitElement, css, html } from 'lit';
import type { PropertyValues, TemplateResult } from 'lit';
import { convertBsToAd, formatBsDate } from '@nepali-date-picker/core';
import type { CalendarDate, CalendarSystem, NepaliDatePickerChangeDetail, NepaliLanguage } from './types.js';
import { parseEvents, parseIso, parseWeeklyOffDays, toIso } from './utils.js';
import { CalendarController } from './internal/calendar-controller.js';
import { weekdayOf } from './internal/calendar-model.js';
import { renderCalendar } from './internal/calendar-view.js';
import { calendarStyles, tokens } from './internal/styles.js';

/**
 * An inline Bikram Sambat (Nepali) calendar date picker rendered as native DOM, so it works in
 * React, Vue, Angular, Svelte, and plain HTML.
 *
 * All calendar math comes from `@nepali-date-picker/core`, the same engine used by the Kotlin,
 * Android, and Python builds, so dates always agree across platforms.
 *
 * @fires {CustomEvent<NepaliDatePickerChangeDetail>} change - Dispatched when the user selects a
 *   date. Bubbles and crosses the shadow boundary.
 *
 * @cssprop [--ndp-font] - Font family for the whole element.
 * @cssprop [--ndp-bg] - Calendar surface background.
 * @cssprop [--ndp-text] - Primary text color.
 * @cssprop [--ndp-muted] - Secondary text, weekday headers and out-of-month days.
 * @cssprop [--ndp-accent] - Selection background and focus color.
 * @cssprop [--ndp-on-accent] - Text color on top of the accent.
 * @cssprop [--ndp-hover] - Hover wash over a selectable day.
 * @cssprop [--ndp-today-ring] - Ring color marking today.
 * @cssprop [--ndp-border] - Divider and outline color.
 * @cssprop [--ndp-radius] - Corner radius of the surface and day cells.
 */
export class NepaliDatePicker extends LitElement {
  static override properties = {
    value: { type: String },
    language: { type: String },
    min: { type: String },
    max: { type: String },
    disabled: { type: Boolean, reflect: true },
    showEnglish: { type: Boolean, attribute: 'show-english' },
    calendarSystem: { type: String, attribute: 'calendar-system' },
    showCalendarToggle: { type: Boolean, attribute: 'show-calendar-toggle' },
    showAdjacentMonthDays: { type: Boolean, attribute: 'show-adjacent-month-days' },
    showSecondaryDate: { type: Boolean, attribute: 'show-secondary-date' },
    events: { type: String },
    weeklyOffDays: { type: String, attribute: 'weekly-off-days' },
  };

  /** Selected Bikram Sambat date as a `YYYY-MM-DD` string. Empty when nothing is selected. */
  declare value: string;
  /** Display language, `en` (English + Latin digits) or `ne` (Nepali + Devanagari digits). */
  declare language: NepaliLanguage;
  /** Earliest selectable Bikram Sambat date as `YYYY-MM-DD`, or empty for no lower bound. */
  declare min: string;
  /** Latest selectable Bikram Sambat date as `YYYY-MM-DD`, or empty for no upper bound. */
  declare max: string;
  /** When set, the picker is read-only and dims itself. */
  declare disabled: boolean;
  /** Show the Gregorian equivalent of the selected date under the calendar. */
  declare showEnglish: boolean;
  /**
   * Calendar the grid displays, `bs` (Bikram Sambat) or `ad` (Gregorian). Only the display changes:
   * `value` and the `change` event stay Bikram Sambat, so switching keeps the same day selected.
   */
  declare calendarSystem: CalendarSystem;
  /** Show a `B.S.` / `A.D.` switch above the month header. */
  declare showCalendarToggle: boolean;
  /**
   * Fill the grid's empty cells with the neighbouring months' days, drawn faded. Clicking one picks
   * that day and moves the grid to its month.
   */
  declare showAdjacentMonthDays: boolean;
  /**
   * Pair every day with the same day in the other calendar, drawn small in the corner of the cell,
   * and name that calendar's months under the month header. The Bikram Sambat grid then reads
   * "2" over "18", the way `NepaliDatePickerWithEnglishDate` does in Compose.
   */
  declare showSecondaryDate: boolean;
  /**
   * The days to mark, as JSON: `[{"date":"2083-06-03","name":"Constitution Day",
   * "kind":"governmentPublic"}]`. A holiday colours its day; add `"indicate": true` for an event
   * that should also draw a dot. Something that runs longer than a day takes `"endDate"` or
   * `"days"` and marks every day of the span. Malformed entries are ignored rather than thrown.
   */
  declare events: string;
  /**
   * The weekdays the institution never opens, as a comma-separated list. Sunday is 1 and Saturday
   * is 7, so Nepal's office week is `7` and a school closed Saturday and Sunday is `7,1`.
   */
  declare weeklyOffDays: string;

  private readonly cal = new CalendarController(this);

  static override styles = [
    tokens,
    calendarStyles,
    css`
      :host {
        display: inline-block;
        background: var(--ndp-bg);
        border-radius: var(--ndp-radius);
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.06);
      }
      :host([disabled]) {
        opacity: 0.55;
        pointer-events: none;
      }
    `,
  ];

  constructor() {
    super();
    this.value = '';
    this.language = 'en';
    this.min = '';
    this.max = '';
    this.disabled = false;
    this.showEnglish = false;
    this.calendarSystem = 'bs';
    this.showCalendarToggle = false;
    this.showAdjacentMonthDays = false;
    this.showSecondaryDate = false;
    this.events = '';
    this.weeklyOffDays = '';
    this.cal.onSelect = () => this.commit();
  }

  override willUpdate(changed: PropertyValues<this>): void {
    if (changed.has('events') || changed.has('weeklyOffDays')) {
      this.cal.setCalendarEvents(parseWeeklyOffDays(this.weeklyOffDays), parseEvents(this.events));
    }
    if (changed.has('min') || changed.has('max')) {
      this.cal.configure({ mode: 'single', min: parseIso(this.min), max: parseIso(this.max) });
    }
    // Applied before the value so the grid lands on the selection in the right calendar.
    if (changed.has('calendarSystem')) {
      this.cal.setSystem(this.calendarSystem === 'ad' ? 'ad' : 'bs');
    }
    if (changed.has('value')) {
      this.cal.setSelected(parseIso(this.value));
    }
  }

  private commit(): void {
    if (this.disabled || !this.cal.selected) return;
    const date = this.cal.selected;
    this.value = toIso(date);
    this.dispatchEvent(
      new CustomEvent<NepaliDatePickerChangeDetail>('change', {
        detail: buildChangeDetail(date, this.language),
        bubbles: true,
        composed: true,
      }),
    );
  }

  override render(): TemplateResult {
    return html`
      <div class="surface">
        ${renderCalendar(this.cal, {
          language: this.language,
          disabled: this.disabled,
          showFooter: true,
          showEnglish: this.showEnglish,
          showCalendarToggle: this.showCalendarToggle,
          showAdjacentDays: this.showAdjacentMonthDays,
          showSecondaryDate: this.showSecondaryDate,
        })}
      </div>
    `;
  }
}

/** Build the public `change` payload for a selected Bikram Sambat date. */
export function buildChangeDetail(date: CalendarDate, language: NepaliLanguage): NepaliDatePickerChangeDetail {
  const ad = convertBsToAd(date.year, date.month, date.dayOfMonth);
  const adDate: CalendarDate = { year: ad.year, month: ad.month, dayOfMonth: ad.dayOfMonth };
  return {
    bs: date,
    ad: adDate,
    bsIso: toIso(date),
    adIso: toIso(adDate),
    formatted: formatBsDate(date.year, date.month, date.dayOfMonth, weekdayOf(date), language, 'full', 'full', 'full', null),
  };
}

if (!customElements.get('nepali-date-picker')) {
  customElements.define('nepali-date-picker', NepaliDatePicker);
}

declare global {
  interface HTMLElementTagNameMap {
    'nepali-date-picker': NepaliDatePicker;
  }
}
