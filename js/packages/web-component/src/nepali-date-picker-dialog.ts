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
import type { NepaliDatePickerChangeDetail, NepaliLanguage } from './types.js';
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
 * @fires change - [NepaliDatePickerChangeDetail] when the user confirms a date.
 * @fires cancel - When the user dismisses the dialog without confirming.
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
