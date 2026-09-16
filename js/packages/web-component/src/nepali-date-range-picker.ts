/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import { LitElement, css, html } from 'lit';
import type { PropertyValues, TemplateResult } from 'lit';
import { convertBsToAd } from '@nepali-date-picker/core';
import type { CalendarDate, NepaliDateRangeChangeDetail, NepaliLanguage } from './types.js';
import { parseIso, toIso } from './utils.js';
import { CalendarController } from './internal/calendar-controller.js';
import { renderCalendar } from './internal/calendar-view.js';
import { calendarStyles, tokens } from './internal/styles.js';

/**
 * A Bikram Sambat date range picker. The first click sets the start, the second sets the end (clicks
 * earlier than the start swap the two); a third click starts a new range.
 *
 * @fires {CustomEvent<NepaliDateRangeChangeDetail>} change - Dispatched on each pick.
 *
 * @cssprop [--ndp-font] - Font family for the whole element.
 * @cssprop [--ndp-bg] - Calendar surface background.
 * @cssprop [--ndp-text] - Primary text color.
 * @cssprop [--ndp-muted] - Secondary text, weekday headers and out-of-month days.
 * @cssprop [--ndp-accent] - Background of the two range endpoints.
 * @cssprop [--ndp-on-accent] - Text color on top of the accent.
 * @cssprop [--ndp-in-range] - Wash behind the days between the endpoints.
 * @cssprop [--ndp-hover] - Hover wash over a selectable day.
 * @cssprop [--ndp-today-ring] - Ring color marking today.
 * @cssprop [--ndp-border] - Divider and outline color.
 * @cssprop [--ndp-radius] - Corner radius of the surface and day cells.
 */
export class NepaliDateRangePicker extends LitElement {
  static override properties = {
    start: { type: String },
    end: { type: String },
    language: { type: String },
    min: { type: String },
    max: { type: String },
    disabled: { type: Boolean, reflect: true },
    showEnglish: { type: Boolean, attribute: 'show-english' },
  };

  /** Selected start date as a `YYYY-MM-DD` Bikram Sambat string. */
  declare start: string;
  /** Selected end date as a `YYYY-MM-DD` Bikram Sambat string. */
  declare end: string;
  declare language: NepaliLanguage;
  declare min: string;
  declare max: string;
  declare disabled: boolean;
  declare showEnglish: boolean;

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
    this.start = '';
    this.end = '';
    this.language = 'en';
    this.min = '';
    this.max = '';
    this.disabled = false;
    this.showEnglish = false;
    this.cal.onSelect = () => this.commit();
  }

  override willUpdate(changed: PropertyValues<this>): void {
    if (changed.has('min') || changed.has('max')) {
      this.cal.configure({ mode: 'range', min: parseIso(this.min), max: parseIso(this.max) });
    }
    if (changed.has('start') || changed.has('end')) {
      this.cal.setRange(parseIso(this.start), parseIso(this.end));
    }
  }

  private commit(): void {
    if (this.disabled) return;
    const start = this.cal.rangeStart;
    const end = this.cal.rangeEnd;
    this.start = start ? toIso(start) : '';
    this.end = end ? toIso(end) : '';
    this.dispatchEvent(
      new CustomEvent<NepaliDateRangeChangeDetail>('change', {
        detail: {
          start,
          end,
          startBsIso: start ? toIso(start) : null,
          endBsIso: end ? toIso(end) : null,
          startAdIso: start ? toIso(adOf(start)) : null,
          endAdIso: end ? toIso(adOf(end)) : null,
        },
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
        })}
      </div>
    `;
  }
}

function adOf(date: CalendarDate): CalendarDate {
  const ad = convertBsToAd(date.year, date.month, date.dayOfMonth);
  return { year: ad.year, month: ad.month, dayOfMonth: ad.dayOfMonth };
}

if (!customElements.get('nepali-date-range-picker')) {
  customElements.define('nepali-date-range-picker', NepaliDateRangePicker);
}

declare global {
  interface HTMLElementTagNameMap {
    'nepali-date-range-picker': NepaliDateRangePicker;
  }
}
