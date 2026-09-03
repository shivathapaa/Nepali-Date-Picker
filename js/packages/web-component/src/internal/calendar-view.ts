/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import { html, nothing } from 'lit';
import type { TemplateResult } from 'lit';
import { classMap } from 'lit/directives/class-map.js';
import { convertBsToAd, getBsMonthName, getTodayBs, getWeekdayName, localizeDigits } from '@nepali-date-picker/core';
import type { CalendarDate, NepaliLanguage } from '../types.js';
import { sameDate, toIso } from '../utils.js';
import { monthGrid, YEAR_RANGE } from './calendar-model.js';
import type { CalendarController } from './calendar-controller.js';

const WEEKDAYS = [1, 2, 3, 4, 5, 6, 7];

/** Display-only options for the shared month calendar renderer. */
export interface CalendarViewOptions {
  language: NepaliLanguage;
  disabled: boolean;
  /** Show the Today shortcut and (when enabled) the English-date line. */
  showFooter?: boolean;
  /** Show the Gregorian equivalent of the current selection under the grid. */
  showEnglish?: boolean;
}

function digitScript(language: NepaliLanguage): 'devanagari' | 'latin' {
  return language === 'ne' ? 'devanagari' : 'latin';
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

/** Render the shared month calendar (header, weekday row, day grid, optional footer). */
export function renderCalendar(c: CalendarController, opts: CalendarViewOptions): TemplateResult {
  const { language, disabled } = opts;
  const script = digitScript(language);
  const loc = (value: number): string => localizeDigits(String(value), script);
  const monthName = getBsMonthName(c.viewMonth, 'full', language);
  const today = getTodayBs();

  const { leading, days } = monthGrid(c.viewYear, c.viewMonth);
  const cells: TemplateResult[] = [];
  for (let i = 0; i < leading; i += 1) cells.push(html`<span class="day blank" aria-hidden="true"></span>`);
  for (const day of days) {
    const date: CalendarDate = { year: c.viewYear, month: c.viewMonth, dayOfMonth: day };
    const isToday = today.year === date.year && today.month === date.month && today.dayOfMonth === date.dayOfMonth;
    const isSelected = sameDate(c.selected, date);
    const isStart = sameDate(c.rangeStart, date);
    const isEnd = sameDate(c.rangeEnd, date);
    const inRange = c.inRange(date);
    const selectable = c.isSelectable(date);
    const weekdayName = getWeekdayName(c.weekdayOf(date), 'full', language);
    const ariaLabel = `${weekdayName}, ${monthName} ${date.dayOfMonth}, ${date.year}`;
    cells.push(html`
      <button
        class=${classMap({
          day: true,
          today: isToday,
          selected: isSelected,
          'range-start': isStart,
          'range-end': isEnd,
          'in-range': inRange,
        })}
        role="gridcell"
        aria-label=${ariaLabel}
        aria-selected=${isSelected || isStart || isEnd ? 'true' : 'false'}
        aria-disabled=${selectable ? 'false' : 'true'}
        aria-current=${isToday ? 'date' : nothing}
        tabindex=${sameDate(c.focus, date) ? 0 : -1}
        @click=${() => c.pick(date)}
      >
        ${loc(day)}
      </button>
    `);
  }
  while (cells.length % 7 !== 0) cells.push(html`<span class="day blank" aria-hidden="true"></span>`);

  const rows: TemplateResult[] = [];
  for (let i = 0; i < cells.length; i += 7) rows.push(html`<div role="row">${cells.slice(i, i + 7)}</div>`);

  const years: number[] = [];
  for (let y = YEAR_RANGE.first; y <= YEAR_RANGE.last; y += 1) years.push(y);

  const atMin = c.viewYear <= YEAR_RANGE.first && c.viewMonth <= 1;
  const atMax = c.viewYear >= YEAR_RANGE.last && c.viewMonth >= 12;
  const english = opts.showEnglish ? englishLine(c, language) : null;

  return html`
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
