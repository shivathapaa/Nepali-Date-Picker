import { describe, it, expect, afterEach } from 'vitest';
import type { LitElement } from 'lit';
import { getBsMonth, getTotalDaysInBsMonth } from '@nepali-date-picker/core';
import '../src/index.js';
import type {
  NepaliDatePicker,
  NepaliDatePickerDialog,
  NepaliDatePickerDocked,
  NepaliDateRangePicker,
} from '../src/index.js';

/** A month whose grid needs all six rows, and one that fits in five. */
const TALL_MONTH = { year: 2083, month: 4 };
const SHORT_MONTH = pickShortMonth();

function leadingOf(year: number, month: number): number {
  return getBsMonth(year, month).daysFromStartOfWeekToFirstOfMonth;
}

/** The first month of 2083 whose leading cells plus days stop short of six rows. */
function pickShortMonth(): { year: number; month: number } {
  for (let month = 1; month <= 12; month += 1) {
    const filled = leadingOf(2083, month) + getTotalDaysInBsMonth(2083, month);
    if (filled <= 35) return { year: 2083, month };
  }
  throw new Error('no month of 2083 fits in five rows');
}

async function mount(attrs: Record<string, string> = {}): Promise<NepaliDatePicker> {
  const el = document.createElement('nepali-date-picker');
  for (const [key, value] of Object.entries(attrs)) el.setAttribute(key, value);
  document.body.appendChild(el);
  await el.updateComplete;
  return el;
}

async function mountVariant<T extends LitElement>(
  tag: string,
  attrs: Record<string, string> = {},
): Promise<T> {
  const el = document.createElement(tag) as T;
  for (const [key, value] of Object.entries(attrs)) el.setAttribute(key, value);
  document.body.appendChild(el);
  await el.updateComplete;
  return el;
}

function iso({ year, month }: { year: number; month: number }, dayOfMonth: number): string {
  const pad = (n: number): string => String(n).padStart(2, '0');
  return `${year}-${pad(month)}-${pad(dayOfMonth)}`;
}

function cells(el: LitElement): HTMLElement[] {
  return [...el.renderRoot.querySelectorAll<HTMLElement>('.day')];
}

function adjacentCells(el: LitElement): HTMLButtonElement[] {
  return [...el.renderRoot.querySelectorAll<HTMLButtonElement>('button.day.adjacent')];
}

function monthLabel(el: LitElement): string {
  return el.renderRoot.querySelector('.label span')?.textContent?.trim() ?? '';
}

afterEach(() => {
  document.body.innerHTML = '';
});

describe('adjacent month days', () => {
  it('leaves the edge cells blank unless asked for', async () => {
    const el = await mount({ value: iso(TALL_MONTH, 15) });
    expect(adjacentCells(el)).toHaveLength(0);
    expect(el.renderRoot.querySelectorAll('.day.blank').length).toBeGreaterThan(0);
  });

  it('fills the edge cells with the neighbours when asked for', async () => {
    const el = await mount({ value: iso(TALL_MONTH, 15), 'show-adjacent-month-days': '' });
    expect(el.renderRoot.querySelectorAll('.day.blank')).toHaveLength(0);
    expect(adjacentCells(el).length).toBeGreaterThan(0);
  });

  it('starts the grid with the tail of the previous month', async () => {
    const el = await mount({ value: iso(TALL_MONTH, 15), 'show-adjacent-month-days': '' });
    const leading = leadingOf(TALL_MONTH.year, TALL_MONTH.month);
    const previousDays = getTotalDaysInBsMonth(TALL_MONTH.year, TALL_MONTH.month - 1);

    const rendered = cells(el)
      .slice(0, leading)
      .map((cell) => cell.textContent?.trim());
    const expected = Array.from({ length: leading }, (_, i) =>
      String(previousDays - leading + 1 + i),
    );
    expect(rendered).toEqual(expected);
  });

  it('ends the grid with the head of the next month', async () => {
    const el = await mount({ value: iso(TALL_MONTH, 15), 'show-adjacent-month-days': '' });
    const leading = leadingOf(TALL_MONTH.year, TALL_MONTH.month);
    const trailing = 42 - leading - getTotalDaysInBsMonth(TALL_MONTH.year, TALL_MONTH.month);

    const rendered = cells(el)
      .slice(-trailing)
      .map((cell) => cell.textContent?.trim());
    expect(rendered).toEqual(Array.from({ length: trailing }, (_, i) => String(i + 1)));
  });

  it('keeps the grid a whole number of weeks', async () => {
    const el = await mount({ value: iso(TALL_MONTH, 15), 'show-adjacent-month-days': '' });
    expect(cells(el)).toHaveLength(42);
  });

  it('does not pad past the last row holding a day of the month', async () => {
    const el = await mount({ value: iso(SHORT_MONTH, 10), 'show-adjacent-month-days': '' });
    expect(cells(el)).toHaveLength(35);
  });

  it('picks the day and moves the grid when a next-month cell is clicked', async () => {
    const el = await mount({ value: iso(TALL_MONTH, 15), 'show-adjacent-month-days': '' });
    const leading = leadingOf(TALL_MONTH.year, TALL_MONTH.month);
    const trailing = 42 - leading - getTotalDaysInBsMonth(TALL_MONTH.year, TALL_MONTH.month);
    const before = monthLabel(el);

    (cells(el).at(-trailing) as HTMLButtonElement).click();
    await el.updateComplete;

    expect(monthLabel(el)).not.toBe(before);
    expect(el.value).toBe(iso({ year: TALL_MONTH.year, month: TALL_MONTH.month + 1 }, 1));
  });

  it('picks the day and moves the grid back when a previous-month cell is clicked', async () => {
    const el = await mount({ value: iso(TALL_MONTH, 15), 'show-adjacent-month-days': '' });
    const leading = leadingOf(TALL_MONTH.year, TALL_MONTH.month);
    const previousMonth = { year: TALL_MONTH.year, month: TALL_MONTH.month - 1 };
    const before = monthLabel(el);

    (cells(el)[leading - 1] as HTMLButtonElement).click();
    await el.updateComplete;

    expect(monthLabel(el)).not.toBe(before);
    expect(el.value).toBe(
      iso(previousMonth, getTotalDaysInBsMonth(previousMonth.year, previousMonth.month)),
    );
  });

  it('leaves a day the picker cannot select inert', async () => {
    const el = await mount({
      value: iso(TALL_MONTH, 15),
      min: iso(TALL_MONTH, 1),
      'show-adjacent-month-days': '',
    });
    const leading = leadingOf(TALL_MONTH.year, TALL_MONTH.month);
    const before = monthLabel(el);
    const cell = cells(el)[leading - 1] as HTMLButtonElement;
    expect(cell.getAttribute('aria-disabled')).toBe('true');

    cell.click();
    await el.updateComplete;

    expect(monthLabel(el)).toBe(before);
    expect(el.value).toBe(iso(TALL_MONTH, 15));
  });

  it('names the month an adjacent day belongs to, for a screen reader', async () => {
    const el = await mount({ value: iso(TALL_MONTH, 15), 'show-adjacent-month-days': '' });
    const leading = leadingOf(TALL_MONTH.year, TALL_MONTH.month);
    const label = cells(el)[leading - 1]?.getAttribute('aria-label') ?? '';
    expect(label).toContain('Asar');
    expect(label).not.toContain('Shrawn');
  });

  it('announces that an adjacent day moves the grid', async () => {
    const el = await mount({ value: iso(TALL_MONTH, 15), 'show-adjacent-month-days': '' });
    const leading = leadingOf(TALL_MONTH.year, TALL_MONTH.month);

    for (const cell of adjacentCells(el)) {
      expect(cell.getAttribute('aria-label')).toContain('shows another month');
    }
    // An own-month cell says nothing about moving, since tapping it does not.
    const ownDay = cells(el)[leading] as HTMLButtonElement;
    expect(ownDay.getAttribute('aria-label')).not.toContain('shows another month');
  });

  it('announces it in Nepali too', async () => {
    const el = await mount({
      value: iso(TALL_MONTH, 15),
      language: 'ne',
      'show-adjacent-month-days': '',
    });

    for (const cell of adjacentCells(el)) {
      expect(cell.getAttribute('aria-label')).toContain('अर्को महिना देखाउँछ');
    }
  });

  it('keeps the adjacent cells out of the keyboard tab order', async () => {
    const el = await mount({ value: iso(TALL_MONTH, 15), 'show-adjacent-month-days': '' });
    for (const cell of adjacentCells(el)) {
      expect(cell.getAttribute('tabindex')).toBe('-1');
    }
  });

  it('fills the edges in the Gregorian view too', async () => {
    const el = await mount({
      value: iso(TALL_MONTH, 15),
      'calendar-system': 'ad',
      'show-adjacent-month-days': '',
    });
    expect(el.renderRoot.querySelectorAll('.day.blank')).toHaveLength(0);
    expect(adjacentCells(el).length).toBeGreaterThan(0);
  });

  it('fills the edges in the docked popover', async () => {
    const el = await mountVariant<NepaliDatePickerDocked>('nepali-date-picker-docked', {
      value: iso(TALL_MONTH, 15),
      'show-adjacent-month-days': '',
    });
    el.renderRoot.querySelector<HTMLButtonElement>('.icon-button')!.click();
    await el.updateComplete;

    expect(el.renderRoot.querySelectorAll('.day.blank')).toHaveLength(0);
    expect(adjacentCells(el).length).toBeGreaterThan(0);
  });

  it('fills the edges in the dialog', async () => {
    const el = await mountVariant<NepaliDatePickerDialog>('nepali-date-picker-dialog', {
      value: iso(TALL_MONTH, 15),
      'show-adjacent-month-days': '',
    });
    el.open = true;
    await el.updateComplete;

    expect(el.renderRoot.querySelectorAll('.day.blank')).toHaveLength(0);
    expect(adjacentCells(el).length).toBeGreaterThan(0);
  });

  it('starts a range on a borrowed day and moves the range picker to its month', async () => {
    const el = await mountVariant<NepaliDateRangePicker>('nepali-date-range-picker', {
      start: iso(TALL_MONTH, 15),
      'show-adjacent-month-days': '',
    });
    const leading = leadingOf(TALL_MONTH.year, TALL_MONTH.month);
    const previousMonth = { year: TALL_MONTH.year, month: TALL_MONTH.month - 1 };
    const before = monthLabel(el);

    (cells(el)[leading - 1] as HTMLButtonElement).click();
    await el.updateComplete;

    expect(monthLabel(el)).not.toBe(before);
    expect(el.start).toBe(
      iso(previousMonth, getTotalDaysInBsMonth(previousMonth.year, previousMonth.month)),
    );
  });

  it('leaves the range picker edges blank unless asked for', async () => {
    const el = await mountVariant<NepaliDateRangePicker>('nepali-date-range-picker', {
      start: iso(TALL_MONTH, 15),
    });
    expect(adjacentCells(el)).toHaveLength(0);
    expect(el.renderRoot.querySelectorAll('.day.blank').length).toBeGreaterThan(0);
  });
});
