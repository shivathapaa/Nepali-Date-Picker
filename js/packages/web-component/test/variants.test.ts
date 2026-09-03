import { describe, it, expect, afterEach } from 'vitest';
import { getBsMonthName, getTotalDaysInBsMonth } from '@nepali-date-picker/core';
import '../src/index.js';
import type {
  NepaliDateField,
  NepaliDatePickerDialog,
  NepaliDatePickerDocked,
  NepaliDateRangeField,
  NepaliDateRangePicker,
  NepaliWheelDatePicker,
} from '../src/index.js';
import type { NepaliDatePickerChangeDetail, NepaliDateRangeChangeDetail } from '../src/types.js';

async function mount<T extends HTMLElement>(tag: string, attrs: Record<string, string> = {}): Promise<T> {
  const el = document.createElement(tag) as T;
  for (const [key, value] of Object.entries(attrs)) el.setAttribute(key, value);
  document.body.appendChild(el);
  await (el as unknown as { updateComplete: Promise<unknown> }).updateComplete;
  return el;
}

function dayByText(root: ParentNode, text: string): HTMLButtonElement {
  const found = [...root.querySelectorAll<HTMLButtonElement>('button.day')].find((b) => b.textContent?.trim() === text);
  if (!found) throw new Error(`no day cell "${text}"`);
  return found;
}

afterEach(() => {
  document.body.innerHTML = '';
});

describe('<nepali-date-range-picker>', () => {
  it('selects a start then an end and shades the range', async () => {
    const el = await mount<NepaliDateRangePicker>('nepali-date-range-picker');
    let detail: NepaliDateRangeChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDateRangeChangeDetail>).detail;
    });

    dayByText(el.renderRoot, '10').click();
    await el.updateComplete;
    dayByText(el.renderRoot, '15').click();
    await el.updateComplete;

    expect(detail!.start?.dayOfMonth).toBe(10);
    expect(detail!.end?.dayOfMonth).toBe(15);
    expect(el.renderRoot.querySelector('.day.range-start')?.textContent?.trim()).toBe('10');
    expect(el.renderRoot.querySelector('.day.range-end')?.textContent?.trim()).toBe('15');
    expect(el.renderRoot.querySelectorAll('.day.in-range').length).toBeGreaterThan(0);
  });

  it('swaps when the second pick is earlier than the first', async () => {
    const el = await mount<NepaliDateRangePicker>('nepali-date-range-picker');
    dayByText(el.renderRoot, '20').click();
    await el.updateComplete;
    let detail: NepaliDateRangeChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDateRangeChangeDetail>).detail;
    });
    dayByText(el.renderRoot, '5').click();
    await el.updateComplete;
    expect(detail!.start?.dayOfMonth).toBe(5);
    expect(detail!.end?.dayOfMonth).toBe(20);
  });
});

describe('<nepali-date-picker-dialog>', () => {
  it('renders nothing until opened', async () => {
    const el = await mount<NepaliDatePickerDialog>('nepali-date-picker-dialog', { value: '2081-01-15' });
    expect(el.renderRoot.querySelector('.backdrop')).toBeNull();
    el.open = true;
    await el.updateComplete;
    expect(el.renderRoot.querySelector('.backdrop')).not.toBeNull();
  });

  it('confirms a selection with OK and closes', async () => {
    const el = await mount<NepaliDatePickerDialog>('nepali-date-picker-dialog', { value: '2081-01-15' });
    el.open = true;
    await el.updateComplete;

    let detail: NepaliDatePickerChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDatePickerChangeDetail>).detail;
    });

    dayByText(el.renderRoot, '20').click();
    await el.updateComplete;
    const [, ok] = el.renderRoot.querySelectorAll<HTMLButtonElement>('.actions button');
    ok!.click();
    await el.updateComplete;

    expect(detail!.bsIso).toBe('2081-01-20');
    expect(el.open).toBe(false);
  });

  it('cancel reverts and fires cancel', async () => {
    const el = await mount<NepaliDatePickerDialog>('nepali-date-picker-dialog', { value: '2081-01-15' });
    el.open = true;
    await el.updateComplete;
    let cancelled = false;
    let changed = false;
    el.addEventListener('cancel', () => {
      cancelled = true;
    });
    el.addEventListener('change', () => {
      changed = true;
    });
    dayByText(el.renderRoot, '20').click();
    await el.updateComplete;
    const [cancel] = el.renderRoot.querySelectorAll<HTMLButtonElement>('.actions button');
    cancel!.click();
    await el.updateComplete;
    expect(cancelled).toBe(true);
    expect(changed).toBe(false);
    expect(el.open).toBe(false);
    expect(el.value).toBe('2081-01-15');
  });
});

describe('<nepali-date-picker-docked>', () => {
  it('opens a popover and selects a day', async () => {
    const el = await mount<NepaliDatePickerDocked>('nepali-date-picker-docked', { value: '2081-01-15' });
    expect(el.renderRoot.querySelector('.popover')).toBeNull();
    el.renderRoot.querySelector<HTMLButtonElement>('.icon-button')!.click();
    await el.updateComplete;
    expect(el.renderRoot.querySelector('.popover')).not.toBeNull();

    let detail: NepaliDatePickerChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDatePickerChangeDetail>).detail;
    });
    dayByText(el.renderRoot, '8').click();
    await el.updateComplete;
    expect(detail!.bsIso).toBe('2081-01-08');
    expect(el.renderRoot.querySelector('.popover')).toBeNull();
  });

  it('commits a typed date on change', async () => {
    const el = await mount<NepaliDatePickerDocked>('nepali-date-picker-docked');
    let detail: NepaliDatePickerChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDatePickerChangeDetail>).detail;
    });
    const input = el.renderRoot.querySelector<HTMLInputElement>('input')!;
    input.value = '2081-03-09';
    input.dispatchEvent(new Event('change', { bubbles: true }));
    await el.updateComplete;
    expect(detail!.bsIso).toBe('2081-03-09');
  });
});

describe('<nepali-date-field>', () => {
  it('emits change for a valid typed date', async () => {
    const el = await mount<NepaliDateField>('nepali-date-field');
    let detail: NepaliDatePickerChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDatePickerChangeDetail>).detail;
    });
    const input = el.renderRoot.querySelector<HTMLInputElement>('input')!;
    input.value = '2081/05/24';
    input.dispatchEvent(new Event('change', { bubbles: true }));
    await el.updateComplete;
    expect(detail!.bs).toEqual({ year: 2081, month: 5, dayOfMonth: 24 });
  });

  it('shows an error and fires invalid for a bad day', async () => {
    const el = await mount<NepaliDateField>('nepali-date-field');
    let invalidMsg = '';
    let changed = false;
    el.addEventListener('invalid', (e) => {
      invalidMsg = (e as CustomEvent<{ message: string }>).detail.message;
    });
    el.addEventListener('change', () => {
      changed = true;
    });
    const input = el.renderRoot.querySelector<HTMLInputElement>('input')!;
    input.value = '2081/01/40';
    input.dispatchEvent(new Event('change', { bubbles: true }));
    await el.updateComplete;
    expect(changed).toBe(false);
    expect(invalidMsg).not.toBe('');
    expect(el.renderRoot.querySelector('.error')?.textContent?.trim()).not.toBe('');
  });

  it('accepts Devanagari digits', async () => {
    const el = await mount<NepaliDateField>('nepali-date-field', { language: 'ne' });
    let detail: NepaliDatePickerChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDatePickerChangeDetail>).detail;
    });
    const input = el.renderRoot.querySelector<HTMLInputElement>('input')!;
    input.value = '२०८१/०५/२४';
    input.dispatchEvent(new Event('change', { bubbles: true }));
    await el.updateComplete;
    expect(detail!.bsIso).toBe('2081-05-24');
  });
});

describe('<nepali-date-range-field>', () => {
  it('emits a range and rejects an end before start', async () => {
    const el = await mount<NepaliDateRangeField>('nepali-date-range-field');
    await el.updateComplete;
    const [startField, endField] = el.renderRoot.querySelectorAll<NepaliDateField>('nepali-date-field');
    let detail: NepaliDateRangeChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDateRangeChangeDetail>).detail;
    });

    const setField = async (field: NepaliDateField, text: string) => {
      await field.updateComplete;
      const input = field.renderRoot.querySelector<HTMLInputElement>('input')!;
      input.value = text;
      input.dispatchEvent(new Event('change', { bubbles: true }));
      await el.updateComplete;
    };

    await setField(startField!, '2081-01-10');
    await setField(endField!, '2081-01-20');
    expect(detail!.startBsIso).toBe('2081-01-10');
    expect(detail!.endBsIso).toBe('2081-01-20');

    // Moving the start past the committed end triggers the cross-field ordering error.
    await setField(startField!, '2081-01-25');
    expect(el.renderRoot.querySelector('.error')?.textContent?.trim()).not.toBe('');
  });
});

describe('<nepali-wheel-date-picker>', () => {
  it('selects via the year column and clamps the day to the month length', async () => {
    // Start on a 31-day month at day 31, then move to a shorter month.
    const el = await mount<NepaliWheelDatePicker>('nepali-wheel-date-picker', { value: '2081-01-31' });
    await el.updateComplete;

    let shortMonth = 0;
    for (let m = 2; m <= 12; m += 1) {
      if (getTotalDaysInBsMonth(2081, m) < 31) {
        shortMonth = m;
        break;
      }
    }
    expect(shortMonth).toBeGreaterThan(0);

    let detail: NepaliDatePickerChangeDetail | undefined;
    el.addEventListener('change', (e) => {
      detail = (e as CustomEvent<NepaliDatePickerChangeDetail>).detail;
    });

    const monthCol = el.renderRoot.querySelector<HTMLElement>('.column[aria-label="Month"]')!;
    const monthName = getBsMonthName(shortMonth, 'full', 'en');
    const option = [...monthCol.querySelectorAll<HTMLButtonElement>('.option')].find((o) => o.textContent?.trim() === monthName)!;
    option.click();
    await el.updateComplete;

    const expectedDay = getTotalDaysInBsMonth(2081, shortMonth);
    expect(detail!.bs.month).toBe(shortMonth);
    expect(detail!.bs.dayOfMonth).toBe(expectedDay);
  });

  it('renders a day column sized to the selected month', async () => {
    const el = await mount<NepaliWheelDatePicker>('nepali-wheel-date-picker', { value: '2081-05-15' });
    await el.updateComplete;
    const dayCol = el.renderRoot.querySelector<HTMLElement>('.column[aria-label="Day"]')!;
    expect(dayCol.querySelectorAll('.option').length).toBe(getTotalDaysInBsMonth(2081, 5));
  });
});
