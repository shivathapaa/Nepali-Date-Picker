/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import { LitElement, css, html, nothing } from 'lit';
import type { PropertyValues, TemplateResult } from 'lit';
import { convertBsToAd } from '@nepali-date-picker/core';
import type {
  CalendarDate,
  CalendarSystem,
  NepaliDaySelectDetail,
  NepaliEventInput,
  NepaliEventSelectDetail,
  NepaliLanguage,
} from './types.js';
import { parseEvents, parseIso, parseWeeklyOffDays, toIso } from './utils.js';
import { CalendarController } from './internal/calendar-controller.js';
import { toCanonical } from './internal/calendar-model.js';
import { renderCalendar } from './internal/calendar-view.js';
import { calendarStyles, tokens } from './internal/styles.js';
import { collapseEventRows, eventKindOf, monthEventRows, weekdayLabel } from './internal/event-rows.js';

/**
 * A browsable Bikram Sambat (Nepali) month calendar rendered as native DOM, so it works in React,
 * Vue, Angular, Svelte, and plain HTML.
 *
 * Where `<nepali-date-picker>` asks for a date, this one is read: it shows both calendars' numbers
 * and the neighbouring months' days by default, marks the days an institution is closed for, and
 * can write the picked day and the month's events out under the grid.
 *
 * The grid is the picker's own, so a day looks the same on both. All calendar math comes from
 * `@nepali-date-picker/core`, the same engine the Kotlin, Android and Python builds use.
 *
 * @fires {CustomEvent<NepaliDaySelectDetail>} day-select - A day was clicked. Carries the day in
 *   both calendars, whether the institution is shut, and everything named on it.
 * @fires {CustomEvent<NepaliEventSelectDetail>} event-select - A line of the month's list was
 *   clicked. Carries the entry with its `id` and `payload` untouched, which is what an app reads
 *   its own record out of.
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
export class NepaliCalendar extends LitElement {
  static override properties = {
    value: { type: String },
    language: { type: String },
    calendarSystem: { type: String, attribute: 'calendar-system' },
    showCalendarToggle: { type: Boolean, attribute: 'show-calendar-toggle' },
    showAdjacentMonthDays: { type: Boolean, attribute: 'show-adjacent-month-days' },
    showSecondaryDate: { type: Boolean, attribute: 'show-secondary-date' },
    showDaySummary: { type: Boolean, attribute: 'show-day-summary' },
    showMonthEvents: { type: Boolean, attribute: 'show-month-events' },
    events: { type: String },
    weeklyOffDays: { type: String, attribute: 'weekly-off-days' },
  };

  /** The picked Bikram Sambat day as a `YYYY-MM-DD` string. Empty when nothing is picked. */
  declare value: string;
  /** Display language, `en` (English + Latin digits) or `ne` (Nepali + Devanagari digits). */
  declare language: NepaliLanguage;
  /**
   * Calendar the grid displays, `bs` (Bikram Sambat) or `ad` (Gregorian). Only the display changes:
   * `value` and the events stay Bikram Sambat, so switching keeps the same day picked.
   */
  declare calendarSystem: CalendarSystem;
  /** Show a `B.S.` / `A.D.` switch above the month header. */
  declare showCalendarToggle: boolean;
  /** Fill the grid's empty cells with the neighbouring months' days. On by default. */
  declare showAdjacentMonthDays: boolean;
  /** Pair every day with the same day in the other calendar. On by default, as a patro is read. */
  declare showSecondaryDate: boolean;
  /** Write the picked day out under the grid: whether the institution is shut, why, and what is on it. */
  declare showDaySummary: boolean;
  /** List the month's events under the grid, a span gathered into a single line. */
  declare showMonthEvents: boolean;
  /**
   * The days to mark, as JSON: `[{"date":"2083-06-03","name":"Constitution Day",
   * "kind":"governmentPublic","id":"constitution-day","payload":"{\\"imageUrl\\":\\"…\\"}"}]`.
   * An entry's `id` and `payload` come back with `day-select` and `event-select` untouched.
   * Malformed entries are ignored rather than thrown.
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
      .lists {
        border-top: 1px solid var(--ndp-border);
        padding: 8px 12px 12px;
        display: flex;
        flex-direction: column;
        gap: 8px;
      }
      .verdict {
        font-size: 0.85rem;
        font-weight: 600;
      }
      .verdict.closed {
        color: var(--_ndp-holiday-public);
      }
      .line {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 0.8rem;
        background: none;
        border: 0;
        padding: 4px 6px;
        margin: 0;
        border-radius: 6px;
        color: inherit;
        font-family: inherit;
        text-align: start;
        width: 100%;
      }
      button.line {
        cursor: pointer;
      }
      button.line:hover {
        background: var(--ndp-hover);
      }
      .line.picked {
        background: var(--ndp-hover);
      }
      .line-day {
        min-width: 2.5rem;
        font-weight: 600;
      }
      .line-weekday {
        color: var(--ndp-muted);
        font-size: 0.7rem;
      }
      .line-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        flex: none;
      }
      .empty {
        font-size: 0.8rem;
        color: var(--ndp-muted);
      }
    `,
  ];

  constructor() {
    super();
    this.value = '';
    this.language = 'en';
    this.calendarSystem = 'bs';
    this.showCalendarToggle = false;
    // A calendar is read rather than filled in, so it opens with everything a patro shows.
    this.showAdjacentMonthDays = true;
    this.showSecondaryDate = true;
    this.showDaySummary = false;
    this.showMonthEvents = false;
    this.events = '';
    this.weeklyOffDays = '';
    this.cal.onSelect = () => this.commit();
  }

  override willUpdate(changed: PropertyValues<this>): void {
    if (changed.has('events') || changed.has('weeklyOffDays')) {
      this.cal.setCalendarEvents(parseWeeklyOffDays(this.weeklyOffDays), parseEvents(this.events));
    }
    // Applied before the value so the grid lands on the picked day in the right calendar.
    if (changed.has('calendarSystem')) {
      this.cal.setSystem(this.calendarSystem === 'ad' ? 'ad' : 'bs');
    }
    if (changed.has('value')) {
      this.cal.setSelected(parseIso(this.value));
    }
  }

  private commit(): void {
    const picked = this.cal.selected;
    if (!picked) return;
    this.value = toIso(picked);
    const ad = convertBsToAd(picked.year, picked.month, picked.dayOfMonth);
    const adDate: CalendarDate = { year: ad.year, month: ad.month, dayOfMonth: ad.dayOfMonth };
    const onThatDay = this.cal.eventsOn(this.displayedDateOf(picked));
    this.dispatchEvent(
      new CustomEvent<NepaliDaySelectDetail>('day-select', {
        detail: {
          bs: picked,
          ad: adDate,
          bsIso: toIso(picked),
          adIso: toIso(adDate),
          isWeeklyOff: this.cal.isWeeklyOff(this.displayedDateOf(picked)),
          isNonWorking:
            this.cal.isWeeklyOff(this.displayedDateOf(picked)) ||
            onThatDay.some((event) => closesOn(event)),
          events: onThatDay,
        },
        bubbles: true,
        composed: true,
      }),
    );
  }

  /** A Bikram Sambat date as the displayed calendar writes it, which is what the controller takes. */
  private displayedDateOf(date: CalendarDate): CalendarDate {
    if (this.cal.system === 'bs') return date;
    return toCanonical('ad', date) === null ? date : (fromBs(date) ?? date);
  }

  private emitEvent(event: NepaliEventInput, firstBsIso: string, lastBsIso: string): void {
    this.dispatchEvent(
      new CustomEvent<NepaliEventSelectDetail>('event-select', {
        detail: { event, firstBsIso, lastBsIso },
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
          disabled: false,
          showFooter: false,
          showEnglish: false,
          showCalendarToggle: this.showCalendarToggle,
          showAdjacentDays: this.showAdjacentMonthDays,
          showSecondaryDate: this.showSecondaryDate,
        })}
        ${this.showDaySummary || this.showMonthEvents
          ? html`<div class="lists">
              ${this.showDaySummary ? this.renderDaySummary() : nothing}
              ${this.showMonthEvents ? this.renderMonthEvents() : nothing}
            </div>`
          : nothing}
      </div>
    `;
  }

  /** The picked day written out: the verdict first, then the weekly rule and what is named on it. */
  private renderDaySummary(): TemplateResult {
    const picked = this.cal.selected;
    if (!picked) {
      return html`<div class="empty">${this.language === 'ne' ? 'मिति चयन गर्नुहोस्' : 'Select a date'}</div>`;
    }
    const displayed = this.displayedDateOf(picked);
    const weeklyOff = this.cal.isWeeklyOff(displayed);
    const onThatDay = this.cal.eventsOn(displayed);
    const closed = weeklyOff || onThatDay.some((event) => closesOn(event));
    const ne = this.language === 'ne';
    return html`
      <div class="verdict ${closed ? 'closed' : ''}">
        ${closed ? (ne ? 'बन्द' : 'Closed') : ne ? 'कार्य दिन' : 'Working day'}
      </div>
      ${weeklyOff
        ? html`<div class="line">
            <span class="line-dot" style="background: var(--_ndp-holiday-public)"></span>
            <span>${ne ? 'साप्ताहिक बिदा' : 'Weekly day off'}</span>
          </div>`
        : nothing}
      ${onThatDay.map(
        (event) => html`<div class="line">
          <span class="line-dot" style="background: ${dotColor(event)}"></span>
          <span>${event.name ?? ''}</span>
        </div>`,
      )}
      ${!weeklyOff && onThatDay.length === 0
        ? html`<div class="empty">${ne ? 'यस दिन केही छैन' : 'Nothing on this day'}</div>`
        : nothing}
    `;
  }

  /** The month's events in day order, a span gathered into one clickable line. */
  private renderMonthEvents(): TemplateResult {
    const rows = collapseEventRows(monthEventRows(this.cal, this.cal.viewYear, this.cal.viewMonth));
    const ne = this.language === 'ne';
    if (rows.length === 0) {
      return html`<div class="empty">${ne ? 'यस महिना केही छैन' : 'Nothing this month'}</div>`;
    }
    const pickedIso = this.value;
    return html`${rows.map((row) => {
      const covers = pickedIso !== '' && pickedIso >= row.firstIso && pickedIso <= row.lastIso;
      const span = row.firstIso !== row.lastIso;
      return html`<button
        type="button"
        class="line ${covers ? 'picked' : ''}"
        @click=${() => this.emitEvent(row.event, row.firstIso, row.lastIso)}
      >
        <span class="line-day">
          ${row.firstDay}${span ? `-${row.lastDay}` : ''}
          <span class="line-weekday">${weekdayLabel(row.first, this.language)}</span>
        </span>
        <span class="line-dot" style="background: ${dotColor(row.event)}"></span>
        <span>${row.event.name ?? ''}</span>
      </button>`;
    })}`;
  }
}

/** Whether an entry shuts the institution, by its own flag or by what its kind usually means. */
function closesOn(event: NepaliEventInput): boolean {
  if (event.closesOffices !== undefined) return event.closesOffices;
  return eventKindOf(event) !== 'observance';
}

/** The colour a line's dot takes: the entry's own, or the slot its kind maps to. */
function dotColor(event: NepaliEventInput): string {
  if (event.color !== undefined && event.color !== '') return event.color;
  switch (eventKindOf(event)) {
    case 'governmentPublic':
      return 'var(--_ndp-holiday-public)';
    case 'religious':
      return 'var(--_ndp-holiday-religious)';
    case 'regional':
      return 'var(--_ndp-holiday-regional)';
    default:
      return 'var(--_ndp-holiday-observance)';
  }
}

/** A Bikram Sambat date as the Gregorian calendar writes it, or null when it has no equivalent. */
function fromBs(date: CalendarDate): CalendarDate | null {
  const ad = convertBsToAd(date.year, date.month, date.dayOfMonth);
  return { year: ad.year, month: ad.month, dayOfMonth: ad.dayOfMonth };
}

if (!customElements.get('nepali-calendar')) {
  customElements.define('nepali-calendar', NepaliCalendar);
}

declare global {
  interface HTMLElementTagNameMap {
    'nepali-calendar': NepaliCalendar;
  }
}
