/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import { LitElement, css, html } from 'lit';
import type { PropertyValues, TemplateResult } from 'lit';
import { getBsMonthName, getTodayBs, getTotalDaysInBsMonth, localizeDigits } from '@nepali-date-picker/core';
import type { CalendarDate, NepaliDatePickerChangeDetail, NepaliLanguage } from './types.js';
import { parseIso, toIso } from './utils.js';
import { buildChangeDetail } from './nepali-date-picker.js';
import { YEAR_RANGE } from './internal/calendar-model.js';
import { tokens } from './internal/styles.js';

type WheelPart = 'year' | 'month' | 'day';

interface WheelOption {
  value: number;
  label: string;
}

/**
 * A wheel (spinner) Bikram Sambat date picker: three scroll-snapping columns for year, month, and
 * day. The day column adjusts to the selected month's length. Selection is driven by clicking an item
 * or the Up / Down arrows, so it is accessible and works without pointer scrolling.
 *
 * @fires {CustomEvent<NepaliDatePickerChangeDetail>} change - Dispatched whenever the selected date
 *   changes.
 *
 * @cssprop [--ndp-font] - Font family for the whole element.
 * @cssprop [--ndp-bg] - Wheel background.
 * @cssprop [--ndp-text] - Color of the centered, selected value.
 * @cssprop [--ndp-muted] - Color of the values above and below the selection.
 * @cssprop [--ndp-accent] - Selection band and focus outline color.
 * @cssprop [--ndp-hover] - Hover wash over a value.
 * @cssprop [--ndp-radius] - Corner radius of the wheel and its selection band.
 */
export class NepaliWheelDatePicker extends LitElement {
  static override properties = {
    value: { type: String },
    language: { type: String },
    disabled: { type: Boolean, reflect: true },
    _year: { state: true },
    _month: { state: true },
    _day: { state: true },
  };

  declare value: string;
  declare language: NepaliLanguage;
  declare disabled: boolean;
  declare private _year: number;
  declare private _month: number;
  declare private _day: number;

  static override styles = [
    tokens,
    css`
      :host {
        display: inline-block;
        background: var(--ndp-bg);
        border-radius: var(--ndp-radius);
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.06);
        padding: 8px;
      }
      :host([disabled]) {
        opacity: 0.55;
        pointer-events: none;
      }
      .wheels {
        display: flex;
        gap: 8px;
        position: relative;
      }
      .wheels::before {
        content: '';
        position: absolute;
        left: 0;
        right: 0;
        top: 50%;
        transform: translateY(-50%);
        height: 40px;
        background: var(--ndp-hover);
        border-radius: 8px;
        pointer-events: none;
      }
      .column {
        height: 200px;
        overflow-y: auto;
        scroll-snap-type: y mandatory;
        scrollbar-width: none;
        text-align: center;
        outline: none;
        flex: 1;
        min-width: 4.5rem;
      }
      .column::-webkit-scrollbar {
        display: none;
      }
      .column:focus-visible {
        box-shadow: inset 0 0 0 2px var(--ndp-accent);
        border-radius: 8px;
      }
      .pad {
        height: 80px;
      }
      .option {
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        scroll-snap-align: center;
        font: inherit;
        color: var(--ndp-muted);
        background: transparent;
        border: 0;
        width: 100%;
        cursor: pointer;
      }
      .option[aria-selected='true'] {
        color: var(--ndp-text);
        font-weight: 700;
      }
    `,
  ];

  constructor() {
    super();
    this.value = '';
    this.language = 'en';
    this.disabled = false;
    const today = getTodayBs();
    this._year = today.year;
    this._month = today.month;
    this._day = today.dayOfMonth;
  }

  override connectedCallback(): void {
    super.connectedCallback();
    const parsed = parseIso(this.value);
    if (parsed) {
      this._year = parsed.year;
      this._month = parsed.month;
      this._day = parsed.dayOfMonth;
    }
  }

  override willUpdate(changed: PropertyValues<this>): void {
    if (changed.has('value')) {
      const parsed = parseIso(this.value);
      if (parsed) {
        this._year = parsed.year;
        this._month = parsed.month;
        this._day = Math.min(parsed.dayOfMonth, getTotalDaysInBsMonth(parsed.year, parsed.month));
      }
    }
  }

  private select(part: WheelPart, value: number): void {
    if (part === 'year') this._year = value;
    else if (part === 'month') this._month = value;
    else this._day = value;
    const maxDay = getTotalDaysInBsMonth(this._year, this._month);
    if (this._day > maxDay) this._day = maxDay;
    const date: CalendarDate = { year: this._year, month: this._month, dayOfMonth: this._day };
    this.value = toIso(date);
    this.dispatchEvent(
      new CustomEvent<NepaliDatePickerChangeDetail>('change', {
        detail: buildChangeDetail(date, this.language),
        bubbles: true,
        composed: true,
      }),
    );
  }

  private onColumnKeydown(event: KeyboardEvent, part: WheelPart, options: WheelOption[], current: number): void {
    const index = options.findIndex((o) => o.value === current);
    if (event.key === 'ArrowDown' && index < options.length - 1) {
      this.select(part, options[index + 1]!.value);
      event.preventDefault();
    } else if (event.key === 'ArrowUp' && index > 0) {
      this.select(part, options[index - 1]!.value);
      event.preventDefault();
    }
  }

  override updated(): void {
    for (const selected of this.renderRoot.querySelectorAll<HTMLElement>('.option[aria-selected="true"]')) {
      if (typeof selected.scrollIntoView === 'function') selected.scrollIntoView({ block: 'center' });
    }
  }

  private renderColumn(part: WheelPart, options: WheelOption[], current: number, ariaLabel: string): TemplateResult {
    return html`
      <div
        class="column"
        role="listbox"
        tabindex="0"
        aria-label=${ariaLabel}
        @keydown=${(e: KeyboardEvent) => this.onColumnKeydown(e, part, options, current)}
      >
        <div class="pad"></div>
        ${options.map(
          (o) => html`
            <button
              class="option"
              role="option"
              aria-selected=${o.value === current ? 'true' : 'false'}
              @click=${() => this.select(part, o.value)}
            >
              ${o.label}
            </button>
          `,
        )}
        <div class="pad"></div>
      </div>
    `;
  }

  override render(): TemplateResult {
    const script = this.language === 'ne' ? 'devanagari' : 'latin';
    const years: WheelOption[] = [];
    for (let y = YEAR_RANGE.first; y <= YEAR_RANGE.last; y += 1) years.push({ value: y, label: localizeDigits(String(y), script) });
    const months: WheelOption[] = [];
    for (let m = 1; m <= 12; m += 1) months.push({ value: m, label: getBsMonthName(m, 'full', this.language) });
    const days: WheelOption[] = [];
    for (let d = 1; d <= getTotalDaysInBsMonth(this._year, this._month); d += 1) days.push({ value: d, label: localizeDigits(String(d), script) });

    return html`
      <div class="wheels">
        ${this.renderColumn('year', years, this._year, this.language === 'ne' ? 'वर्ष' : 'Year')}
        ${this.renderColumn('month', months, this._month, this.language === 'ne' ? 'महिना' : 'Month')}
        ${this.renderColumn('day', days, this._day, this.language === 'ne' ? 'दिन' : 'Day')}
      </div>
    `;
  }
}

if (!customElements.get('nepali-wheel-date-picker')) {
  customElements.define('nepali-wheel-date-picker', NepaliWheelDatePicker);
}

declare global {
  interface HTMLElementTagNameMap {
    'nepali-wheel-date-picker': NepaliWheelDatePicker;
  }
}
