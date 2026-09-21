import { describe, it, expect, afterEach } from 'vitest';
import type { LitElement } from 'lit';
import {
  convertBsToAd,
  formatAdDate,
  getAdCalendarsInBsMonth,
  getAdMonthName,
  getBsCalendarsInAdMonth,
  getBsMonth,
  getBsMonthName,
  getTotalDaysInBsMonth,
  localizeDigits,
} from '@nepali-date-picker/core';
import '../src/index.js';
import type {
  NepaliDatePicker,
  NepaliDatePickerDialog,
  NepaliDatePickerDocked,
  NepaliDateRangePicker,
} from '../src/index.js';
import { secondaryDatesInMonth, secondaryMonthLabel, weekdayOf } from '../src/internal/calendar-model.js';

/** The month in the Compose screenshot: Asoj 2083, which straddles two Gregorian months. */
const MONTH = { year: 2083, month: 6 };
/** A month whose grid needs all six rows, so it has days to borrow at both edges. */
const TALL_MONTH = { year: 2083, month: 4 };

function iso({ year, month }: { year: number; month: number }, dayOfMonth: number): string {
  const pad = (n: number): string => String(n).padStart(2, '0');
  return `${year}-${pad(month)}-${pad(dayOfMonth)}`;
}

async function mount<T extends LitElement>(tag: string, attrs: Record<string, string> = {}): Promise<T> {
  const el = document.createElement(tag) as T;
  for (const [key, value] of Object.entries(attrs)) el.setAttribute(key, value);
  document.body.appendChild(el);
  await (el as unknown as { updateComplete: Promise<unknown> }).updateComplete;
  return el;
}

/** The cells of the displayed month, leaving out any borrowed from a neighbour. */
function ownCells(el: LitElement): HTMLButtonElement[] {
  return [...el.renderRoot.querySelectorAll<HTMLButtonElement>('button.day:not(.adjacent)')];
}

function textOf(cell: ParentNode, selector: string): string | null {
  return cell.querySelector(selector)?.textContent?.trim() ?? null;
}

function secondaryNumbers(cells: HTMLButtonElement[]): (string | null)[] {
  return cells.map((cell) => textOf(cell, '.secondary'));
}

function subtitleOf(el: LitElement): string | null {
  return textOf(el.renderRoot, '.secondary-month');
}

afterEach(() => {
  document.body.innerHTML = '';
});

describe('paired dates', () => {
  it('leaves the cells alone unless asked for', async () => {
    const el = await mount<NepaliDatePicker>('nepali-date-picker', { value: iso(MONTH, 15) });
    expect(el.renderRoot.querySelectorAll('.secondary')).toHaveLength(0);
    expect(subtitleOf(el)).toBeNull();
    expect(ownCells(el).every((cell) => !cell.classList.contains('dual'))).toBe(true);
  });

  it('pairs every day of a Bikram Sambat month with its Gregorian day', async () => {
    const el = await mount<NepaliDatePicker>('nepali-date-picker', {
      value: iso(MONTH, 15),
      'show-secondary-date': '',
    });
    const cells = ownCells(el);
    const gregorian = getAdCalendarsInBsMonth(MONTH.year, MONTH.month);

    expect(cells).toHaveLength(getTotalDaysInBsMonth(MONTH.year, MONTH.month));
    expect(cells.map((cell) => textOf(cell, '.primary'))).toEqual(
      cells.map((_, index) => String(index + 1)),
    );
    expect(secondaryNumbers(cells)).toEqual(gregorian.map((date) => String(date.dayOfMonth)));
    // Every cell takes the square shape, empty ones included, so the grid stays even.
    expect(cells.every((cell) => cell.classList.contains('dual'))).toBe(true);
    const blanks = [...el.renderRoot.querySelectorAll<HTMLElement>('.day.blank')];
    expect(blanks.length).toBeGreaterThan(0);
    expect(blanks.every((cell) => cell.classList.contains('dual'))).toBe(true);
  });

  it('pairs a Gregorian month with its Bikram Sambat days', async () => {
    const el = await mount<NepaliDatePicker>('nepali-date-picker', {
      value: iso(MONTH, 15),
      'calendar-system': 'ad',
      'show-secondary-date': '',
    });
    const displayed = convertBsToAd(MONTH.year, MONTH.month, 15);
    const nepali = getBsCalendarsInAdMonth(displayed.year, displayed.month);

    expect(secondaryNumbers(ownCells(el))).toEqual(
      nepali.map((date) => (date ? String(date.dayOfMonth) : null)),
    );
  });

  it('draws no counterpart for a day that has none', async () => {
    // The conversion table starts mid-Gregorian-month, so the days before its anchor pair with
    // nothing at all.
    const el = await mount<NepaliDatePicker>('nepali-date-picker', {
      value: '1970-01-01',
      'calendar-system': 'ad',
      'show-secondary-date': '',
    });
    const anchor = convertBsToAd(1970, 1, 1);
    const nepali = getBsCalendarsInAdMonth(anchor.year, anchor.month);
    const rendered = secondaryNumbers(ownCells(el));

    expect(nepali.some((date) => date === null)).toBe(true);
    expect(rendered.filter((value) => value === null)).toHaveLength(
      nepali.filter((date) => date === null).length,
    );
    expect(rendered[anchor.dayOfMonth - 1]).toBe('1');
  });

  it('names one month when the counterparts do not straddle two', async () => {
    // The first Gregorian month of the table only converts from its anchor onwards, and everything
    // after the anchor lands in a single Bikram Sambat month.
    const el = await mount<NepaliDatePicker>('nepali-date-picker', {
      value: '1970-01-01',
      'calendar-system': 'ad',
      'show-secondary-date': '',
    });
    const anchor = convertBsToAd(1970, 1, 1);
    const subtitle = subtitleOf(el);

    expect(subtitle).toBe(
      secondaryMonthLabel('ad', secondaryDatesInMonth('ad', anchor.year, anchor.month), 'en'),
    );
    expect(subtitle).not.toContain('/');
    expect(subtitle).toContain('1970');
  });

  it('names nothing for a month that converts nowhere', async () => {
    const el = await mount<NepaliDatePicker>('nepali-date-picker', {
      value: '1970-01-01',
      'calendar-system': 'ad',
      'show-secondary-date': '',
    });
    // Back from the anchor month to January 1913, which is inside the Gregorian year range yet
    // entirely before the conversion table starts.
    const [previous] = el.renderRoot.querySelectorAll<HTMLButtonElement>('.nav');
    for (let i = 0; i < 3; i += 1) {
      previous!.click();
      await el.updateComplete;
    }

    expect(subtitleOf(el)).toBeNull();
    expect(el.renderRoot.querySelectorAll('.secondary')).toHaveLength(0);
    expect(ownCells(el).every((cell) => cell.getAttribute('aria-disabled') === 'true')).toBe(true);
  });

  it('names the months the displayed month straddles', async () => {
    const el = await mount<NepaliDatePicker>('nepali-date-picker', {
      value: iso(MONTH, 15),
      'show-secondary-date': '',
    });
    const expected = secondaryMonthLabel(
      'bs',
      secondaryDatesInMonth('bs', MONTH.year, MONTH.month),
      'en',
    );

    expect(subtitleOf(el)).toBe(expected);
    // Asoj 2083 runs from September into October 2026.
    expect(expected).toBe('Sep/Oct 2026');
  });

  it('names them under the Gregorian grid too', async () => {
    const el = await mount<NepaliDatePicker>('nepali-date-picker', {
      value: iso(MONTH, 15),
      'calendar-system': 'ad',
      'show-secondary-date': '',
    });
    const displayed = convertBsToAd(MONTH.year, MONTH.month, 15);

    expect(subtitleOf(el)).toBe(
      secondaryMonthLabel('ad', secondaryDatesInMonth('ad', displayed.year, displayed.month), 'en'),
    );
  });

  it('names them for the switch alone, without pairing the cells', async () => {
    const el = await mount<NepaliDatePicker>('nepali-date-picker', {
      value: iso(MONTH, 15),
      'show-calendar-toggle': '',
    });
    expect(subtitleOf(el)).toBe('Sep/Oct 2026');
    expect(el.renderRoot.querySelectorAll('.secondary')).toHaveLength(0);
  });

  it('pairs a borrowed day with its own month, not the displayed one', async () => {
    const el = await mount<NepaliDatePicker>('nepali-date-picker', {
      value: iso(TALL_MONTH, 15),
      'show-adjacent-month-days': '',
      'show-secondary-date': '',
    });
    const leading = getBsMonth(TALL_MONTH.year, TALL_MONTH.month).daysFromStartOfWeekToFirstOfMonth;
    const previous = { year: TALL_MONTH.year, month: TALL_MONTH.month - 1 };
    const borrowed = [...el.renderRoot.querySelectorAll<HTMLButtonElement>('button.day.adjacent')];

    expect(leading).toBeGreaterThan(0);
    expect(secondaryNumbers(borrowed.slice(0, leading))).toEqual(
      getAdCalendarsInBsMonth(previous.year, previous.month)
        .slice(-leading)
        .map((date) => String(date.dayOfMonth)),
    );
  });

  it('writes both numbers in Devanagari', async () => {
    const el = await mount<NepaliDatePicker>('nepali-date-picker', {
      value: iso(MONTH, 15),
      language: 'ne',
      'show-secondary-date': '',
    });
    const [first] = ownCells(el);
    const gregorian = getAdCalendarsInBsMonth(MONTH.year, MONTH.month)[0];

    expect(textOf(first!, '.primary')).toBe(localizeDigits('1', 'devanagari'));
    expect(textOf(first!, '.secondary')).toBe(
      localizeDigits(String(gregorian.dayOfMonth), 'devanagari'),
    );
    expect(subtitleOf(el)).toBe(
      secondaryMonthLabel('bs', secondaryDatesInMonth('bs', MONTH.year, MONTH.month), 'ne'),
    );
  });

  it('speaks both dates of a cell', async () => {
    const el = await mount<NepaliDatePicker>('nepali-date-picker', {
      value: iso(MONTH, 15),
      'show-secondary-date': '',
    });
    const gregorian = getAdCalendarsInBsMonth(MONTH.year, MONTH.month)[0];
    const label = ownCells(el)[0]?.getAttribute('aria-label') ?? '';

    expect(label).toContain(`${getBsMonthName(MONTH.month, 'full', 'en')} 1, ${MONTH.year}`);
    expect(label).toContain(
      `${getAdMonthName(gregorian.month, 'full', 'en')} ${gregorian.dayOfMonth}, ${gregorian.year}`,
    );
  });

  it('speaks the Bikram Sambat half under the Gregorian grid', async () => {
    const el = await mount<NepaliDatePicker>('nepali-date-picker', {
      value: iso(MONTH, 15),
      'calendar-system': 'ad',
      'show-secondary-date': '',
    });
    const displayed = convertBsToAd(MONTH.year, MONTH.month, 15);
    const nepali = getBsCalendarsInAdMonth(displayed.year, displayed.month)[0];
    const label = ownCells(el)[0]?.getAttribute('aria-label') ?? '';

    expect(label).toContain(`${getAdMonthName(displayed.month, 'full', 'en')} 1, ${displayed.year}`);
    expect(label).toContain(
      `${getBsMonthName(nepali!.month, 'full', 'en')} ${nepali!.dayOfMonth}, ${nepali!.year}`,
    );
  });
});

describe('paired dates in the other elements', () => {
  it('pairs the range picker', async () => {
    const el = await mount<NepaliDateRangePicker>('nepali-date-range-picker', {
      start: iso(MONTH, 10),
      end: iso(MONTH, 18),
      'show-secondary-date': '',
    });
    expect(secondaryNumbers(ownCells(el))).toEqual(
      getAdCalendarsInBsMonth(MONTH.year, MONTH.month).map((date) => String(date.dayOfMonth)),
    );
    expect(subtitleOf(el)).toBe('Sep/Oct 2026');
  });

  it('pairs the docked popover', async () => {
    const el = await mount<NepaliDatePickerDocked>('nepali-date-picker-docked', {
      value: iso(MONTH, 15),
      'show-secondary-date': '',
    });
    el.renderRoot.querySelector<HTMLButtonElement>('.icon-button')!.click();
    await el.updateComplete;

    expect(secondaryNumbers(ownCells(el))).toEqual(
      getAdCalendarsInBsMonth(MONTH.year, MONTH.month).map((date) => String(date.dayOfMonth)),
    );
  });

  it('pairs the dialog and puts the Gregorian date under its headline', async () => {
    const el = await mount<NepaliDatePickerDialog>('nepali-date-picker-dialog', {
      value: iso(MONTH, 15),
      'show-secondary-date': '',
    });
    el.open = true;
    await el.updateComplete;

    const selected = { year: MONTH.year, month: MONTH.month, dayOfMonth: 15 };
    const gregorian = convertBsToAd(selected.year, selected.month, selected.dayOfMonth);

    expect(el.renderRoot.querySelectorAll('.secondary').length).toBeGreaterThan(0);
    expect(textOf(el.renderRoot, '.dialog-headline-secondary')).toBe(
      formatAdDate(
        gregorian.year,
        gregorian.month,
        gregorian.dayOfMonth,
        weekdayOf(selected),
        'en',
        'long',
        'medium',
        'medium',
        null,
      ),
    );
  });

  it('leaves the dialog headline alone without the attribute', async () => {
    const el = await mount<NepaliDatePickerDialog>('nepali-date-picker-dialog', {
      value: iso(MONTH, 15),
    });
    el.open = true;
    await el.updateComplete;
    expect(el.renderRoot.querySelector('.dialog-headline-secondary')).toBeNull();
  });
});
