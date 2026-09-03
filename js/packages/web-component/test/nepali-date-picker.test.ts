import { describe, it, expect, afterEach } from 'vitest';
import { convertBsToAd, getBsMonthName, getTodayBs, getTotalDaysInBsMonth } from '@nepali-date-picker/core';
import '../src/index.js';
import type { NepaliDatePicker } from '../src/index.js';
import type { NepaliDatePickerChangeDetail } from '../src/types.js';

async function mount(attrs: Record<string, string> = {}): Promise<NepaliDatePicker> {
  const el = document.createElement('nepali-date-picker');
  for (const [key, value] of Object.entries(attrs)) el.setAttribute(key, value);
  document.body.appendChild(el);
  await el.updateComplete;
  return el;
}

function dayButtons(el: NepaliDatePicker): HTMLButtonElement[] {
  return [...el.renderRoot.querySelectorAll<HTMLButtonElement>('button.day')];
}

function dayByText(el: NepaliDatePicker, text: string): HTMLButtonElement {
  const found = dayButtons(el).find((b) => b.textContent?.trim() === text);
  if (!found) throw new Error(`no day cell with text "${text}"`);
  return found;
}

afterEach(() => {
  document.body.innerHTML = '';
});

describe('<nepali-date-picker>', () => {
  it('registers the custom element', () => {
    expect(customElements.get('nepali-date-picker')).toBeTruthy();
  });

  it('renders one focusable day cell per day of the viewed month', async () => {
    const el = await mount({ value: '2081-01-15' });
    expect(dayButtons(el).length).toBe(getTotalDaysInBsMonth(2081, 1));
    const roving = el.renderRoot.querySelectorAll('button.day[tabindex="0"]');
    expect(roving.length).toBe(1);
  });

  it('marks the value as selected', async () => {
    const el = await mount({ value: '2081-01-15' });
    const selected = el.renderRoot.querySelector<HTMLButtonElement>('button.day.selected');
    expect(selected?.textContent?.trim()).toBe('15');
    expect(selected?.getAttribute('aria-selected')).toBe('true');
  });

  it('emits a change event with BS, AD, ISO, and formatted payload', async () => {
    const el = await mount({ value: '2081-01-15' });
    let detail: NepaliDatePickerChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDatePickerChangeDetail>).detail;
    });

    dayByText(el, '20').click();
    await el.updateComplete;

    expect(detail).toBeDefined();
    expect(detail!.bs).toEqual({ year: 2081, month: 1, dayOfMonth: 20 });
    expect(detail!.bsIso).toBe('2081-01-20');
    const ad = convertBsToAd(2081, 1, 20);
    expect(detail!.ad).toEqual({ year: ad.year, month: ad.month, dayOfMonth: ad.dayOfMonth });
    expect(detail!.adIso).toBe(`${ad.year}-${String(ad.month).padStart(2, '0')}-${String(ad.dayOfMonth).padStart(2, '0')}`);
    expect(detail!.formatted).toContain('2081');
  });

  it('localizes digits and names in Nepali', async () => {
    const el = await mount({ value: '2081-03-15', language: 'ne' });
    const selected = el.renderRoot.querySelector<HTMLButtonElement>('button.day.selected');
    expect(selected?.textContent?.trim()).toBe('१५');
    const label = el.renderRoot.querySelector('.label span');
    expect(label?.textContent?.trim()).toBe(getBsMonthName(3, 'full', 'ne'));
  });

  it('disables days outside the min/max range', async () => {
    const el = await mount({ value: '2081-01-15', min: '2081-01-10', max: '2081-01-20' });
    expect(dayByText(el, '5').getAttribute('aria-disabled')).toBe('true');
    expect(dayByText(el, '15').getAttribute('aria-disabled')).toBe('false');

    let fired = false;
    el.addEventListener('change', () => {
      fired = true;
    });
    dayByText(el, '5').click();
    await el.updateComplete;
    expect(fired).toBe(false);
  });

  it('moves roving focus with the arrow keys', async () => {
    const el = await mount({ value: '2081-01-15' });
    const grid = el.renderRoot.querySelector('[role="grid"]')!;
    grid.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowRight', bubbles: true }));
    await el.updateComplete;
    expect(el.renderRoot.querySelector('button.day[tabindex="0"]')?.textContent?.trim()).toBe('16');

    grid.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown', bubbles: true }));
    await el.updateComplete;
    expect(el.renderRoot.querySelector('button.day[tabindex="0"]')?.textContent?.trim()).toBe('23');
  });

  it('crosses the month boundary when arrowing past the last day', async () => {
    const lastDay = getTotalDaysInBsMonth(2081, 1);
    const el = await mount({ value: `2081-01-${lastDay}` });
    const grid = el.renderRoot.querySelector('[role="grid"]')!;
    grid.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowRight', bubbles: true }));
    await el.updateComplete;

    const label = el.renderRoot.querySelector('.label span');
    expect(label?.textContent?.trim()).toBe(getBsMonthName(2, 'full', 'en'));
    expect(el.renderRoot.querySelector('button.day[tabindex="0"]')?.textContent?.trim()).toBe('1');
  });

  it('changes the month with PageDown', async () => {
    const el = await mount({ value: '2081-01-15' });
    const grid = el.renderRoot.querySelector('[role="grid"]')!;
    grid.dispatchEvent(new KeyboardEvent('keydown', { key: 'PageDown', bubbles: true }));
    await el.updateComplete;
    expect(el.renderRoot.querySelector('.label span')?.textContent?.trim()).toBe(getBsMonthName(2, 'full', 'en'));
  });

  it('jumps to the current month with the Today button', async () => {
    const el = await mount({ value: '1970-01-01' });
    el.renderRoot.querySelector<HTMLButtonElement>('.footer button')!.click();
    await el.updateComplete;

    const today = getTodayBs();
    expect(el.renderRoot.querySelector('.label span')?.textContent?.trim()).toBe(
      getBsMonthName(today.month, 'full', 'en'),
    );
    expect(el.renderRoot.querySelector('button.day[tabindex="0"]')?.textContent?.trim()).toBe(
      String(today.dayOfMonth),
    );
  });
});
