/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import { LitElement, css, html, nothing } from 'lit';
import type { PropertyValues, TemplateResult } from 'lit';
import {
  getTotalDaysInAdMonth,
  getTotalDaysInBsMonth,
  localizeDigits,
  toLatinDigits,
} from '@nepali-date-picker/core';
import type {
  CalendarDate,
  CalendarSystem,
  NepaliDateFieldInvalidDetail,
  NepaliDatePickerChangeDetail,
  NepaliLanguage,
} from './types.js';
import { parseIso, toIso } from './utils.js';
import { buildChangeDetail } from './nepali-date-picker.js';
import { fromCanonical, isSelectable, toCanonical, yearRangeOf } from './internal/calendar-model.js';
import { tokens } from './internal/styles.js';

interface Validation {
  date: CalendarDate | null;
  error: string;
}

/**
 * A text field for typing a Bikram Sambat date (`YYYY/MM/DD`) with inline validation and localized
 * error messages. Devanagari digits are accepted. No calendar UI, so it is ideal inside forms.
 *
 * @fires {CustomEvent<NepaliDatePickerChangeDetail>} change - Dispatched when the typed date becomes
 *   valid.
 * @fires {CustomEvent<NepaliDateFieldInvalidDetail>} invalid - Dispatched when the typed date is
 *   rejected. React does not deliver this event to an `onInvalid` prop, so use `addEventListener`.
 *
 * @cssprop [--ndp-font] - Font family for the label, input and message.
 * @cssprop [--ndp-bg] - Input background.
 * @cssprop [--ndp-text] - Typed text color.
 * @cssprop [--ndp-muted] - Label color.
 * @cssprop [--ndp-accent] - Focus outline color.
 * @cssprop [--ndp-border] - Input border color.
 * @cssprop [--ndp-error] - Border and message color when the typed date is rejected.
 */
export class NepaliDateField extends LitElement {
  static override properties = {
    value: { type: String },
    language: { type: String },
    min: { type: String },
    max: { type: String },
    disabled: { type: Boolean, reflect: true },
    label: { type: String },
    calendarSystem: { type: String, attribute: 'calendar-system' },
    _error: { state: true },
  };

  declare value: string;
  declare language: NepaliLanguage;
  declare min: string;
  declare max: string;
  declare disabled: boolean;
  declare label: string;
  /**
   * Calendar the user types in, `bs` (Bikram Sambat) or `ad` (Gregorian). `value` and the
   * `change` event stay Bikram Sambat either way.
   */
  declare calendarSystem: CalendarSystem;
  declare private _error: string;

  static override styles = [
    tokens,
    css`
      :host {
        display: inline-block;
      }
      :host([disabled]) {
        opacity: 0.55;
        pointer-events: none;
      }
      .field {
        display: inline-flex;
        flex-direction: column;
        gap: 4px;
      }
      label {
        font-size: 0.78rem;
        color: var(--_ndp-muted);
      }
      input {
        font: inherit;
        color: var(--_ndp-text);
        background: var(--_ndp-bg);
        border: 1px solid var(--_ndp-border);
        border-radius: 8px;
        padding: 8px 10px;
        min-width: 9.5rem;
      }
      input:focus-visible {
        outline: 2px solid var(--_ndp-accent);
        border-color: var(--_ndp-accent);
      }
      :host(.invalid) input,
      .invalid input {
        border-color: var(--_ndp-error);
      }
      .error {
        color: var(--_ndp-error);
        font-size: 0.75rem;
        min-height: 1em;
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
    this.label = '';
    this.calendarSystem = 'bs';
    this._error = '';
  }

  private t(key: 'format' | 'monthDay' | 'day' | 'range' | 'notAllowed'): string {
    const en = {
      format: 'Enter a date as YYYY/MM/DD',
      monthDay: 'Month or day is incorrect',
      day: 'Day is invalid for this month',
      range: 'Date is out of the supported year range',
      notAllowed: 'Date is not allowed',
    };
    const ne = {
      format: 'मिति वर्ष/महिना/दिन ढाँचामा लेख्नुहोस्',
      monthDay: 'महिना वा दिन गलत छ',
      day: 'दिन मिलेन, महिनाको कुल दिन जाँच्नुहोस्',
      range: 'मिति अपेक्षित वर्षहरूको सीमा बाहिर छ',
      notAllowed: 'यो मिति छान्न दिइएको छैन',
    };
    return (this.language === 'ne' ? ne : en)[key];
  }

  private system(): CalendarSystem {
    return this.calendarSystem === 'ad' ? 'ad' : 'bs';
  }

  /**
   * Parse and check what the user typed, in whichever calendar the field types in.
   *
   * The returned date is always Bikram Sambat: the year range and month length are checked in
   * the typed calendar, then the date is converted before the min / max rules apply.
   */
  private validate(raw: string): Validation {
    const text = raw.trim();
    if (!text) return { date: null, error: '' };
    const match = /^(\d{1,4})[-/](\d{1,2})[-/](\d{1,2})$/.exec(toLatinDigits(text));
    if (!match) return { date: null, error: this.t('format') };
    const system = this.system();
    const yearRange = yearRangeOf(system);
    const year = Number(match[1]);
    const month = Number(match[2]);
    const day = Number(match[3]);
    if (month < 1 || month > 12) return { date: null, error: this.t('monthDay') };
    if (year < yearRange.first || year > yearRange.last) return { date: null, error: this.t('range') };
    const daysInMonth = system === 'ad' ? getTotalDaysInAdMonth(year, month) : getTotalDaysInBsMonth(year, month);
    if (day < 1 || day > daysInMonth) return { date: null, error: this.t('day') };
    const canonical = toCanonical(system, { year, month, dayOfMonth: day });
    if (!canonical) return { date: null, error: this.t('range') };
    if (!isSelectable(canonical, parseIso(this.min), parseIso(this.max))) {
      return { date: null, error: this.t('notAllowed') };
    }
    return { date: canonical, error: '' };
  }

  private onInput(event: Event): void {
    this._error = this.validate((event.target as HTMLInputElement).value).error;
  }

  private onChange(event: Event): void {
    const raw = (event.target as HTMLInputElement).value;
    const result = this.validate(raw);
    this._error = result.error;
    if (result.date) {
      this.value = toIso(result.date);
      this.dispatchEvent(
        new CustomEvent<NepaliDatePickerChangeDetail>('change', {
          detail: buildChangeDetail(result.date, this.language),
          bubbles: true,
          composed: true,
        }),
      );
    } else if (result.error) {
      this.dispatchEvent(
        new CustomEvent<NepaliDateFieldInvalidDetail>('invalid', {
          detail: { message: result.error },
          bubbles: true,
          composed: true,
        }),
      );
    }
  }

  private get displayValue(): string {
    const parsed = parseIso(this.value);
    if (!parsed) return '';
    const displayed = fromCanonical(this.system(), parsed);
    if (!displayed) return '';
    return localizeDigits(toIso(displayed), this.language === 'ne' ? 'devanagari' : 'latin');
  }

  override willUpdate(changed: PropertyValues<this>): void {
    // The message names what was typed in the calendar that was on screen, so it cannot outlive a
    // switch. The digits themselves are re-derived by `displayValue` on every render.
    if (changed.has('calendarSystem')) this._error = '';
  }

  override updated(changed: PropertyValues): void {
    if (changed.has('_error')) this.classList.toggle('invalid', Boolean(this._error));
  }

  override render(): TemplateResult {
    const placeholder = this.language === 'ne' ? 'वर्ष/महिना/दिन' : 'YYYY/MM/DD';
    return html`
      <div class=${this._error ? 'field invalid' : 'field'}>
        ${this.label ? html`<label id="ndp-field-label">${this.label}</label>` : nothing}
        <input
          type="text"
          inputmode="numeric"
          placeholder=${placeholder}
          aria-labelledby=${this.label ? 'ndp-field-label' : nothing}
          aria-invalid=${this._error ? 'true' : 'false'}
          .value=${this.displayValue}
          ?disabled=${this.disabled}
          @input=${this.onInput}
          @change=${this.onChange}
        />
        <span class="error" role="alert">${this._error}</span>
      </div>
    `;
  }
}

if (!customElements.get('nepali-date-field')) {
  customElements.define('nepali-date-field', NepaliDateField);
}

declare global {
  interface HTMLElementTagNameMap {
    'nepali-date-field': NepaliDateField;
  }
}
