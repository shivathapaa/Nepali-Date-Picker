import { describe, it, expect, afterEach } from 'vitest';
import type { LitElement } from 'lit';
import {
  convertAdToBs,
  convertBsToAd,
  getAdYearRangeForBsYears,
  getBsYearRange,
  getTotalDaysInAdMonth,
  getTotalDaysInBsMonth,
} from '@nepali-date-picker/core';
import '../src/index.js';
import type {
  NepaliDateField,
  NepaliDatePicker,
  NepaliDatePickerDialog,
  NepaliDatePickerDocked,
  NepaliDateRangeField,
  NepaliDateRangePicker,
  NepaliWheelDatePicker,
} from '../src/index.js';
import type {
  NepaliDatePickerChangeDetail,
  NepaliDateRangeChangeDetail,
} from '../src/types.js';

async function mount(attrs: Record<string, string> = {}): Promise<NepaliDatePicker> {
  const el = document.createElement('nepali-date-picker');
  for (const [key, value] of Object.entries(attrs)) el.setAttribute(key, value);
  document.body.appendChild(el);
  await el.updateComplete;
  return el;
}

function dayButtons(el: LitElement): HTMLButtonElement[] {
  return [...el.renderRoot.querySelectorAll<HTMLButtonElement>('button.day:not(.blank)')];
}

function dayByText(el: LitElement, text: string): HTMLButtonElement {
  const found = dayButtons(el).find((b) => b.textContent?.trim() === text);
  if (!found) throw new Error(`no day cell with text "${text}"`);
  return found;
}

function segments(el: LitElement): HTMLButtonElement[] {
  return [...el.renderRoot.querySelectorAll<HTMLButtonElement>('button.segment')];
}

/** The engine hands back a fully described calendar; an event only carries the date. */
function expectSameDay(
  actual: { year: number; month: number; dayOfMonth: number },
  expected: { year: number; month: number; dayOfMonth: number },
): void {
  expect(actual.year).toBe(expected.year);
  expect(actual.month).toBe(expected.month);
  expect(actual.dayOfMonth).toBe(expected.dayOfMonth);
}

/** Commits text into one of the fields the range field composes. */
async function typeInto(host: LitElement, field: NepaliDateField, text: string): Promise<void> {
  await field.updateComplete;
  const input = field.renderRoot.querySelector<HTMLInputElement>('input')!;
  input.value = text;
  input.dispatchEvent(new Event('change', { bubbles: true }));
  await host.updateComplete;
}

afterEach(() => {
  document.body.innerHTML = '';
});

describe('calendar system switching', () => {
  it('hides the switch unless asked for', async () => {
    const el = await mount({ value: '2083-06-01' });
    expect(segments(el)).toHaveLength(0);
  });

  it('shows two segments when asked for, with the active one marked', async () => {
    const el = await mount({ value: '2083-06-01', 'show-calendar-toggle': '' });
    const [bs, ad] = segments(el);
    expect(bs.textContent?.trim()).toBe('B.S.');
    expect(ad.textContent?.trim()).toBe('A.D.');
    expect(bs.getAttribute('aria-checked')).toBe('true');
    expect(ad.getAttribute('aria-checked')).toBe('false');
  });

  it('renders Gregorian month lengths in the Gregorian grid', async () => {
    // BS 2083-06-01 is AD 2026-09-17, so the grid opens on a 30-day September.
    const el = await mount({ value: '2083-06-01', 'calendar-system': 'ad' });
    expect(dayButtons(el).length).toBe(getTotalDaysInAdMonth(2026, 9));
    expect(dayButtons(el).length).not.toBe(getTotalDaysInBsMonth(2083, 6));
  });

  it('keeps the selection when the switch is used', async () => {
    const el = await mount({ value: '2083-06-01', 'show-calendar-toggle': '' });
    const [, ad] = segments(el);

    ad.click();
    await el.updateComplete;

    expect(el.value).toBe('2083-06-01');
    const selected = el.renderRoot.querySelector<HTMLButtonElement>('button.day.selected');
    // The same day, now written the Gregorian way.
    expect(selected?.textContent?.trim()).toBe('17');
  });

  it('reports a Bikram Sambat date when a Gregorian cell is picked', async () => {
    const el = await mount({ value: '2083-06-01', 'calendar-system': 'ad' });
    let detail: NepaliDatePickerChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDatePickerChangeDetail>).detail;
    });

    dayByText(el, '20').click();
    await el.updateComplete;

    const expected = convertAdToBs(2026, 9, 20);
    expect(detail!.bs).toEqual({
      year: expected.year,
      month: expected.month,
      dayOfMonth: expected.dayOfMonth,
    });
    expect(detail!.ad).toEqual({ year: 2026, month: 9, dayOfMonth: 20 });
  });

  it('offers Gregorian years in the year select', async () => {
    const el = await mount({ value: '2083-06-01', 'calendar-system': 'ad' });
    const select = el.renderRoot.querySelector<HTMLSelectElement>('select');
    const adRange = getAdYearRangeForBsYears(getBsYearRange().first, getBsYearRange().last);
    expect(select?.options.length).toBe(adRange.last - adRange.first + 1);
    expect(select?.value).toBe('2026');
  });

  it('disables Gregorian days that predate the conversion anchor', async () => {
    // BS 1970-01-01 is AD 1913-04-13, so the first twelve days of that month convert to nothing.
    const el = await mount({ value: '1970-01-01', 'calendar-system': 'ad' });
    expect(dayByText(el, '1').getAttribute('aria-disabled')).toBe('true');
    expect(dayByText(el, '13').getAttribute('aria-disabled')).toBe('false');
  });

  it('labels the switch in Nepali', async () => {
    const el = await mount({ language: 'ne', 'show-calendar-toggle': '' });
    const [bs, ad] = segments(el);
    expect(bs.textContent?.trim()).toBe('बि.सं.');
    expect(ad.textContent?.trim()).toBe('ई.सं.');
  });

  it('spins the wheel in Gregorian but reports Bikram Sambat', async () => {
    const el = document.createElement('nepali-wheel-date-picker') as NepaliWheelDatePicker;
    el.setAttribute('value', '2083-06-01');
    el.setAttribute('calendar-system', 'ad');
    document.body.appendChild(el);
    await el.updateComplete;

    const selected = [...el.renderRoot.querySelectorAll<HTMLElement>('.option[aria-selected="true"]')];
    // Year / month / day columns all centre on the Gregorian equivalent of the value.
    expect(selected.map((o) => o.textContent?.trim())).toEqual(['2026', 'September', '17']);

    let detail: NepaliDatePickerChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDatePickerChangeDetail>).detail;
    });
    const dayColumn = el.renderRoot.querySelectorAll('.column')[2]!;
    [...dayColumn.querySelectorAll<HTMLButtonElement>('button.option')]
      .find((b) => b.textContent?.trim() === '20')!
      .click();
    await el.updateComplete;

    const expected = convertAdToBs(2026, 9, 20);
    expect(detail!.bs.year).toBe(expected.year);
    expect(detail!.bs.month).toBe(expected.month);
    expect(detail!.bs.dayOfMonth).toBe(expected.dayOfMonth);
    expect(el.value).toBe(
      `${expected.year}-${String(expected.month).padStart(2, '0')}-${String(expected.dayOfMonth).padStart(2, '0')}`,
    );
  });

  it('parses a typed Gregorian date back to Bikram Sambat in the field', async () => {
    const el = document.createElement('nepali-date-field') as NepaliDateField;
    el.setAttribute('calendar-system', 'ad');
    document.body.appendChild(el);
    await el.updateComplete;

    let detail: NepaliDatePickerChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDatePickerChangeDetail>).detail;
    });

    const input = el.renderRoot.querySelector<HTMLInputElement>('input')!;
    input.value = '2026/09/17';
    input.dispatchEvent(new Event('change', { bubbles: true }));
    await el.updateComplete;

    expect(detail!.bs).toEqual({ year: 2083, month: 6, dayOfMonth: 1 });
    expect(el.value).toBe('2083-06-01');
  });

  it('writes an existing Bikram Sambat value out in Gregorian in the field', async () => {
    const el = document.createElement('nepali-date-field') as NepaliDateField;
    el.setAttribute('value', '2083-06-01');
    el.setAttribute('calendar-system', 'ad');
    document.body.appendChild(el);
    await el.updateComplete;

    expect(el.renderRoot.querySelector<HTMLInputElement>('input')!.value).toBe('2026-09-17');
  });

  it('keeps a selected range across a switch in the range picker', async () => {
    const el = document.createElement('nepali-date-range-picker') as NepaliDateRangePicker;
    el.setAttribute('start', '2083-06-01');
    el.setAttribute('end', '2083-06-10');
    el.setAttribute('show-calendar-toggle', '');
    document.body.appendChild(el);
    await el.updateComplete;

    const [, ad] = segments(el);
    ad.click();
    await el.updateComplete;

    expect(el.start).toBe('2083-06-01');
    expect(el.end).toBe('2083-06-10');
    // AD 2026-09-17 is the start; its cell must carry the range-start marker.
    const start = el.renderRoot.querySelector<HTMLButtonElement>('button.day.range-start');
    expect(start?.textContent?.trim()).toBe(String(convertBsToAd(2083, 6, 1).dayOfMonth));
  });

  it('opens the docked popover in Gregorian and still reports Bikram Sambat', async () => {
    const el = document.createElement('nepali-date-picker-docked') as NepaliDatePickerDocked;
    el.setAttribute('value', '2083-06-01');
    el.setAttribute('calendar-system', 'ad');
    document.body.appendChild(el);
    await el.updateComplete;

    el.renderRoot.querySelector<HTMLButtonElement>('.icon-button')!.click();
    await el.updateComplete;

    // September 2026 has 30 days; no Bikram Sambat month does.
    expect(dayButtons(el)).toHaveLength(getTotalDaysInAdMonth(2026, 9));

    let detail: NepaliDatePickerChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDatePickerChangeDetail>).detail;
    });
    dayByText(el, '20').click();
    await el.updateComplete;

    expectSameDay(detail!.bs, convertAdToBs(2026, 9, 20));
  });

  it('opens the dialog in Gregorian and confirms a Bikram Sambat value', async () => {
    const el = document.createElement('nepali-date-picker-dialog') as NepaliDatePickerDialog;
    el.setAttribute('value', '2083-06-01');
    el.setAttribute('calendar-system', 'ad');
    el.setAttribute('show-calendar-toggle', '');
    document.body.appendChild(el);
    await el.updateComplete;
    el.open = true;
    await el.updateComplete;

    expect(segments(el)).toHaveLength(2);
    expect(dayButtons(el)).toHaveLength(getTotalDaysInAdMonth(2026, 9));

    let detail: NepaliDatePickerChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDatePickerChangeDetail>).detail;
    });
    dayByText(el, '20').click();
    await el.updateComplete;
    const [, ok] = el.renderRoot.querySelectorAll<HTMLButtonElement>('.actions button');
    ok!.click();
    await el.updateComplete;

    const expected = convertAdToBs(2026, 9, 20);
    expectSameDay(detail!.bs, expected);
    expect(el.value).toBe(
      `${expected.year}-${String(expected.month).padStart(2, '0')}-${String(expected.dayOfMonth).padStart(2, '0')}`,
    );
  });

  it('types a Gregorian range in the range field and reports Bikram Sambat', async () => {
    const el = document.createElement('nepali-date-range-field') as NepaliDateRangeField;
    el.setAttribute('calendar-system', 'ad');
    document.body.appendChild(el);
    await el.updateComplete;

    let detail: NepaliDateRangeChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDateRangeChangeDetail>).detail;
    });

    const [startField, endField] = el.renderRoot.querySelectorAll<NepaliDateField>('nepali-date-field');
    await typeInto(el, startField!, '2026/09/17');
    await typeInto(el, endField!, '2026/09/20');

    expect(detail!.startBsIso).toBe('2083-06-01');
    expectSameDay(detail!.end!, convertAdToBs(2026, 9, 20));
    expect(el.start).toBe('2083-06-01');
  });

  it('writes the docked field in Gregorian when that is the calendar shown', async () => {
    const el = document.createElement('nepali-date-picker-docked') as NepaliDatePickerDocked;
    el.setAttribute('value', '2083-06-01');
    el.setAttribute('calendar-system', 'ad');
    document.body.appendChild(el);
    await el.updateComplete;

    expect(el.renderRoot.querySelector<HTMLInputElement>('input')!.value).toBe('2026-09-17');
  });

  it('rewrites the docked field when the calendar changes after mount', async () => {
    const el = document.createElement('nepali-date-picker-docked') as NepaliDatePickerDocked;
    el.setAttribute('value', '2083-06-01');
    document.body.appendChild(el);
    await el.updateComplete;
    const input = el.renderRoot.querySelector<HTMLInputElement>('input')!;
    expect(input.value).toBe('2083-06-01');

    el.calendarSystem = 'ad';
    await el.updateComplete;
    expect(el.renderRoot.querySelector<HTMLInputElement>('input')!.value).toBe('2026-09-17');

    el.calendarSystem = 'bs';
    await el.updateComplete;
    expect(el.renderRoot.querySelector<HTMLInputElement>('input')!.value).toBe('2083-06-01');
    // Only the display moved; the value never left Bikram Sambat.
    expect(el.value).toBe('2083-06-01');
  });

  it('reads a date typed into the docked field in the calendar it displays', async () => {
    const el = document.createElement('nepali-date-picker-docked') as NepaliDatePickerDocked;
    el.setAttribute('calendar-system', 'ad');
    document.body.appendChild(el);
    await el.updateComplete;

    let detail: NepaliDatePickerChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDatePickerChangeDetail>).detail;
    });

    const input = el.renderRoot.querySelector<HTMLInputElement>('input')!;
    input.value = '2026-09-17';
    input.dispatchEvent(new Event('change', { bubbles: true }));
    await el.updateComplete;

    expect(el.value).toBe('2083-06-01');
    expectSameDay(detail!.bs, convertAdToBs(2026, 9, 17));
  });

  it('refuses a docked entry that is a real date in neither direction', async () => {
    const el = document.createElement('nepali-date-picker-docked') as NepaliDatePickerDocked;
    el.setAttribute('value', '2083-06-01');
    el.setAttribute('calendar-system', 'ad');
    document.body.appendChild(el);
    await el.updateComplete;

    const input = el.renderRoot.querySelector<HTMLInputElement>('input')!;
    // AD 1913-04-12 is one day before the conversion anchor, so it has no Bikram Sambat equivalent.
    input.value = '1913-04-12';
    input.dispatchEvent(new Event('change', { bubbles: true }));
    await el.updateComplete;

    expect(el.value).toBe('2083-06-01');
  });

  it('rewrites the field when the calendar changes after mount', async () => {
    const el = document.createElement('nepali-date-field') as NepaliDateField;
    el.setAttribute('value', '2083-06-01');
    document.body.appendChild(el);
    await el.updateComplete;
    expect(el.renderRoot.querySelector<HTMLInputElement>('input')!.value).toBe('2083-06-01');

    el.calendarSystem = 'ad';
    await el.updateComplete;
    expect(el.renderRoot.querySelector<HTMLInputElement>('input')!.value).toBe('2026-09-17');
    expect(el.value).toBe('2083-06-01');
  });

  it('clears a stale field error when the calendar changes', async () => {
    const el = document.createElement('nepali-date-field') as NepaliDateField;
    document.body.appendChild(el);
    await el.updateComplete;

    const input = el.renderRoot.querySelector<HTMLInputElement>('input')!;
    // Baisakh 2083 has no 32nd day, so this is rejected in Bikram Sambat.
    input.value = '2083/01/32';
    input.dispatchEvent(new Event('change', { bubbles: true }));
    await el.updateComplete;
    expect(el.renderRoot.querySelector('.error')?.textContent?.trim()).not.toBe('');

    el.calendarSystem = 'ad';
    await el.updateComplete;

    // The message described what was typed in the old calendar, so it must not outlive the switch.
    expect(el.renderRoot.querySelector('.error')?.textContent?.trim()).toBe('');
  });

  it('rewrites both range fields when the calendar changes after mount', async () => {
    const el = document.createElement('nepali-date-range-field') as NepaliDateRangeField;
    el.setAttribute('start', '2083-06-01');
    el.setAttribute('end', '2083-06-10');
    document.body.appendChild(el);
    await el.updateComplete;

    el.calendarSystem = 'ad';
    await el.updateComplete;

    const [startField, endField] = el.renderRoot.querySelectorAll<NepaliDateField>('nepali-date-field');
    await startField!.updateComplete;
    await endField!.updateComplete;
    expect(startField!.renderRoot.querySelector<HTMLInputElement>('input')!.value).toBe('2026-09-17');
    expect(endField!.renderRoot.querySelector<HTMLInputElement>('input')!.value).toBe('2026-09-26');
    expect(el.start).toBe('2083-06-01');
  });

  it('writes an existing Bikram Sambat range out in Gregorian in the range field', async () => {
    const el = document.createElement('nepali-date-range-field') as NepaliDateRangeField;
    el.setAttribute('start', '2083-06-01');
    el.setAttribute('end', '2083-06-10');
    el.setAttribute('calendar-system', 'ad');
    document.body.appendChild(el);
    await el.updateComplete;

    const [startField, endField] = el.renderRoot.querySelectorAll<NepaliDateField>('nepali-date-field');
    await startField!.updateComplete;
    await endField!.updateComplete;
    expect(startField!.renderRoot.querySelector<HTMLInputElement>('input')!.value).toBe('2026-09-17');
    expect(endField!.renderRoot.querySelector<HTMLInputElement>('input')!.value).toBe('2026-09-26');
  });
});

describe('calendar switch motion', () => {
  /** jsdom ships no Web Animations API, so the animation is observed by standing one in. */
  function captureAnimations(): { calls: Keyframe[][]; restore: () => void } {
    const calls: Keyframe[][] = [];
    const proto = Element.prototype as unknown as { animate?: unknown };
    const original = proto.animate;
    proto.animate = function stub(keyframes: Keyframe[]): { finished: Promise<void> } {
      calls.push(keyframes);
      return { finished: Promise.resolve() };
    };
    return {
      calls,
      restore: () => {
        if (original === undefined) delete proto.animate;
        else proto.animate = original;
      },
    };
  }

  it('fades the header and the grid in when the calendar switches', async () => {
    const el = await mount({ value: '2083-06-01', 'show-calendar-toggle': '' });
    const animations = captureAnimations();
    try {
      const [, ad] = segments(el);
      ad.click();
      await el.updateComplete;

      // One for the month header, one for the day grid.
      expect(animations.calls).toHaveLength(2);
      expect(animations.calls[0]![0]).toMatchObject({ opacity: '0' });
      expect(animations.calls[0]![1]).toMatchObject({ opacity: '1', transform: 'scale(1)' });
    } finally {
      animations.restore();
    }
  });

  it('does not fade when only the month changes', async () => {
    const el = await mount({ value: '2083-06-01', 'show-calendar-toggle': '' });
    const animations = captureAnimations();
    try {
      el.renderRoot.querySelectorAll<HTMLButtonElement>('button.nav')[1]!.click();
      await el.updateComplete;

      expect(animations.calls).toHaveLength(0);
    } finally {
      animations.restore();
    }
  });

  it('stays still when the user asks for reduced motion', async () => {
    const el = await mount({ value: '2083-06-01', 'show-calendar-toggle': '' });
    const animations = captureAnimations();
    const originalMatchMedia = window.matchMedia;
    window.matchMedia = ((query: string) =>
      ({ matches: query.includes('prefers-reduced-motion'), media: query })) as typeof window.matchMedia;
    try {
      const [, ad] = segments(el);
      ad.click();
      await el.updateComplete;

      expect(animations.calls).toHaveLength(0);
      // The switch itself still happened; only the motion was skipped.
      expect(segments(el)[1]!.getAttribute('aria-checked')).toBe('true');
    } finally {
      window.matchMedia = originalMatchMedia;
      animations.restore();
    }
  });
});
