/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import { LitElement, html, nothing } from 'lit';
import type { PropertyValues, TemplateResult } from 'lit';
import { classMap } from 'lit/directives/class-map.js';
import { formatBsDate } from '@nepali-date-picker/core';
import type { CalendarSystem, NepaliDatePickerChangeDetail, NepaliLanguage } from './types.js';
import { parseIso, toIso } from './utils.js';
import { buildChangeDetail } from './nepali-date-picker.js';
import { CalendarController } from './internal/calendar-controller.js';
import { weekdayOf } from './internal/calendar-model.js';
import { renderCalendar } from './internal/calendar-view.js';
import { calendarStyles, overlayStyles, tokens } from './internal/styles.js';

/**
 * A modal Bikram Sambat date picker dialog with a headline and confirm / cancel actions. Set
 * `fullscreen` for the full-screen variant.
 *
 * @fires {CustomEvent<NepaliDatePickerChangeDetail>} change - Dispatched when the user confirms a
 *   date.
 * @fires {CustomEvent} cancel - Dispatched, with no detail, when the user dismisses the dialog
 *   without confirming.
 *
 * @cssprop [--ndp-font] - Font family for the whole element.
 * @cssprop [--ndp-bg] - Dialog and calendar surface background.
 * @cssprop [--ndp-text] - Primary text color.
 * @cssprop [--ndp-muted] - Secondary text, weekday headers and out-of-month days.
 * @cssprop [--ndp-accent] - Selection background and the confirm action color.
 * @cssprop [--ndp-on-accent] - Text color on top of the accent.
 * @cssprop [--ndp-hover] - Hover wash over a day or an action.
 * @cssprop [--ndp-today-ring] - Ring color marking today.
 * @cssprop [--ndp-border] - Divider and outline color.
 * @cssprop [--ndp-radius] - Corner radius of the dialog, surface and day cells.
 */
export class NepaliDatePickerDialog extends LitElement {
  static override properties = {
    open: { type: Boolean, reflect: true },
    value: { type: String },
    language: { type: String },
    min: { type: String },
    max: { type: String },
    fullscreen: { type: Boolean },
    showEnglish: { type: Boolean, attribute: 'show-english' },
    calendarSystem: { type: String, attribute: 'calendar-system' },
    showCalendarToggle: { type: Boolean, attribute: 'show-calendar-toggle' },
    showAdjacentMonthDays: { type: Boolean, attribute: 'show-adjacent-month-days' },
    heading: { type: String },
  };

  /** Whether the dialog is shown. */
  declare open: boolean;
  /** Selected / initial date as a `YYYY-MM-DD` Bikram Sambat string. */
  declare value: string;
  declare language: NepaliLanguage;
  declare min: string;
  declare max: string;
  /** Render as a full-screen dialog instead of a centered card. */
  declare fullscreen: boolean;
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
  /** Dialog heading; defaults to a localized "Select Nepali Date". */
  declare heading: string;

  private readonly cal = new CalendarController(this);

  static override styles = [tokens, calendarStyles, overlayStyles];

  constructor() {
    super();
    this.open = false;
    this.value = '';
    this.language = 'en';
    this.min = '';
    this.max = '';
    this.fullscreen = false;
    this.showEnglish = false;
    this.calendarSystem = 'bs';
    this.showCalendarToggle = false;
    this.showAdjacentMonthDays = false;
    this.heading = '';
  }

  /** Open the dialog. */
  show(): void {
    this.open = true;
  }

  /** Close the dialog without emitting a selection. */
  close(): void {
    this.open = false;
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

  private onConfirm(): void {
    const date = this.cal.selected;
    if (!date) return;
    this.value = toIso(date);
    this.open = false;
    this.dispatchEvent(
      new CustomEvent<NepaliDatePickerChangeDetail>('change', {
        detail: buildChangeDetail(date, this.language),
        bubbles: true,
        composed: true,
      }),
    );
  }

  private onCancel(): void {
    this.cal.setSelected(parseIso(this.value));
    this.open = false;
    this.dispatchEvent(new CustomEvent('cancel', { bubbles: true, composed: true }));
  }

  private onBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) this.onCancel();
  }

  private onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      event.stopPropagation();
      this.onCancel();
    }
  }

  private headline(): string {
    const date = this.cal.selected;
    if (!date) return this.language === 'ne' ? 'मिति चयन गर्नुहोस्' : 'Select date';
    return formatBsDate(date.year, date.month, date.dayOfMonth, weekdayOf(date), this.language, 'full', 'medium', 'medium', null);
  }

  override render(): TemplateResult | typeof nothing {
    if (!this.open) return nothing;
    const dialogTitle = this.heading || (this.language === 'ne' ? 'नेपाली मिति चयन गर्नुहोस्' : 'Select Nepali Date');
    return html`
      <div class="backdrop" @click=${this.onBackdrop} @keydown=${this.onKeydown}>
        <div
          class=${classMap({ dialog: true, fullscreen: this.fullscreen })}
          role="dialog"
          aria-modal="true"
          aria-label=${dialogTitle}
          tabindex="-1"
        >
          <div class="dialog-title">${dialogTitle}</div>
          <div class="dialog-headline">${this.headline()}</div>
          ${renderCalendar(this.cal, {
            language: this.language,
            disabled: false,
            showFooter: true,
            showEnglish: this.showEnglish,
            showCalendarToggle: this.showCalendarToggle,
            showAdjacentDays: this.showAdjacentMonthDays,
          })}
          <div class="actions">
            <button @click=${this.onCancel}>${this.language === 'ne' ? 'रद्द गर्नुहोस्' : 'Cancel'}</button>
            <button ?disabled=${!this.cal.selected} @click=${this.onConfirm}>
              ${this.language === 'ne' ? 'भयो' : 'OK'}
            </button>
          </div>
        </div>
      </div>
    `;
  }

  override updated(changed: PropertyValues<this>): void {
    if (changed.has('open') && this.open) {
      this.renderRoot.querySelector<HTMLElement>('.dialog')?.focus();
    }
  }
}

if (!customElements.get('nepali-date-picker-dialog')) {
  customElements.define('nepali-date-picker-dialog', NepaliDatePickerDialog);
}

declare global {
  interface HTMLElementTagNameMap {
    'nepali-date-picker-dialog': NepaliDatePickerDialog;
  }
}
