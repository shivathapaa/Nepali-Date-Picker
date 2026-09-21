import { describe, it, expect, afterEach } from 'vitest';
import { getBsCalendar, getTotalDaysInBsMonth } from '@nepali-date-picker/core';
import { parseEvents } from '../src/utils.js';
import '../src/index.js';
import type { NepaliDatePicker } from '../src/index.js';

/** A month the tests page to, so the marked days are always on screen. */
const MONTH = { year: 2083, month: 6 };

function iso(day: number, month = MONTH.month, year = MONTH.year): string {
  return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
}

/** The first day of [MONTH] falling on [dayOfWeek], 1 for Sunday. */
function firstDayOn(dayOfWeek: number): number {
  for (let day = 1; day <= 28; day += 1) {
    if (getBsCalendar(MONTH.year, MONTH.month, day).dayOfWeek === dayOfWeek) return day;
  }
  throw new Error(`no weekday ${dayOfWeek} in 2083-06`);
}

async function mount(attrs: Record<string, string> = {}): Promise<NepaliDatePicker> {
  const el = document.createElement('nepali-date-picker') as NepaliDatePicker;
  el.setAttribute('value', iso(10));
  for (const [key, value] of Object.entries(attrs)) el.setAttribute(key, value);
  document.body.appendChild(el);
  await el.updateComplete;
  return el;
}

function dayCell(el: NepaliDatePicker, day: number): HTMLButtonElement {
  const cells = Array.from(
    el.shadowRoot?.querySelectorAll<HTMLButtonElement>('button.day') ?? [],
  );
  const match = cells.find((cell) => cell.getAttribute('aria-label')?.includes(` ${day}, ${MONTH.year}`));
  if (!match) throw new Error(`no cell for day ${day}`);
  return match;
}

afterEach(() => {
  document.body.innerHTML = '';
});

describe('parsing a span', () => {
  it('expands a day count into one entry per day', () => {
    const span = parseEvents([{ date: iso(10), name: 'Dashain', kind: 'religious', days: 4 }]);

    expect(span.map((e) => e.date)).toEqual([iso(10), iso(11), iso(12), iso(13)]);
    expect(span.every((e) => e.name === 'Dashain' && e.kind === 'religious')).toBe(true);
  });

  it('includes both ends of an end date', () => {
    const span = parseEvents([{ date: iso(10), name: 'Leave', endDate: iso(14) }]);

    expect(span).toHaveLength(5);
    expect(span[span.length - 1]?.date).toBe(iso(14));
  });

  it('lets the end date win over a day count', () => {
    const span = parseEvents([{ date: iso(10), name: 'Leave', endDate: iso(12), days: 30 }]);

    expect(span).toHaveLength(3);
  });

  it('carries the display fields and drops the span fields', () => {
    const [first] = parseEvents([
      { date: iso(10), name: 'Trip', kind: 'observance', closesOffices: true, color: '#ff7043', indicate: true, days: 2 },
    ]);

    expect(first).toEqual({
      date: iso(10),
      name: 'Trip',
      kind: 'observance',
      closesOffices: true,
      color: '#ff7043',
      indicate: true,
    });
  });

  it('marks the one day when the span says nothing usable', () => {
    expect(parseEvents([{ date: iso(10), endDate: iso(9) }])).toHaveLength(1);
    expect(parseEvents([{ date: iso(10), endDate: 'not a date' }])).toHaveLength(1);
    expect(parseEvents([{ date: iso(10), days: 0 }])).toHaveLength(1);
    expect(parseEvents([{ date: iso(10), days: -5 }])).toHaveLength(1);
    expect(parseEvents([{ date: iso(10), days: 2.5 }])).toHaveLength(1);
  });

  it('caps a span at a year, so a mistyped count cannot fill the grid', () => {
    expect(parseEvents([{ date: iso(1), days: 5000 }])).toHaveLength(366);
  });

  it('crosses a month and a year boundary', () => {
    const lastOfChaitra = getTotalDaysInBsMonth(2083, 12);
    const span = parseEvents([{ date: iso(lastOfChaitra, 12, 2083), name: 'Year end', days: 3 }]);

    expect(span.map((e) => e.date)).toEqual([
      iso(lastOfChaitra, 12, 2083),
      iso(1, 1, 2084),
      iso(2, 1, 2084),
    ]);
  });

  it('stops at the end of the supported range instead of throwing', () => {
    const span = parseEvents([{ date: '2100-12-28', name: 'Past the table', days: 20 }]);

    expect(span.length).toBeGreaterThan(0);
    expect(span.length).toBeLessThan(20);
  });

  it('expands a span given as a JSON attribute string', () => {
    const span = parseEvents(JSON.stringify([{ date: iso(10), name: 'Dashain', days: 3 }]));

    expect(span).toHaveLength(3);
  });
});

describe('drawing a span', () => {
  it('colours every day it covers', async () => {
    const el = await mount({
      events: JSON.stringify([
        { date: iso(10), name: 'Dashain', kind: 'religious', days: 3 },
      ]),
    });

    for (const day of [10, 11, 12]) {
      expect(dayCell(el, day).classList.contains('kind-religious')).toBe(true);
    }
    expect(dayCell(el, 13).classList.contains('marked')).toBe(false);
  });

  it('announces the name on every day of the span', async () => {
    const el = await mount({
      events: JSON.stringify([{ date: iso(10), name: 'Dashain', endDate: iso(12) }]),
    });

    expect(dayCell(el, 11).getAttribute('aria-label')).toContain('Dashain');
  });

  it('draws a dot on each day when the span asks for one', async () => {
    const el = await mount({
      events: JSON.stringify([
        { date: iso(10), name: 'Sprint', indicate: true, color: '#ff7043', days: 2 },
      ]),
    });

    for (const day of [10, 11]) {
      const dots = dayCell(el, day).querySelectorAll('.event-dot');
      expect(dots).toHaveLength(1);
      expect((dots[0] as HTMLElement).style.background).toBe('rgb(255, 112, 67)');
    }
  });

  it('keeps a weekly off day looking closed when an observance span crosses it', async () => {
    const saturday = firstDayOn(7);
    const el = await mount({
      'weekly-off-days': '7',
      events: JSON.stringify([
        { date: iso(saturday - 1), name: 'Sports week', kind: 'observance', days: 3 },
      ]),
    });

    expect(dayCell(el, saturday).classList.contains('weekly-off')).toBe(true);
    expect(dayCell(el, saturday - 1).classList.contains('kind-observance')).toBe(true);
  });

  it('lets a closing span outrank the week on the days it covers', async () => {
    const saturday = firstDayOn(7);
    const el = await mount({
      'weekly-off-days': '7',
      events: JSON.stringify([
        { date: iso(saturday - 1), name: 'Dashain', kind: 'religious', days: 3 },
      ]),
    });

    expect(dayCell(el, saturday).classList.contains('kind-religious')).toBe(true);
    expect(dayCell(el, saturday).classList.contains('weekly-off')).toBe(false);
  });

  it('takes a span set as a property rather than an attribute', async () => {
    const el = await mount();
    (el as unknown as { events: unknown }).events = [
      { date: iso(10), name: 'Dashain', kind: 'religious', days: 3 },
    ];
    await el.updateComplete;

    for (const day of [10, 11, 12]) {
      expect(dayCell(el, day).classList.contains('kind-religious')).toBe(true);
    }
  });

  it('draws two dots at most when each cell also carries its Gregorian day', async () => {
    const el = await mount({
      'show-secondary-date': '',
      events: JSON.stringify([
        { date: iso(10), name: 'A', indicate: true, days: 2 },
        { date: iso(10), name: 'B', indicate: true, days: 2 },
        { date: iso(10), name: 'C', indicate: true, days: 2 },
      ]),
    });

    expect(dayCell(el, 10).querySelectorAll('.event-dot')).toHaveLength(2);
    expect(dayCell(el, 11).querySelectorAll('.event-dot')).toHaveLength(2);
  });

  it('marks the days of a span that reaches into the viewed month', async () => {
    const previousMonthLastDay = getTotalDaysInBsMonth(MONTH.year, MONTH.month - 1);
    const el = await mount({
      events: JSON.stringify([
        {
          date: iso(previousMonthLastDay - 1, MONTH.month - 1),
          name: 'Tihar',
          kind: 'religious',
          days: 4,
        },
      ]),
    });

    expect(dayCell(el, 1).classList.contains('kind-religious')).toBe(true);
    expect(dayCell(el, 2).classList.contains('kind-religious')).toBe(true);
    expect(dayCell(el, 3).classList.contains('marked')).toBe(false);
  });
});

describe('a span on the other calendar elements', () => {
  const span = JSON.stringify([
    { date: iso(10), name: 'Dashain', kind: 'religious', endDate: iso(13) },
  ]);

  async function mountTag(tag: string, attrs: Record<string, string>): Promise<HTMLElement> {
    const el = document.createElement(tag);
    for (const [key, value] of Object.entries(attrs)) el.setAttribute(key, value);
    document.body.appendChild(el);
    await (el as HTMLElement & { updateComplete: Promise<unknown> }).updateComplete;
    return el as HTMLElement;
  }

  function markedDays(el: HTMLElement): number {
    return el.shadowRoot?.querySelectorAll('button.day.kind-religious').length ?? 0;
  }

  it('marks the docked calendar once it is open', async () => {
    const el = await mountTag('nepali-date-picker-docked', { value: iso(10), events: span });

    el.shadowRoot?.querySelector<HTMLButtonElement>('button')?.click();
    await (el as HTMLElement & { updateComplete: Promise<unknown> }).updateComplete;

    expect(markedDays(el)).toBe(4);
  });

  it('marks every month the range picker draws', async () => {
    const el = await mountTag('nepali-date-range-picker', { start: iso(10), events: span });

    expect(markedDays(el)).toBeGreaterThanOrEqual(4);
  });

  it('marks the dialog it opens', async () => {
    const el = await mountTag('nepali-date-picker-dialog', { value: iso(10), events: span });

    (el as HTMLElement & { show: () => void }).show();
    await (el as HTMLElement & { updateComplete: Promise<unknown> }).updateComplete;

    expect(markedDays(el)).toBe(4);
  });
});
