/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import { LitElement, css, html, nothing } from 'lit';
import type { PropertyValues, TemplateResult } from 'lit';
import { localizeDigits } from '@nepali-date-picker/core';
import type { NepaliDatePickerChangeDetail, NepaliLanguage } from './types.js';
import { parseIso, toIso } from './utils.js';
import { buildChangeDetail } from './nepali-date-picker.js';
import { CalendarController } from './internal/calendar-controller.js';
import { renderCalendar } from './internal/calendar-view.js';
import { calendarStyles, fieldStyles, overlayStyles, tokens } from './internal/styles.js';

/**
 * A docked Bikram Sambat date picker: a text field the user can type into, plus a calendar button
 * that opens the month grid in an anchored popover.
 *
 * @fires change - [NepaliDatePickerChangeDetail] when a valid date is chosen by typing or clicking.
 */
export class NepaliDatePickerDocked extends LitElement {
  static override properties = {
    value: { type: String },
    language: { type: String },
    min: { type: String },
    max: { type: String },
    disabled: { type: Boolean, reflect: true },
    showEnglish: { type: Boolean, attribute: 'show-english' },
    label: { type: String },
    _open: { state: true },
  };

  declare value: string;
  declare language: NepaliLanguage;
  declare min: string;
  declare max: string;
  declare disabled: boolean;
  declare showEnglish: boolean;
  /** Optional field label. */
  declare label: string;
  declare private _open: boolean;

  private readonly cal = new CalendarController(this);
  private readonly onDocumentPointer = (event: Event): void => {
    if (!event.composedPath().includes(this)) this._open = false;
  };

  static override styles = [
    tokens,
    calendarStyles,
    fieldStyles,
    overlayStyles,
    css`
      :host {
        display: inline-block;
      }
      :host([disabled]) {
        opacity: 0.55;
        pointer-events: none;
      }
      .popover {
        padding: 8px;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.18);
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
    this.label = '';
    this._open = false;
    this.cal.onSelect = () => this.onCalendarPick();
  }

  override disconnectedCallback(): void {
    super.disconnectedCallback();
    document.removeEventListener('pointerdown', this.onDocumentPointer, true);
  }

  override willUpdate(changed: PropertyValues<this>): void {
    if (changed.has('min') || changed.has('max')) {
      this.cal.configure({ mode: 'single', min: parseIso(this.min), max: parseIso(this.max) });
    }
    if (changed.has('value')) {
      this.cal.setSelected(parseIso(this.value));
    }
  }

  override updated(changed: PropertyValues): void {
    if (changed.has('_open')) {
      if (this._open) document.addEventListener('pointerdown', this.onDocumentPointer, true);
      else document.removeEventListener('pointerdown', this.onDocumentPointer, true);
    }
  }

  private get displayValue(): string {
    const parsed = parseIso(this.value);
    if (!parsed) return '';
    return localizeDigits(toIso(parsed), this.language === 'ne' ? 'devanagari' : 'latin');
  }

  private commit(iso: string): void {
    const parsed = parseIso(iso);
    if (!parsed || !this.cal.isSelectable(parsed)) return;
    this.value = toIso(parsed);
    this.cal.setSelected(parsed);
    this.dispatchEvent(
      new CustomEvent<NepaliDatePickerChangeDetail>('change', {
        detail: buildChangeDetail(parsed, this.language),
        bubbles: true,
        composed: true,
      }),
    );
  }

  private onInputChange(event: Event): void {
    this.commit((event.target as HTMLInputElement).value);
  }

  private onInputKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter') this.commit((event.target as HTMLInputElement).value);
    else if (event.key === 'Escape') this._open = false;
  }

  private onCalendarPick(): void {
    const date = this.cal.selected;
    if (!date) return;
    this.value = toIso(date);
    this._open = false;
    this.dispatchEvent(
      new CustomEvent<NepaliDatePickerChangeDetail>('change', {
        detail: buildChangeDetail(date, this.language),
        bubbles: true,
        composed: true,
      }),
    );
  }

  override render(): TemplateResult {
    const placeholder = this.language === 'ne' ? 'वर्ष/महिना/दिन' : 'YYYY/MM/DD';
    const toggleLabel = this.language === 'ne' ? 'पात्रो खोल्नुहोस्' : 'Open calendar';
    return html`
      <div class="field anchor">
        ${this.label ? html`<label id="ndp-docked-label">${this.label}</label>` : nothing}
        <div class="row">
          <input
            type="text"
            inputmode="numeric"
            placeholder=${placeholder}
            aria-labelledby=${this.label ? 'ndp-docked-label' : nothing}
            .value=${this.displayValue}
            ?disabled=${this.disabled}
            @change=${this.onInputChange}
            @keydown=${this.onInputKeydown}
          />
          <button
            class="icon-button"
            aria-label=${toggleLabel}
            aria-haspopup="dialog"
            aria-expanded=${this._open ? 'true' : 'false'}
            ?disabled=${this.disabled}
            @click=${() => {
              this._open = !this._open;
            }}
          >
            📅
          </button>
        </div>
        ${this._open
          ? html`
              <div class="popover" role="dialog" aria-label=${this.language === 'ne' ? 'नेपाली मिति' : 'Nepali date'}>
                ${renderCalendar(this.cal, {
                  language: this.language,
                  disabled: false,
                  showFooter: true,
                  showEnglish: this.showEnglish,
                })}
              </div>
            `
          : nothing}
      </div>
    `;
  }
}

if (!customElements.get('nepali-date-picker-docked')) {
  customElements.define('nepali-date-picker-docked', NepaliDatePickerDocked);
}

declare global {
  interface HTMLElementTagNameMap {
    'nepali-date-picker-docked': NepaliDatePickerDocked;
  }
}
