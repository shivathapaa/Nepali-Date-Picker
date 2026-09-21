import { describe, it, expect, afterEach } from 'vitest';
import { getBsCalendar } from '@nepali-date-picker/core';
import '../src/index.js';
import type { NepaliDatePicker } from '../src/index.js';

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
const SUNDAY = firstDayOn(1);
const WORKDAY = firstDayOn(2);

function iso(day: number): string {
  return `${MONTH.year}-0${MONTH.month}-${String(day).padStart(2, '0')}`;
}

async function mount(attrs: Record<string, string> = {}): Promise<NepaliDatePicker> {
  const el = document.createElement('nepali-date-picker') as NepaliDatePicker;
  el.setAttribute('value', iso(WORKDAY));
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

describe('weekly off days', () => {
  it('colours the weekdays the institution never opens', async () => {
    const el = await mount({ 'weekly-off-days': '7' });

    expect(dayCell(el, SATURDAY).classList.contains('weekly-off')).toBe(true);
    expect(dayCell(el, WORKDAY).classList.contains('marked')).toBe(false);
  });

  it('takes a two-day week', async () => {
    const el = await mount({ 'weekly-off-days': '7,1' });

    expect(dayCell(el, SATURDAY).classList.contains('weekly-off')).toBe(true);
    expect(dayCell(el, SUNDAY).classList.contains('weekly-off')).toBe(true);
  });

  it('ignores a week written to the JavaScript convention', async () => {
    // JavaScript numbers Sunday 0; this calendar numbers it 1, and closing nothing is safer than
    // closing the wrong day.
    const el = await mount({ 'weekly-off-days': '0,6' });

    expect(dayCell(el, SUNDAY).classList.contains('weekly-off')).toBe(false);
    expect(dayCell(el, firstDayOn(6)).classList.contains('weekly-off')).toBe(true);
  });

  it('draws no dot for the week', async () => {
    const el = await mount({ 'weekly-off-days': '7' });

    expect(dayCell(el, SATURDAY).querySelector('.event-dots')).toBeNull();
  });
});

describe('events', () => {
  it('colours a day by the kind of what is on it', async () => {
    const el = await mount({
      events: JSON.stringify([
        { date: iso(WORKDAY), name: 'Constitution Day', kind: 'governmentPublic' },
      ]),
    });

    expect(dayCell(el, WORKDAY).classList.contains('kind-governmentPublic')).toBe(true);
  });

  it('lets a named closure outrank the week, and an open day not', async () => {
    const el = await mount({
      'weekly-off-days': '7',
      events: JSON.stringify([
        { date: iso(SATURDAY), name: 'Dashain', kind: 'religious' },
        { date: iso(SUNDAY), name: 'Annual programme', kind: 'observance' },
      ]),
    });

    expect(dayCell(el, SATURDAY).classList.contains('kind-religious')).toBe(true);
    expect(dayCell(el, SATURDAY).classList.contains('weekly-off')).toBe(false);
    // Sunday is not in this week's off days, so the programme is all it has.
    expect(dayCell(el, SUNDAY).classList.contains('kind-observance')).toBe(true);
  });

  it('keeps a weekly off day looking closed when only an observance lands on it', async () => {
    const el = await mount({
      'weekly-off-days': '7',
      events: JSON.stringify([{ date: iso(SATURDAY), name: 'World Health Day', kind: 'observance' }]),
    });

    expect(dayCell(el, SATURDAY).classList.contains('weekly-off')).toBe(true);
  });

  it('draws a dot only for the entries that ask for one', async () => {
    const el = await mount({
      events: JSON.stringify([
        { date: iso(WORKDAY), name: 'Sarkari bida', kind: 'governmentPublic' },
        { date: iso(WORKDAY), name: 'Standup', indicate: true },
        { date: iso(WORKDAY), name: 'Birthday', indicate: true, color: '#ff7043' },
      ]),
    });

    const dots = dayCell(el, WORKDAY).querySelectorAll('.event-dot');
    expect(dots).toHaveLength(2);
    expect((dots[1] as HTMLElement).style.background).toBe('rgb(255, 112, 67)');
  });

  it('caps the dots at three', async () => {
    const el = await mount({
      events: JSON.stringify(
        Array.from({ length: 5 }, (_, index) => ({
          date: iso(WORKDAY),
          name: `Event ${index}`,
          indicate: true,
        })),
      ),
    });

    expect(dayCell(el, WORKDAY).querySelectorAll('.event-dot')).toHaveLength(3);
  });

  it('announces every name after the date', async () => {
    const el = await mount({
      events: JSON.stringify([
        { date: iso(WORKDAY), name: 'Constitution Day', kind: 'governmentPublic' },
        { date: iso(WORKDAY), name: 'Standup', indicate: true },
      ]),
    });

    const label = dayCell(el, WORKDAY).getAttribute('aria-label') ?? '';
    expect(label).toContain('Constitution Day');
    expect(label).toContain('Standup');
  });

  it('survives malformed input without throwing', async () => {
    const el = await mount({ events: 'not json at all' });

    expect(dayCell(el, WORKDAY).classList.contains('marked')).toBe(false);

    const partial = await mount({
      events: JSON.stringify([{ name: 'No date here' }, { date: 'nonsense' }]),
    });
    expect(partial.shadowRoot?.querySelectorAll('.event-dot')).toHaveLength(0);
  });

  it('marks nothing when no events are given', async () => {
    const el = await mount();

    expect(el.shadowRoot?.querySelectorAll('.marked')).toHaveLength(0);
    expect(el.shadowRoot?.querySelectorAll('.event-dot')).toHaveLength(0);
  });

  it('updates when the events change', async () => {
    const el = await mount();
    expect(dayCell(el, WORKDAY).classList.contains('marked')).toBe(false);

    el.setAttribute(
      'events',
      JSON.stringify([{ date: iso(WORKDAY), name: 'Added later', kind: 'religious' }]),
    );
    await el.updateComplete;

    expect(dayCell(el, WORKDAY).classList.contains('kind-religious')).toBe(true);
  });
});
