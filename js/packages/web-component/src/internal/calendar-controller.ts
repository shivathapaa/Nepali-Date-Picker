/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import type { LitElement, ReactiveController } from 'lit';
import { addDaysToBsDate, getTodayBs } from '@nepali-date-picker/core';
import type { CalendarDate, CalendarSystem } from '../types.js';
import {
  canonicalDatesInMonth,
  clampDay,
  compare,
  fromCanonical,
  isSelectable,
  stepMonth,
  weekdayOf,
  withinRange,
  yearRangeOf,
} from './calendar-model.js';

export type CalendarMode = 'single' | 'range';

/**
 * Owns the shared calendar state (viewed month, roving focus, and the current single or range
 * selection) plus every user interaction (month navigation, keyboard movement, and picking a day).
 *
 * It is a [ReactiveController], so any element that renders a month grid can delegate all of this
 * behavior instead of re-implementing it. After a selection changes it invokes [onSelect] so the
 * host can dispatch its own public event.
 *
 * The grid can show either calendar. [viewYear], [viewMonth] and [focus] follow [system], while
 * [selected], [rangeStart] and [rangeEnd] are always Bikram Sambat, which is what makes switching
 * calendars keep the same day selected.
 */
export class CalendarController implements ReactiveController {
  viewYear: number;
  viewMonth: number;
  /** Roving focus, expressed in [system]. */
  focus: CalendarDate;
  selected: CalendarDate | null = null;
  rangeStart: CalendarDate | null = null;
  rangeEnd: CalendarDate | null = null;
  system: CalendarSystem = 'bs';

  /** Invoked whenever the selection changes as a result of user interaction. */
  onSelect?: () => void;

  private readonly host: LitElement;
  private mode: CalendarMode = 'single';
  private min: CalendarDate | null = null;
  private max: CalendarDate | null = null;
  private pendingFocus = false;
  private canonicalCache: { key: string; dates: (CalendarDate | null)[] } | null = null;

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

  /** Set the single-selection value (Bikram Sambat) and move the view/focus onto it. */
  setSelected(date: CalendarDate | null): void {
    this.selected = date;
    if (date) this.moveTo(date);
  }

  /** Set the range selection (Bikram Sambat) and move the view/focus onto its start. */
  setRange(start: CalendarDate | null, end: CalendarDate | null): void {
    this.rangeStart = start;
    this.rangeEnd = end;
    if (start) this.moveTo(start);
  }

  /** Switch the displayed calendar, re-anchoring on the selection so the same day stays in view. */
  setSystem(system: CalendarSystem): void {
    if (system === this.system) return;
    const anchor = this.selected ?? this.rangeStart ?? this.canonicalOf(this.focus);
    this.system = system;
    this.canonicalCache = null;
    if (anchor) {
      this.moveTo(anchor);
    } else {
      const range = yearRangeOf(system);
      this.viewYear = Math.min(Math.max(this.viewYear, range.first), range.last);
      this.focus = { year: this.viewYear, month: this.viewMonth, dayOfMonth: 1 };
    }
    this.pendingFocus = true;
    this.host.requestUpdate();
  }

  hostUpdated(): void {
    if (!this.pendingFocus) return;
    this.pendingFocus = false;
    const cell = this.host.renderRoot.querySelector<HTMLElement>('.day[tabindex="0"]');
    cell?.focus();
  }

  /** The Bikram Sambat date a displayed cell selects, or `null` when it has none. */
  canonicalOf(date: CalendarDate): CalendarDate | null {
    if (this.system === 'bs') return date;
    const key = `${this.viewYear}-${this.viewMonth}`;
    if (this.canonicalCache?.key !== key) {
      this.canonicalCache = { key, dates: canonicalDatesInMonth('ad', this.viewYear, this.viewMonth) };
    }
    if (date.year !== this.viewYear || date.month !== this.viewMonth) {
      // Outside the cached month (keyboard movement mid-step); fall back to a single conversion.
      return canonicalDatesInMonth('ad', date.year, date.month)[date.dayOfMonth - 1] ?? null;
    }
    return this.canonicalCache.dates[date.dayOfMonth - 1] ?? null;
  }

  /** Whether a displayed cell can be picked. */
  isSelectable(date: CalendarDate): boolean {
    const canonical = this.canonicalOf(date);
    return canonical != null && isSelectable(canonical, this.min, this.max);
  }

  isSelected(date: CalendarDate): boolean {
    return this.matches(this.selected, date);
  }

  isRangeStart(date: CalendarDate): boolean {
    return this.matches(this.rangeStart, date);
  }

  isRangeEnd(date: CalendarDate): boolean {
    return this.matches(this.rangeEnd, date);
  }

  isFocused(date: CalendarDate): boolean {
    return (
      this.focus.year === date.year && this.focus.month === date.month && this.focus.dayOfMonth === date.dayOfMonth
    );
  }

  isToday(date: CalendarDate): boolean {
    const today = getTodayBs();
    return this.matches({ year: today.year, month: today.month, dayOfMonth: today.dayOfMonth }, date);
  }

  /** Whether a displayed cell should be shaded as part of the current range (range mode only). */
  inRange(date: CalendarDate): boolean {
    if (this.mode !== 'range') return false;
    const canonical = this.canonicalOf(date);
    return canonical != null && withinRange(canonical, this.rangeStart, this.rangeEnd);
  }

  weekdayOf(date: CalendarDate): number {
    return weekdayOf(date, this.system);
  }

  setView(year: number, month: number, moveFocus: boolean): void {
    this.viewYear = year;
    this.viewMonth = month;
    this.canonicalCache = null;
    if (moveFocus) {
      this.focus = { year, month, dayOfMonth: clampDay(this.system, year, month, this.focus.dayOfMonth) };
      this.pendingFocus = true;
    }
    this.host.requestUpdate();
  }

  changeMonth(delta: number): void {
    const next = stepMonth(this.system, this.viewYear, this.viewMonth, delta);
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
        this.moveFocusByDays(-(this.weekdayOf(this.focus) - 1));
        break;
      case 'End':
        this.moveFocusByDays(7 - this.weekdayOf(this.focus));
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

  /** Pick a displayed day. In single mode it replaces the value; in range mode it sets start, then end. */
  pick(date: CalendarDate): void {
    const canonical = this.canonicalOf(date);
    if (!canonical || !isSelectable(canonical, this.min, this.max)) return;
    this.focus = date;
    if (this.mode === 'single') {
      this.selected = canonical;
    } else if (!this.rangeStart || this.rangeEnd) {
      this.rangeStart = canonical;
      this.rangeEnd = null;
    } else if (compare(canonical, this.rangeStart) < 0) {
      this.rangeEnd = this.rangeStart;
      this.rangeStart = canonical;
    } else {
      this.rangeEnd = canonical;
    }
    this.host.requestUpdate();
    this.onSelect?.();
  }

  /** Whether a Bikram Sambat date and a displayed cell are the same day. */
  private matches(canonical: CalendarDate | null, date: CalendarDate): boolean {
    if (!canonical) return false;
    const displayed = fromCanonical(this.system, canonical);
    if (!displayed) return false;
    return (
      displayed.year === date.year && displayed.month === date.month && displayed.dayOfMonth === date.dayOfMonth
    );
  }

  /** Move the view and focus onto a Bikram Sambat date, expressed in the displayed calendar. */
  private moveTo(canonical: CalendarDate): void {
    const displayed = fromCanonical(this.system, canonical);
    if (!displayed) return;
    const range = yearRangeOf(this.system);
    if (displayed.year < range.first || displayed.year > range.last) return;
    this.viewYear = displayed.year;
    this.viewMonth = displayed.month;
    this.focus = displayed;
    this.canonicalCache = null;
  }

  private moveFocusByDays(delta: number): void {
    const canonical = this.canonicalOf(this.focus);
    if (!canonical) return;
    try {
      const moved = addDaysToBsDate(canonical.year, canonical.month, canonical.dayOfMonth, delta);
      this.moveTo({ year: moved.year, month: moved.month, dayOfMonth: moved.dayOfMonth });
      this.pendingFocus = true;
      this.host.requestUpdate();
    } catch {
      // Reached a supported-range boundary: keep the current focus.
    }
  }
}
