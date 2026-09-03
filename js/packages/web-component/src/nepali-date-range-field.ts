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
import { compare } from './internal/calendar-model.js';
import './nepali-date-field.js';
import type { NepaliDateField } from './nepali-date-field.js';

/**
 * A pair of Bikram Sambat date text fields for a start / end range, with the same typed validation as
 * `<nepali-date-field>` plus a cross-field check that the end is not before the start.
 *
 * @fires change - [NepaliDateRangeChangeDetail] whenever a valid start or end changes.
 */
export class NepaliDateRangeField extends LitElement {
  static override properties = {
    start: { type: String },
    end: { type: String },
    language: { type: String },
    min: { type: String },
    max: { type: String },
    disabled: { type: Boolean, reflect: true },
    startLabel: { type: String, attribute: 'start-label' },
    endLabel: { type: String, attribute: 'end-label' },
    _error: { state: true },
  };

  declare start: string;
  declare end: string;
  declare language: NepaliLanguage;
  declare min: string;
  declare max: string;
  declare disabled: boolean;
  declare startLabel: string;
  declare endLabel: string;
  declare private _error: string;

  static override styles = [
    css`
      :host {
        --ndp-error: #ba1a1a;
        display: inline-block;
        font-family: system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;
      }
      .row {
        display: inline-flex;
        gap: 12px;
        flex-wrap: wrap;
      }
      .error {
        color: var(--ndp-error);
        font-size: 0.75rem;
        min-height: 1em;
        margin-top: 4px;
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
    this.startLabel = '';
    this.endLabel = '';
    this._error = '';
  }

  override willUpdate(changed: PropertyValues<this>): void {
    if (changed.has('startLabel') && !this.startLabel) this.startLabel = this.language === 'ne' ? 'सुरु मिति' : 'Start date';
    if (changed.has('endLabel') && !this.endLabel) this.endLabel = this.language === 'ne' ? 'अन्त्य मिति' : 'End date';
  }

  private onStart(event: Event): void {
    // Keep the nested field's own change event from leaking out as the range's change.
    event.stopPropagation();
    this.start = (event.target as NepaliDateField).value || '';
    this.emit();
  }

  private onEnd(event: Event): void {
    event.stopPropagation();
    this.end = (event.target as NepaliDateField).value || '';
    this.emit();
  }

  private emit(): void {
    const start = parseIso(this.start);
    const end = parseIso(this.end);
    if (start && end && compare(end, start) < 0) {
      this._error = this.language === 'ne' ? 'अन्त्य मिति सुरु मिति भन्दा पछि हुनुपर्छ' : 'End date must not be before the start date';
      return;
    }
    this._error = '';
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
      <div class="row">
        <nepali-date-field
          class="start"
          label=${this.startLabel}
          language=${this.language}
          .value=${this.start}
          min=${this.min}
          max=${this.max}
          ?disabled=${this.disabled}
          @change=${this.onStart}
        ></nepali-date-field>
        <nepali-date-field
          class="end"
          label=${this.endLabel}
          language=${this.language}
          .value=${this.end}
          min=${this.start || this.min}
          max=${this.max}
          ?disabled=${this.disabled}
          @change=${this.onEnd}
        ></nepali-date-field>
      </div>
      <div class="error" role="alert">${this._error}</div>
    `;
  }
}

function adOf(date: CalendarDate): CalendarDate {
  const ad = convertBsToAd(date.year, date.month, date.dayOfMonth);
  return { year: ad.year, month: ad.month, dayOfMonth: ad.dayOfMonth };
}

if (!customElements.get('nepali-date-range-field')) {
  customElements.define('nepali-date-range-field', NepaliDateRangeField);
}

declare global {
  interface HTMLElementTagNameMap {
    'nepali-date-range-field': NepaliDateRangeField;
  }
}
