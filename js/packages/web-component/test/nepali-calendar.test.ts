import { describe, it, expect, afterEach } from 'vitest';
import { getBsCalendar } from '@nepali-date-picker/core';
import '../src/index.js';
import type { NepaliCalendar, NepaliDaySelectEvent, NepaliEventSelectEvent } from '../src/index.js';

/** A month the tests page to, so the marked days are always on screen. */
const MONTH = { year: 2083, month: 6 };

/** The first day of [MONTH] falling on [dayOfWeek], 1 for Sunday. */
function firstDayOn(dayOfWeek: number): number {
  for (let day = 1; day <= 28; day += 1) {
    if (getBsCalendar(MONTH.year, MONTH.month, day).dayOfWeek === dayOfWeek) return day;
  }
  throw new Error(`no weekday ${dayOfWeek} in 2083-06`);
}

const SATURDAY = firstDayOn(7);
const WORKDAY = firstDayOn(2);

function iso(day: number): string {
  return `${MONTH.year}-0${MONTH.month}-${String(day).padStart(2, '0')}`;
}

async function mount(attrs: Record<string, string> = {}): Promise<NepaliCalendar> {
  const el = document.createElement('nepali-calendar') as NepaliCalendar;
  el.setAttribute('value', iso(WORKDAY));
  for (const [key, value] of Object.entries(attrs)) el.setAttribute(key, value);
  document.body.appendChild(el);
  await el.updateComplete;
  return el;
}

function dayCell(el: NepaliCalendar, day: number): HTMLButtonElement {
  const cells = Array.from(el.shadowRoot?.querySelectorAll<HTMLButtonElement>('button.day') ?? []);
  const match = cells.find((cell) => cell.getAttribute('aria-label')?.includes(` ${day}, ${MONTH.year}`));
  if (!match) throw new Error(`no cell for day ${day}`);
  return match;
}

function lines(el: NepaliCalendar): HTMLElement[] {
  return Array.from(el.shadowRoot?.querySelectorAll<HTMLElement>('.lists .line') ?? []);
}

function text(el: NepaliCalendar, selector: string): string {
  return el.shadowRoot?.querySelector(selector)?.textContent?.trim() ?? '';
}

const FESTIVAL = JSON.stringify([
  {
    date: iso(10),
    name: 'Indra Jatra',
    kind: 'religious',
    id: 'indra-jatra',
    days: 3,
    payload: '{"bannerUrl":"https://example.org/jatra.jpg"}',
  },
  { date: iso(14), name: 'Staff meeting', kind: 'observance', id: 'staff-meeting' },
]);

afterEach(() => {
  document.body.innerHTML = '';
});

describe('nepali-calendar', () => {
  it('reads like a patro out of the box', async () => {
    const el = await mount();

    // Both calendars' numbers and the neighbouring months' days, which a picker leaves off.
    expect(el.showSecondaryDate).toBe(true);
    expect(el.showAdjacentMonthDays).toBe(true);
    expect(el.shadowRoot?.querySelector('.day .secondary')).not.toBeNull();
  });

  it('reports the day that was clicked, with what is on it', async () => {
    const el = await mount({ events: FESTIVAL, 'weekly-off-days': '7' });
    let detail: NepaliDaySelectEvent['detail'] | null = null;
    el.addEventListener('day-select', (event) => {
      detail = (event as NepaliDaySelectEvent).detail;
    });

    dayCell(el, 10).click();
    await el.updateComplete;

    expect(detail).not.toBeNull();
    expect(detail!.bsIso).toBe(iso(10));
    expect(detail!.events.map((entry) => entry.name)).toEqual(['Indra Jatra']);
    expect(detail!.isNonWorking).toBe(true);
    // The payload crosses untouched, which is what an app reads its own record out of.
    expect(detail!.events[0]?.payload).toContain('bannerUrl');
  });

  it('calls a weekly off day closed even with nothing named on it', async () => {
    const el = await mount({ 'weekly-off-days': '7' });
    let detail: NepaliDaySelectEvent['detail'] | null = null;
    el.addEventListener('day-select', (event) => {
      detail = (event as NepaliDaySelectEvent).detail;
    });

    dayCell(el, SATURDAY).click();
    await el.updateComplete;

    expect(detail!.isWeeklyOff).toBe(true);
    expect(detail!.isNonWorking).toBe(true);
    expect(detail!.events).toEqual([]);
  });

  it('writes the picked day out when asked', async () => {
    const el = await mount({
      events: FESTIVAL,
      'weekly-off-days': '7',
      'show-day-summary': '',
      value: iso(10),
    });

    expect(text(el, '.verdict')).toBe('Closed');
    expect(lines(el).some((line) => line.textContent?.includes('Indra Jatra'))).toBe(true);
  });

  it('says so for a day carrying nothing', async () => {
    const el = await mount({ 'show-day-summary': '', value: iso(WORKDAY) });

    expect(text(el, '.verdict')).toBe('Working day');
    expect(text(el, '.empty')).toBe('Nothing on this day');
  });

  it('gathers a span into one line of the month list', async () => {
    const el = await mount({ events: FESTIVAL, 'show-month-events': '' });

    const rows = lines(el);
    expect(rows).toHaveLength(2);
    // Three days of one festival, written once with the days it covers.
    expect(rows[0]?.textContent).toContain('Indra Jatra');
    expect(rows[0]?.textContent).toContain('10-12');
    expect(rows[1]?.textContent).toContain('Staff meeting');
  });

  it('hands the entry back when a line is clicked', async () => {
    const el = await mount({ events: FESTIVAL, 'show-month-events': '' });
    let detail: NepaliEventSelectEvent['detail'] | null = null;
    el.addEventListener('event-select', (event) => {
      detail = (event as NepaliEventSelectEvent).detail;
    });

    lines(el)[0]?.click();
    await el.updateComplete;

    expect(detail).not.toBeNull();
    expect(detail!.event.id).toBe('indra-jatra');
    expect(detail!.event.payload).toContain('https://example.org/jatra.jpg');
    expect(detail!.firstBsIso).toBe(iso(10));
    expect(detail!.lastBsIso).toBe(iso(12));
  });

  it('says a month with nothing named is empty', async () => {
    const el = await mount({ 'show-month-events': '' });

    expect(text(el, '.empty')).toBe('Nothing this month');
  });

  it('keeps the picked day while the grid turns Gregorian', async () => {
    const el = await mount({ value: iso(WORKDAY), 'calendar-system': 'ad' });

    expect(el.value).toBe(iso(WORKDAY));
    expect(el.shadowRoot?.querySelector('button.day')).not.toBeNull();
  });
});
