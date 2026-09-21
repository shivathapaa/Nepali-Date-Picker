import '@nepali-date-picker/web-component';
import type { NepaliDatePickerDialog } from '@nepali-date-picker/web-component';
import {
  addDaysToBsDate,
  adDateTimeFromIso,
  adDateTimeToIso,
  bsDateTimeFromIso,
  bsDateTimeToIso,
  compareBsDates,
  convertAdToBs,
  convertBsToAd,
  createCalendarPolicy,
  createDetailedEvent,
  createEvent,
  expandEventDays,
  expandEventThrough,
  formatAdDate,
  formatAdDateByPattern,
  formatBsDate,
  formatBsDateByPattern,
  formatTimeEnglish,
  formatTimeNepali,
  getAdCalendar,
  getAdCalendarsInBsMonth,
  getAdDaysBetween,
  getAdMonth,
  getAdMonthName,
  getAdYearRange,
  getAdYearRangeForBsYears,
  getBsCalendar,
  getBsCalendarsInAdMonth,
  getBsDaysBetween,
  getBsMonth,
  getBsMonthName,
  getBsYearRange,
  getCurrentTime,
  getTodayAd,
  getTodayBs,
  getTotalDaysInAdMonth,
  getTotalDaysInBsMonth,
  getWeekdayName,
  isAdDateConvertible,
  localizeDigits,
  toLatinDigits,
} from '@nepali-date-picker/core';

type Lang = 'en' | 'ne';

const $ = <T extends Element>(selector: string): T => {
  const el = document.querySelector<T>(selector);
  if (!el) throw new Error(`missing element: ${selector}`);
  return el;
};

/* Global language, theme and palette controls apply to every picker on the page.
   An element that names its own `language` opts out by not carrying the `.sync` class. */
function applyLanguage(lang: Lang): void {
  for (const el of document.querySelectorAll('.sync')) el.setAttribute('language', lang);
}

$('#global-language').addEventListener('change', (e) => {
  applyLanguage((e.target as HTMLSelectElement).value as Lang);
});

$('#global-theme').addEventListener('change', (e) => {
  document.body.dataset.theme = (e.target as HTMLSelectElement).value;
});

$('#global-palette').addEventListener('change', (e) => {
  document.body.dataset.palette = (e.target as HTMLSelectElement).value;
});

/* Event log: every picker's change / cancel / invalid event bubbles here. */
const log = $('#log');
function record(name: string, detail: unknown, source: string): void {
  const entry = document.createElement('div');
  entry.className = 'log-entry';
  const body = detail === undefined ? '' : `<pre>${JSON.stringify(detail, null, 2)}</pre>`;
  entry.innerHTML = `<span class="name">${name}</span> from <code>${source}</code>${body}`;
  log.prepend(entry);
  while (log.children.length > 12) log.lastElementChild?.remove();
}

for (const type of ['change', 'cancel', 'invalid'] as const) {
  document.addEventListener(type, (e) => {
    const target = e.target as HTMLElement;
    if (!target.tagName?.toLowerCase().startsWith('nepali-')) return;
    record(type, (e as CustomEvent).detail, `<${target.tagName.toLowerCase()}>`);
  });
}

/* Dialog open buttons. */
$('#open-dialog').addEventListener('click', () => ($('#dialog-el') as NepaliDatePickerDialog).show());
$('#open-dialog-fs').addEventListener('click', () => ($('#dialog-fs') as NepaliDatePickerDialog).show());
$('#open-dialog-paired').addEventListener('click', () =>
  ($('#dialog-paired') as NepaliDatePickerDialog).show(),
);
$('#open-dialog-heading').addEventListener('click', () =>
  ($('#dialog-heading') as NepaliDatePickerDialog).show(),
);
$('#open-dialog-ne').addEventListener('click', () =>
  ($('#dialog-ne') as NepaliDatePickerDialog).show(),
);

/* The bounded field reports acceptance or the reason it refused, beside the field itself. */
const boundedStatus = $('#bounded-field-status');
$('#bounded-field').addEventListener('change', (e) => {
  boundedStatus.textContent = `accepted ${(e as CustomEvent).detail.bsIso}`;
});
$('#bounded-field').addEventListener('invalid', (e) => {
  boundedStatus.textContent = `rejected: ${(e as CustomEvent).detail.message}`;
});

/* Framework code tabs. */
for (const tab of document.querySelectorAll<HTMLButtonElement>('.tab')) {
  tab.addEventListener('click', () => {
    const name = tab.dataset.tab;
    for (const t of document.querySelectorAll<HTMLButtonElement>('.tab')) {
      t.setAttribute('aria-selected', String(t === tab));
    }
    for (const panel of document.querySelectorAll<HTMLElement>('.tabpanel')) {
      if (panel.dataset.tab === name) panel.setAttribute('data-active', '');
      else panel.removeAttribute('data-active');
    }
  });
}

/* Engine playground: two dates and a pattern in, every exported query out.
   Each group is a separate tab so the whole surface fits without a wall of text. */
const todayBs = getTodayBs();
const todayAd = getTodayAd();

const bsInput = $<HTMLInputElement>('#bs-input');
const adInput = $<HTMLInputElement>('#ad-input');
const patternInput = $<HTMLInputElement>('#pattern-input');

bsInput.value = iso(todayBs);
adInput.value = iso(todayAd);

for (const input of [bsInput, adInput, patternInput]) {
  input.addEventListener('input', renderEngine);
  input.addEventListener('change', renderEngine);
}

for (const tab of document.querySelectorAll<HTMLButtonElement>('#engine-tabs .tab')) {
  tab.addEventListener('click', () => {
    const name = tab.dataset.engine;
    for (const t of document.querySelectorAll<HTMLButtonElement>('#engine-tabs .tab')) {
      t.setAttribute('aria-selected', String(t === tab));
    }
    for (const panel of document.querySelectorAll<HTMLElement>('.tabpanel[data-engine]')) {
      if (panel.dataset.engine === name) panel.setAttribute('data-active', '');
      else panel.removeAttribute('data-active');
    }
  });
}

/** Writes `label: value` rows into one engine panel, replacing whatever was there. */
function fill(id: string, rows: Array<[string, string]>): void {
  const host = $(id);
  host.replaceChildren(
    ...rows.map(([label, value]) => {
      const row = document.createElement('div');
      row.innerHTML = `${label}: <span class="result"></span>`;
      row.querySelector('span')!.textContent = value;
      return row;
    }),
  );
}

/** A `YYYY-MM-DD` string as three numbers, or null when it is not one. */
function parts(value: string): [number, number, number] | null {
  const [y, m, d] = value.split('-').map(Number);
  return y && m && d ? [y, m, d] : null;
}

/** Runs a query that throws when its input is out of range, so one bad box does not blank a tab. */
function guarded(compute: () => string): string {
  try {
    return compute();
  } catch {
    return 'out of range';
  }
}

function renderEngine(): void {
  const bs = parts(bsInput.value) ?? [todayBs.year, todayBs.month, todayBs.dayOfMonth];
  const ad = parts(adInput.value) ?? [todayAd.year, todayAd.month, todayAd.dayOfMonth];
  const pattern = patternInput.value || 'yyyy/MM/dd';
  const time = getCurrentTime();

  fill('#engine-convert', [
    ['Today (BS)', iso(todayBs)],
    ['Today (AD)', iso(todayAd)],
    ['Now', `${time.hour}:${pad(time.minute)}:${pad(time.second)}`],
    ['AD → BS', guarded(() => iso(convertAdToBs(...ad)))],
    ['BS → AD', guarded(() => iso(convertBsToAd(...bs)))],
    ['BS day, in full', guarded(() => describe(getBsCalendar(...bs)))],
    ['AD day, in full', guarded(() => describe(getAdCalendar(...ad)))],
    ['BS + 45 days', guarded(() => iso(addDaysToBsDate(...bs, 45)))],
    ['BS − 45 days', guarded(() => iso(addDaysToBsDate(...bs, -45)))],
  ]);

  fill('#engine-query', [
    ['Days in this BS month', guarded(() => String(getTotalDaysInBsMonth(bs[0], bs[1])))],
    ['Days in this AD month', guarded(() => String(getTotalDaysInAdMonth(ad[0], ad[1])))],
    ['BS month opens on', guarded(() => weekday(getBsMonth(bs[0], bs[1]).firstDayOfMonth))],
    ['BS month closes on', guarded(() => weekday(getBsMonth(bs[0], bs[1]).lastDayOfMonth))],
    ['AD month opens on', guarded(() => weekday(getAdMonth(ad[0], ad[1]).firstDayOfMonth))],
    [
      'AD days in this BS month',
      guarded(() => {
        const days = getAdCalendarsInBsMonth(bs[0], bs[1]);
        return `${days.length}, ${iso(days[0])} to ${iso(days[days.length - 1])}`;
      }),
    ],
    [
      'BS days in this AD month',
      guarded(() => {
        const days = getBsCalendarsInAdMonth(ad[0], ad[1]);
        const known = days.filter((day) => day !== null);
        return `${known.length} of ${days.length} convertible`;
      }),
    ],
    ['BS days from today', guarded(() => String(getBsDaysBetween(todayBs.year, todayBs.month, todayBs.dayOfMonth, ...bs)))],
    ['AD days from today', guarded(() => String(getAdDaysBetween(todayAd.year, todayAd.month, todayAd.dayOfMonth, ...ad)))],
    [
      'BS date vs today',
      guarded(() => {
        const sign = compareBsDates(...bs, todayBs.year, todayBs.month, todayBs.dayOfMonth);
        return sign < 0 ? 'earlier' : sign > 0 ? 'later' : 'the same day';
      }),
    ],
    ['AD date convertible', guarded(() => (isAdDateConvertible(...ad) ? 'yes' : 'no'))],
  ]);

  fill('#engine-format', [
    ['BS, full', guarded(() => formatBsDate(...bs, getBsCalendar(...bs).dayOfWeek, 'en', 'full', 'full', 'full', null))],
    ['BS, full (नेपाली)', guarded(() => formatBsDate(...bs, getBsCalendar(...bs).dayOfWeek, 'ne', 'full', 'full', 'full', null))],
    ['BS, compact', guarded(() => formatBsDate(...bs, getBsCalendar(...bs).dayOfWeek, 'en', 'compact_ymd', 'short', 'short', null))],
    ['AD, long', guarded(() => formatAdDate(...ad, getAdCalendar(...ad).dayOfWeek, 'en', 'long', 'medium', 'full', null))],
    ['BS by pattern', guarded(() => formatBsDateByPattern(pattern, ...bs, 'en'))],
    ['BS by pattern (नेपाली)', guarded(() => formatBsDateByPattern(pattern, ...bs, 'ne'))],
    ['AD by pattern', guarded(() => formatAdDateByPattern(pattern, ...ad, 'en'))],
    ['AD by pattern (नेपाली)', guarded(() => formatAdDateByPattern(pattern, ...ad, 'ne'))],
  ]);

  fill('#engine-time', [
    ['English 12h', formatTimeEnglish(time.hour, time.minute, time.second, time.nanosecond, true)],
    ['English 24h', formatTimeEnglish(time.hour, time.minute, time.second, time.nanosecond, false)],
    ['Nepali 12h', formatTimeNepali(time.hour, time.minute, time.second, time.nanosecond, true)],
    ['Nepali 24h', formatTimeNepali(time.hour, time.minute, time.second, time.nanosecond, false)],
    ['BS date, ISO', guarded(() => bsIso(bs, time))],
    ['Read back (BS)', guarded(() => iso(bsDateTimeFromIso(bsIso(bs, time)).calendar))],
    ['AD date, ISO', guarded(() => adIso(ad, time))],
    ['Read back (AD)', guarded(() => iso(adDateTimeFromIso(adIso(ad, time)).calendar))],
  ]);

  fill('#engine-digits', [
    ['To Devanagari', localizeDigits(iso(todayBs), 'devanagari')],
    ['Back to Latin', toLatinDigits(localizeDigits(iso(todayBs), 'devanagari'))],
    ['BS month name', guarded(() => getBsMonthName(bs[1], 'full', 'en'))],
    ['BS month name (नेपाली)', guarded(() => getBsMonthName(bs[1], 'full', 'ne'))],
    ['BS month, short', guarded(() => getBsMonthName(bs[1], 'short', 'en'))],
    ['AD month name', guarded(() => getAdMonthName(ad[1], 'full', 'en'))],
    ['AD month, medium', guarded(() => getAdMonthName(ad[1], 'medium', 'en'))],
    ['Weekday of the BS date', guarded(() => weekday(getBsCalendar(...bs).dayOfWeek))],
    ['The same, in Nepali', guarded(() => getWeekdayName(getBsCalendar(...bs).dayOfWeek, 'full', 'ne'))],
  ]);

  const bsRange = getBsYearRange();
  const adRange = getAdYearRange();
  fill('#engine-bounds', [
    ['BS year range', `${bsRange.first} to ${bsRange.last}`],
    ['AD year range', `${adRange.first} to ${adRange.last}`],
    ['AD years for the whole BS range', spanFor(bsRange.first, bsRange.last)],
    ['AD years for BS 2080..2085', spanFor(2080, 2085)],
    ['AD years for BS 2000..2010', spanFor(2000, 2010)],
    ['AD years for the last BS year', spanFor(bsRange.last, bsRange.last)],
    ['First AD day of the BS range', guarded(() => iso(convertBsToAd(bsRange.first, 1, 1)))],
    ['AD 1913-01-01 convertible', isAdDateConvertible(1913, 1, 1) ? 'yes' : 'no'],
    ['AD 1913-04-13 convertible', isAdDateConvertible(1913, 4, 13) ? 'yes' : 'no'],
    [`AD ${adRange.last + 1}-01-01 convertible`, isAdDateConvertible(adRange.last + 1, 1, 1) ? 'yes' : 'no'],
  ]);
}

/** The English years a Bikram Sambat window maps onto, clamped into the supported range. */
function spanFor(first: number, last: number): string {
  const range = getAdYearRangeForBsYears(first, last);
  return `${range.first} to ${range.last}`;
}

function bsIso(date: [number, number, number], time: ReturnType<typeof getCurrentTime>): string {
  return bsDateTimeToIso(...date, time.hour, time.minute, time.second, time.nanosecond);
}

function adIso(date: [number, number, number], time: ReturnType<typeof getCurrentTime>): string {
  return adDateTimeToIso(...date, time.hour, time.minute, time.second, time.nanosecond);
}

/** A day with the detail the bare triple does not carry: its weekday and its place in the year. */
function describe(date: ReturnType<typeof getBsCalendar>): string {
  return `${iso(date)}, ${weekday(date.dayOfWeek)}, day ${date.dayOfYear} of the year`;
}

function weekday(dayOfWeek: number): string {
  return getWeekdayName(dayOfWeek, 'full', 'en');
}

renderEngine();

/* Form submit demo. */
$('#demo-form').addEventListener('submit', (e) => {
  e.preventDefault();
  const field = $('#demo-form nepali-date-field') as HTMLElement & { value: string };
  record('submit', { value: field.value }, '<form>');
});

function pad(n: number): string {
  return n < 10 ? `0${n}` : String(n);
}

/* Marking days: the same list on every element that draws a grid. */
const WorkingWindow = 21;
const MARKED_MONTH = { year: 2081, month: 5 };
const FESTIVAL_MONTH = { year: 2081, month: 6 };

const holidays = [
  { date: bs(MARKED_MONTH, 26), name: 'Constitution Day', kind: 'governmentPublic' },
  { date: bs(MARKED_MONTH, 29), name: 'Local jatra', kind: 'regional' },
  { date: bs(MARKED_MONTH, 31), name: 'World Health Day', kind: 'observance' },
];

const ownEvents = [
  { date: bs(MARKED_MONTH, 25), name: 'Standup', indicate: true, color: '#42a5f5' },
  { date: bs(MARKED_MONTH, 28), name: 'Sprint review', indicate: true, color: '#f4511e' },
  { date: bs(MARKED_MONTH, 28), name: "Aama's birthday", indicate: true, color: '#8e24aa' },
  { date: bs(MARKED_MONTH, 28), name: 'Dentist', indicate: true, color: '#43a047' },
  { date: bs(MARKED_MONTH, 28), name: 'Bank errand', indicate: true, color: '#6d4c41' },
];

for (const el of document.querySelectorAll('.marks')) {
  el.setAttribute('events', JSON.stringify(holidays));
}

$('#events-dots').setAttribute('events', JSON.stringify([...holidays, ...ownEvents]));

/* A span states a festival once and marks every day it covers. */
$('#span-festival').setAttribute(
  'events',
  JSON.stringify([
    {
      date: bs(FESTIVAL_MONTH, 17),
      endDate: bs(FESTIVAL_MONTH, 26),
      name: 'Dashain',
      kind: 'religious',
    },
  ]),
);

$('#span-leave').setAttribute(
  'events',
  JSON.stringify([
    { date: bs(MARKED_MONTH, 24), days: 5, name: 'Annual leave', kind: 'observance' },
  ]),
);

$('#open-events-dialog').addEventListener('click', () =>
  ($('#events-dialog') as NepaliDatePickerDialog).show(),
);

/* The queries behind the marking: one policy, asked about a day, a month and a span of days.
   createEvent takes the kind's own closes-offices default; createDetailedEvent states it, plus the
   id a span's days share and an opaque payload the library never reads. A span is written once and
   expanded either by a day count or by the day it ends on. */
const policy = createCalendarPolicy(
  [7],
  [
    ...holidays.map((h) =>
      createEvent(MARKED_MONTH.year, MARKED_MONTH.month, Number(h.date.slice(-2)), h.name, h.kind),
    ),
    ...expandEventDays(
      createDetailedEvent(
        FESTIVAL_MONTH.year,
        FESTIVAL_MONTH.month,
        17,
        'Dashain',
        'religious',
        true,
        'dashain',
        '{"source":"cache"}',
      ),
      10,
    ),
    ...expandEventThrough(
      createDetailedEvent(
        FESTIVAL_MONTH.year,
        FESTIVAL_MONTH.month,
        2,
        'Ghatasthapana week',
        'religious',
        false,
        'ghatasthapana',
        null,
      ),
      FESTIVAL_MONTH.year,
      FESTIVAL_MONTH.month,
      8,
    ),
  ],
);

function describeDay(year: number, month: number, dayOfMonth: number): void {
  const status = policy.statusOf(year, month, dayOfMonth);
  $('#policy-weekly-off').textContent = status.isWeeklyOff ? 'yes' : 'no';
  $('#policy-non-working').textContent = status.isNonWorking ? 'yes' : 'no';
  $('#policy-kind').textContent = status.primaryKind ?? 'none';
  $('#policy-names').textContent = status.names.length ? status.names.join(', ') : 'nothing';

  const days = policy.monthStatus(year, month);
  $('#policy-month-days').textContent = String(days.length);
  $('#policy-month-closed').textContent = String(days.filter((day) => day.isNonWorking).length);
  $('#policy-month-entries').textContent = String(policy.eventsIn(year, month).length);

  // The counting window ends 21 days on, which the engine resolves across the month end.
  const windowEnd = addDaysToBsDate(year, month, dayOfMonth, WorkingWindow);
  $('#policy-working').textContent = String(
    policy.workingDaysBetween(
      year,
      month,
      dayOfMonth,
      windowEnd.year,
      windowEnd.month,
      windowEnd.dayOfMonth,
    ),
  );
  $('#policy-next').textContent = iso(policy.nextWorkingDay(year, month, dayOfMonth));
  $('#policy-plus').textContent = iso(policy.addWorkingDays(year, month, dayOfMonth, 5));
}

$('#policy-picker').addEventListener('change', (e) => {
  const { bs: picked } = (e as CustomEvent).detail;
  describeDay(picked.year, picked.month, picked.dayOfMonth);
});

describeDay(MARKED_MONTH.year, MARKED_MONTH.month, 24);

/** A `YYYY-MM-DD` Bikram Sambat string for a day of one month. */
function bs(month: { year: number; month: number }, dayOfMonth: number): string {
  return `${month.year}-${pad(month.month)}-${pad(dayOfMonth)}`;
}

function iso(date: { year: number; month: number; dayOfMonth: number }): string {
  return `${date.year}-${pad(date.month)}-${pad(date.dayOfMonth)}`;
}

// The browsable calendar reports the day that was clicked and the entry behind a clicked line of
// the month's list. A payload is an opaque string here: this page prints it, an app would parse it
// and render whatever it holds, image URLs included.
const calendarWithLists = document.getElementById('calendar-lists');
const calendarOutput = document.getElementById('calendar-output');
if (calendarWithLists && calendarOutput) {
  const show = (event: Event): void => {
    calendarOutput.textContent = JSON.stringify((event as CustomEvent).detail, null, 2);
  };
  calendarWithLists.addEventListener('day-select', show);
  calendarWithLists.addEventListener('event-select', show);
}
