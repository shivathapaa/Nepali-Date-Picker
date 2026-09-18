/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import { LitElement, css, html, nothing } from 'lit';
import type { PropertyValues, TemplateResult } from 'lit';
import { localizeDigits } from '@nepali-date-picker/core';
import type { CalendarSystem, NepaliDatePickerChangeDetail, NepaliLanguage } from './types.js';
import { parseIso, toIso } from './utils.js';
import { buildChangeDetail } from './nepali-date-picker.js';
import { CalendarController } from './internal/calendar-controller.js';
import { fromCanonical, toCanonical } from './internal/calendar-model.js';
import { renderCalendar } from './internal/calendar-view.js';
import { calendarStyles, fieldStyles, overlayStyles, tokens } from './internal/styles.js';

/**
 * A docked Bikram Sambat date picker: a text field the user can type into, plus a calendar button
 * that opens the month grid in an anchored popover.
 *
 * @fires {CustomEvent<NepaliDatePickerChangeDetail>} change - Dispatched when a valid date is
 *   chosen by typing or clicking.
 *
 * @cssprop [--ndp-font] - Font family for the whole element.
 * @cssprop [--ndp-bg] - Field and popover background.
 * @cssprop [--ndp-text] - Primary text color.
 * @cssprop [--ndp-muted] - Label, weekday headers and out-of-month days.
 * @cssprop [--ndp-accent] - Selection background and the field focus outline.
 * @cssprop [--ndp-on-accent] - Text color on top of the accent.
 * @cssprop [--ndp-hover] - Hover wash over a selectable day.
 * @cssprop [--ndp-today-ring] - Ring color marking today.
 * @cssprop [--ndp-border] - Field border, divider and outline color.
 * @cssprop [--ndp-error] - Border and message color when the typed date is rejected.
 * @cssprop [--ndp-radius] - Corner radius of the popover, surface and day cells.
 */
export class NepaliDatePickerDocked extends LitElement {
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
    label: { type: String },
    _open: { state: true },
  };

  declare value: string;
  declare language: NepaliLanguage;
  declare min: string;
  declare max: string;
  declare disabled: boolean;
  declare showEnglish: boolean;
  /**
   * Calendar the grid displays, `bs` (Bikram Sambat) or `ad` (Gregorian). Only the display
   * changes: `value` and the `change` event stay Bikram Sambat.
   */
  declare calendarSystem: CalendarSystem;
  /** Show a `B.S.` / `A.D.` switch above the month header. */
  declare showCalendarToggle: boolean;
  /**
   * Fill the grid's empty cells with the neighbouring months' days, drawn faded. Clicking one picks
   * that day and moves the grid to its month.
   */
  declare showAdjacentMonthDays: boolean;
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
    this.calendarSystem = 'bs';
    this.showCalendarToggle = false;
    this.showAdjacentMonthDays = false;
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
    // Applied before the value so the grid lands on the selection in the right calendar.
    if (changed.has('calendarSystem')) {
      this.cal.setSystem(this.calendarSystem === 'ad' ? 'ad' : 'bs');
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

  private system(): CalendarSystem {
    return this.calendarSystem === 'ad' ? 'ad' : 'bs';
  }

  /** The value written in the calendar on screen, which is also the calendar the field types in. */
  private get displayValue(): string {
    const parsed = parseIso(this.value);
    if (!parsed) return '';
    const displayed = fromCanonical(this.system(), parsed);
    if (!displayed) return '';
    return localizeDigits(toIso(displayed), this.language === 'ne' ? 'devanagari' : 'latin');
  }

  /**
   * Commits text typed into the field, read in the displayed calendar and stored as Bikram Sambat.
   *
   * The controller takes a displayed date to judge selectability and a canonical one to select, so
   * the two calls below are deliberately given different calendars.
   */
  private commit(typed: string): void {
    const displayed = parseIso(typed);
    if (!displayed || !this.cal.isSelectable(displayed)) return;
    const parsed = toCanonical(this.system(), displayed);
    if (!parsed) return;
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
                  showCalendarToggle: this.showCalendarToggle,
                  showAdjacentDays: this.showAdjacentMonthDays,
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
