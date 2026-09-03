/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import type { LitElement, ReactiveController } from 'lit';
import { addDaysToBsDate, getTodayBs } from '@nepali-date-picker/core';
import type { CalendarDate } from '../types.js';
import { clampDay, compare, isSelectable, stepMonth, weekdayOf, withinRange } from './calendar-model.js';

export type CalendarMode = 'single' | 'range';

/**
 * Owns the shared calendar state (viewed month, roving focus, and the current single or range
 * selection) plus every user interaction (month navigation, keyboard movement, and picking a day).
 *
 * It is a [ReactiveController], so any element that renders a month grid can delegate all of this
 * behavior instead of re-implementing it. After a selection changes it invokes [onSelect] so the
 * host can dispatch its own public event.
 */
export class CalendarController implements ReactiveController {
  viewYear: number;
  viewMonth: number;
  focus: CalendarDate;
  selected: CalendarDate | null = null;
  rangeStart: CalendarDate | null = null;
  rangeEnd: CalendarDate | null = null;

  /** Invoked whenever the selection changes as a result of user interaction. */
  onSelect?: () => void;

  private readonly host: LitElement;
  private mode: CalendarMode = 'single';
  private min: CalendarDate | null = null;
  private max: CalendarDate | null = null;
  private pendingFocus = false;

  constructor(host: LitElement) {
    this.host = host;
    host.addController(this);
    const today = getTodayBs();
    this.viewYear = today.year;
    this.viewMonth = today.month;
    this.focus = { year: today.year, month: today.month, dayOfMonth: today.dayOfMonth };
  }

  configure(options: { mode?: CalendarMode; min?: CalendarDate | null; max?: CalendarDate | null }): void {
    if (options.mode) this.mode = options.mode;
    if (options.min !== undefined) this.min = options.min;
    if (options.max !== undefined) this.max = options.max;
  }

  /** Set the single-selection value and move the view/focus onto it. */
  setSelected(date: CalendarDate | null): void {
    this.selected = date;
    if (date) this.moveTo(date);
  }

  /** Set the range selection and move the view/focus onto its start. */
  setRange(start: CalendarDate | null, end: CalendarDate | null): void {
    this.rangeStart = start;
    this.rangeEnd = end;
    if (start) this.moveTo(start);
  }

  hostUpdated(): void {
    if (!this.pendingFocus) return;
    this.pendingFocus = false;
    const cell = this.host.renderRoot.querySelector<HTMLElement>('.day[tabindex="0"]');
    cell?.focus();
  }

  isSelectable(date: CalendarDate): boolean {
    return isSelectable(date, this.min, this.max);
  }

  /** Whether a date should be shaded as part of the current range (range mode only). */
  inRange(date: CalendarDate): boolean {
    return this.mode === 'range' && withinRange(date, this.rangeStart, this.rangeEnd);
  }

  weekdayOf(date: CalendarDate): number {
    return weekdayOf(date);
  }

  setView(year: number, month: number, moveFocus: boolean): void {
    this.viewYear = year;
    this.viewMonth = month;
    if (moveFocus) {
      this.focus = { year, month, dayOfMonth: clampDay(year, month, this.focus.dayOfMonth) };
      this.pendingFocus = true;
    }
    this.host.requestUpdate();
  }

  changeMonth(delta: number): void {
    const next = stepMonth(this.viewYear, this.viewMonth, delta);
    if (next) this.setView(next.year, next.month, true);
  }

  goToToday(): void {
    const today = getTodayBs();
    this.moveTo({ year: today.year, month: today.month, dayOfMonth: today.dayOfMonth });
    this.pendingFocus = true;
    this.host.requestUpdate();
  }

  onKeydown(event: KeyboardEvent): void {
    switch (event.key) {
      case 'ArrowLeft':
        this.moveFocusByDays(-1);
        break;
      case 'ArrowRight':
        this.moveFocusByDays(1);
        break;
      case 'ArrowUp':
        this.moveFocusByDays(-7);
        break;
      case 'ArrowDown':
        this.moveFocusByDays(7);
        break;
      case 'Home':
        this.moveFocusByDays(-(weekdayOf(this.focus) - 1));
        break;
      case 'End':
        this.moveFocusByDays(7 - weekdayOf(this.focus));
        break;
      case 'PageUp':
        this.changeMonth(-1);
        break;
      case 'PageDown':
        this.changeMonth(1);
        break;
      default:
        return;
    }
    event.preventDefault();
  }

  /** Pick a day. In single mode it replaces the value; in range mode it sets start, then end. */
  pick(date: CalendarDate): void {
    if (!this.isSelectable(date)) return;
    this.focus = date;
    if (this.mode === 'single') {
      this.selected = date;
    } else if (!this.rangeStart || this.rangeEnd) {
      this.rangeStart = date;
      this.rangeEnd = null;
    } else if (compare(date, this.rangeStart) < 0) {
      this.rangeEnd = this.rangeStart;
      this.rangeStart = date;
    } else {
      this.rangeEnd = date;
    }
    this.host.requestUpdate();
    this.onSelect?.();
  }

  private moveTo(date: CalendarDate): void {
    this.viewYear = date.year;
    this.viewMonth = date.month;
    this.focus = date;
  }

  private moveFocusByDays(delta: number): void {
    try {
      const moved = addDaysToBsDate(this.focus.year, this.focus.month, this.focus.dayOfMonth, delta);
      this.focus = { year: moved.year, month: moved.month, dayOfMonth: moved.dayOfMonth };
      this.viewYear = moved.year;
      this.viewMonth = moved.month;
      this.pendingFocus = true;
      this.host.requestUpdate();
    } catch {
      // Reached a supported-range boundary: keep the current focus.
    }
  }
}
