/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import { html, nothing } from 'lit';
import type { TemplateResult } from 'lit';
import { classMap } from 'lit/directives/class-map.js';
import {
  convertBsToAd,
  getAdMonthName,
  getBsMonthName,
  getWeekdayName,
  localizeDigits,
} from '@nepali-date-picker/core';
import type { CalendarDate, CalendarSystem, NepaliLanguage } from '../types.js';
import { digitScript, toIso } from '../utils.js';
import {
  monthGrid,
  secondaryDatesInMonth,
  secondaryMonthLabel,
  stepMonth,
  yearRangeOf,
} from './calendar-model.js';
import type { CalendarController } from './calendar-controller.js';
import { closesOffices, eventKindOf } from '../utils.js';

const WEEKDAYS = [1, 2, 3, 4, 5, 6, 7];

/** Display-only options for the shared month calendar renderer. */
export interface CalendarViewOptions {
  language: NepaliLanguage;
  disabled: boolean;
  /** Show the Today shortcut and (when enabled) the English-date line. */
  showFooter?: boolean;
  /** Show the Gregorian equivalent of the current selection under the grid. */
  showEnglish?: boolean;
  /** Show the `B.S.` / `A.D.` switch above the month header. */
  showCalendarToggle?: boolean;
  /**
   * Fill the grid's empty cells with the neighbouring months' days, drawn faded. Clicking one picks
   * that day and moves the grid to its month.
   */
  showAdjacentDays?: boolean;
  /**
   * Pair every day with the same day in the other calendar, drawn small in the corner of the cell.
   * The month header then also names the other calendar's months this one straddles.
   */
  showSecondaryDate?: boolean;
}

const TOGGLE_LABELS: Record<CalendarSystem, Record<NepaliLanguage, string>> = {
  bs: { en: 'B.S.', ne: 'बि.सं.' },
  ad: { en: 'A.D.', ne: 'ई.सं.' },
};

/**
 * Appended to a day cell borrowed from a neighbouring month, after the date itself. The date already
 * names its own month, so this only has to say that choosing the cell moves the grid there, which
 * the fading says to a sighted user and nothing says to a screen reader.
 */
const ADJACENT_DAY_DESCRIPTIONS: Record<NepaliLanguage, string> = {
  en: 'shows another month',
  ne: 'अर्को महिना देखाउँछ',
};

const TOGGLE_DESCRIPTIONS: Record<CalendarSystem, Record<NepaliLanguage, string>> = {
  bs: { en: 'Show the Bikram Sambat calendar', ne: 'बिक्रम सम्बत् क्यालेन्डर हेर्नुहोस्' },
  ad: { en: 'Show the Gregorian calendar', ne: 'ईस्वी सम्बत् क्यालेन्डर हेर्नुहोस्' },
};

function monthNameOf(system: CalendarSystem, month: number, language: NepaliLanguage): string {
  return system === 'ad' ? getAdMonthName(month, 'full', language) : getBsMonthName(month, 'full', language);
}

function englishLine(c: CalendarController, language: NepaliLanguage): string | null {
  const label = language === 'ne' ? 'ईस्वी' : 'AD';
  if (c.selected) return `${label}: ${toIso(adOf(c.selected))}`;
  if (c.rangeStart) {
    const start = toIso(adOf(c.rangeStart));
    const end = c.rangeEnd ? ` – ${toIso(adOf(c.rangeEnd))}` : '';
    return `${label}: ${start}${end}`;
  }
  return null;
}

function adOf(date: CalendarDate): CalendarDate {
  const ad = convertBsToAd(date.year, date.month, date.dayOfMonth);
  return { year: ad.year, month: ad.month, dayOfMonth: ad.dayOfMonth };
}

function renderCalendarToggle(c: CalendarController, language: NepaliLanguage, disabled: boolean): TemplateResult {
  const segment = (system: CalendarSystem): TemplateResult => html`
    <button
      class=${classMap({ segment: true, active: c.system === system })}
      type="button"
      role="radio"
      aria-checked=${c.system === system ? 'true' : 'false'}
      aria-label=${TOGGLE_DESCRIPTIONS[system][language]}
      ?disabled=${disabled}
      @click=${() => c.setSystem(system)}
    >
      ${TOGGLE_LABELS[system][language]}
    </button>
  `;
  return html`
    <div class="calendar-toggle" role="radiogroup" aria-label=${language === 'ne' ? 'पात्रो' : 'Calendar'}>
      ${segment('bs')}${segment('ad')}
    </div>
  `;
}

/** Render the shared month calendar (header, weekday row, day grid, optional footer). */
export function renderCalendar(c: CalendarController, opts: CalendarViewOptions): TemplateResult {
  const { language, disabled } = opts;
  const script = digitScript(language);
  const loc = (value: number): string => localizeDigits(String(value), script);
  const monthName = monthNameOf(c.system, c.viewMonth, language);
  const yearRange = yearRangeOf(c.system);

  const { leading, days } = monthGrid(c.system, c.viewYear, c.viewMonth);

  // The calendar that is not on screen, which is what the small number in each cell and the second
  // line under the month name are written in.
  const otherSystem: CalendarSystem = c.system === 'ad' ? 'bs' : 'ad';
  const paired = opts.showSecondaryDate === true;
  // The second line earns its space whenever a second calendar is in play, either as the small
  // number in each cell or as the thing the switch switches to.
  const showSecondaryLabel = paired || opts.showCalendarToggle === true;
  const secondaryDays = showSecondaryLabel
    ? secondaryDatesInMonth(c.system, c.viewYear, c.viewMonth)
    : null;

  const dayCell = (
    date: CalendarDate,
    adjacent: boolean,
    pairs: (CalendarDate | null)[] | null,
  ): TemplateResult => {
    const secondary = pairs?.[date.dayOfMonth - 1] ?? null;
    const isToday = c.isToday(date);
    const isSelected = c.isSelected(date);
    const isStart = c.isRangeStart(date);
    const isEnd = c.isRangeEnd(date);
    const inRange = c.inRange(date);
    const selectable = c.isSelectable(date);
    // One colour per day, and dots only for the events that asked for one: a weekly off day repeats
    // fifty-two times a year, so spending a dot on it would say nothing the colour has not.
    const onThisDay = c.eventsOn(date);
    const weeklyOff = c.isWeeklyOff(date);
    const closing = onThisDay.find((event) => closesOffices(event));
    const markKind = closing ? eventKindOf(closing) : onThisDay[0] && eventKindOf(onThisDay[0]);
    const weeklyRuleLeads = weeklyOff && closing === undefined;
    const markClass = weeklyRuleLeads ? 'weekly-off' : markKind ? `kind-${markKind}` : '';
    const dots = onThisDay.filter((event) => event.indicate === true).slice(0, paired ? 2 : 3);
    const markedNames = onThisDay
      .map((event) => event.name)
      .filter((name): name is string => typeof name === 'string' && name !== '');
    const weekdayName = getWeekdayName(c.weekdayOf(date), 'full', language);
    const cellMonthName = monthNameOf(c.system, date.month, language);
    const adjacentHint = adjacent ? `, ${ADJACENT_DAY_DESCRIPTIONS[language]}` : '';
    // A paired cell draws two numbers, and only one of them would otherwise be spoken.
    const secondaryPart = secondary
      ? `, ${monthNameOf(otherSystem, secondary.month, language)} ${secondary.dayOfMonth}, ${secondary.year}`
      : '';
    const markedPart = markedNames.length > 0 ? `, ${markedNames.join(', ')}` : '';
    const ariaLabel =
      `${weekdayName}, ${cellMonthName} ${date.dayOfMonth}, ${date.year}` +
      `${secondaryPart}${adjacentHint}${markedPart}`;
    return html`
      <button
        class=${classMap({
          day: true,
          dual: paired,
          adjacent,
          today: isToday,
          selected: isSelected,
          'range-start': isStart,
          'range-end': isEnd,
          'in-range': inRange,
          marked: markClass !== '',
          [markClass]: markClass !== '',
        })}
        role="gridcell"
        aria-label=${ariaLabel}
        aria-selected=${isSelected || isStart || isEnd ? 'true' : 'false'}
        aria-disabled=${selectable ? 'false' : 'true'}
        aria-current=${isToday ? 'date' : nothing}
        tabindex=${!adjacent && c.isFocused(date) ? 0 : -1}
        @click=${() => {
          c.pick(date);
          // A day the picker will not select does not move the view either, so an unreachable cell
          // stays inert. Moving after picking keeps the focus the pick just set.
          if (adjacent && selectable) c.setView(date.year, date.month, true);
        }}
      >
        ${secondary
          ? html`
              <span class="primary">${loc(date.dayOfMonth)}</span>
              <small class="secondary">${loc(secondary.dayOfMonth)}</small>
            `
          : loc(date.dayOfMonth)}
        ${dots.length > 0
          ? html`<span class="event-dots" aria-hidden="true"
              >${dots.map(
                (event) =>
                  html`<span
                    class="event-dot"
                    style=${event.color ? `background:${event.color}` : nothing}
                    data-kind=${eventKindOf(event)}
                  ></span>`,
              )}</span
            >`
          : nothing}
      </button>
    `;
  };

  // An empty cell takes the paired cell's metrics too, so the columns stay even either way.
  const blank = (): TemplateResult =>
    html`<span class=${classMap({ day: true, blank: true, dual: paired })} aria-hidden="true"></span>`;

  // The neighbouring months, resolved once. `null` at the edges of the year range, where there is
  // nothing to borrow from and the cells stay blank.
  const previous = opts.showAdjacentDays ? stepMonth(c.system, c.viewYear, c.viewMonth, -1) : null;
  const next = opts.showAdjacentDays ? stepMonth(c.system, c.viewYear, c.viewMonth, 1) : null;

  // A borrowed day belongs to its own month, so it pairs with that month's counterparts.
  const pairsOf = (month: { year: number; month: number } | null): (CalendarDate | null)[] | null =>
    paired && month ? secondaryDatesInMonth(c.system, month.year, month.month) : null;
  const ownPairs = paired ? secondaryDays : null;

  const cells: TemplateResult[] = [];
  if (previous && leading > 0) {
    const tail = monthGrid(c.system, previous.year, previous.month).days.slice(-leading);
    const previousPairs = pairsOf(previous);
    for (const day of tail) cells.push(dayCell({ ...previous, dayOfMonth: day }, true, previousPairs));
  } else {
    for (let i = 0; i < leading; i += 1) cells.push(blank());
  }
  for (const day of days) {
    cells.push(dayCell({ year: c.viewYear, month: c.viewMonth, dayOfMonth: day }, false, ownPairs));
  }
  // Pad to the end of the last row holding a day of this month, and no further.
  const nextPairs = pairsOf(next);
  while (cells.length % 7 !== 0) {
    cells.push(
      next
        ? dayCell({ ...next, dayOfMonth: cells.length - leading - days.length + 1 }, true, nextPairs)
        : blank(),
    );
  }

  const rows: TemplateResult[] = [];
  for (let i = 0; i < cells.length; i += 7) rows.push(html`<div role="row">${cells.slice(i, i + 7)}</div>`);

  const years: number[] = [];
  for (let y = yearRange.first; y <= yearRange.last; y += 1) years.push(y);

  const atMin = c.viewYear <= yearRange.first && c.viewMonth <= 1;
  const atMax = c.viewYear >= yearRange.last && c.viewMonth >= 12;
  const english = opts.showEnglish ? englishLine(c, language) : null;
  const secondaryLabel = secondaryDays ? secondaryMonthLabel(c.system, secondaryDays, language) : null;

  return html`
    ${opts.showCalendarToggle ? renderCalendarToggle(c, language, disabled) : nothing}
    <div class="header">
      <button
        class="nav"
        aria-label=${language === 'ne' ? 'अघिल्लो महिना' : 'Previous month'}
        ?disabled=${atMin || disabled}
        @click=${() => c.changeMonth(-1)}
      >
        ‹
      </button>
      <span class="label" aria-live="polite">
        <span>${monthName}</span>
        <select
          aria-label=${language === 'ne' ? 'वर्ष' : 'Year'}
          ?disabled=${disabled}
          @change=${(e: Event) => c.setView(Number((e.target as HTMLSelectElement).value), c.viewMonth, true)}
        >
          ${years.map((y) => html`<option value=${y} ?selected=${y === c.viewYear}>${loc(y)}</option>`)}
        </select>
        ${secondaryLabel ? html`<small class="secondary-month">${secondaryLabel}</small>` : nothing}
      </span>
      <button
        class="nav"
        aria-label=${language === 'ne' ? 'अर्को महिना' : 'Next month'}
        ?disabled=${atMax || disabled}
        @click=${() => c.changeMonth(1)}
      >
        ›
      </button>
    </div>
    <div role="grid" aria-label=${`${monthName} ${loc(c.viewYear)}`} @keydown=${(e: KeyboardEvent) => !disabled && c.onKeydown(e)}>
      <div role="row">
        ${WEEKDAYS.map(
          (w) => html`<span class="weekday" role="columnheader" aria-label=${getWeekdayName(w, 'full', language)}>${getWeekdayName(w, 'short', language)}</span>`,
        )}
      </div>
      ${rows}
    </div>
    ${opts.showFooter
      ? html`
          <div class="footer">
            <span class="english">${english ?? ''}</span>
            <button class="link" ?disabled=${disabled} @click=${() => c.goToToday()}>
              ${language === 'ne' ? 'आज' : 'Today'}
            </button>
          </div>
        `
      : nothing}
  `;
}
