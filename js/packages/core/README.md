# @nepali-date-picker/core

Bikram Sambat (Nepali) date conversion and formatting for JavaScript and TypeScript.

This package is compiled from the Kotlin Multiplatform `:core` module of
[Nepali-Date-Picker](https://github.com/shivathapaa/Nepali-Date-Picker), so the BS↔AD conversion
tables and formatting rules match the Kotlin, Android, and Python (`nepali_calendar_utils`) builds
exactly, not a re-implementation.

- **Zero runtime dependencies.** Time zones resolve through the platform `Intl` API.
- **Ships TypeScript types.**
- **ESM.** Works in Node, Deno, and every bundler (Vite, webpack, Rollup, esbuild, Next.js).
- Supported range: **BS 1970–2100**, **AD 1913–2043**.

## Install

```bash
npm install @nepali-date-picker/core
```

## Usage

```ts
import {
  convertAdToBs,
  convertBsToAd,
  getTodayBs,
  formatBsDate,
  formatBsDateByPattern,
  localizeDigits,
} from '@nepali-date-picker/core';

convertAdToBs(2024, 9, 9);   // NepaliDate { year: 2081, month: 5, dayOfMonth: 24, era: 2, ... }
convertBsToAd(2081, 5, 24);  // NepaliDate { year: 2024, month: 9, dayOfMonth: 9, era: 1, ... }

const today = getTodayBs();  // today in Asia/Kathmandu, Bikram Sambat

// Locale-preset formatting. The 4th argument is the weekday, which this function does not
// derive: pass the real one, for example from convertAdToBs(...).dayOfWeek.
formatBsDate(2081, 5, 24, 2, 'en', 'full', 'full', 'full', null);
// "Monday, Bhadra 24, 2081"

// Unicode-pattern formatting, which resolves everything (day-of-year `D` and week-of-year `w`
// included) from the date itself.
formatBsDateByPattern('yyyy-MM-dd EEEE', 2081, 5, 24, 'ne');
// "२०८१-०५-२४ सोमबार"

localizeDigits('2081/05/24', 'devanagari'); // "२०८१/०५/२४"
```

Every value returned by a conversion is a `NepaliDate` with the full calendar breakdown
(`era`, `dayOfWeek`, `dayOfYear`, `totalDaysInMonth`, `weekOfYear`, …).

## API

String enum arguments are case-insensitive.

- `language`: `"en"` | `"ne"`
- `format` (name width): `"short"` | `"medium"` | `"full"`
- `dateFormat`: `"full"` | `"long"` | `"medium"` | `"short_mdy"` | `"short_ymd"` | `"compact_mdy"` | `"compact_ymd"`
- `digitScript`: `"latin"` | `"devanagari"` | `null` (follow `language`)

### Conversion & queries

| Function | Returns |
| --- | --- |
| `getTodayBs()` / `getTodayAd()` | `NepaliDate` |
| `getCurrentTime()` | `NepaliTime` |
| `convertAdToBs(y, m, d)` / `convertBsToAd(y, m, d)` | `NepaliDate` |
| `getBsCalendar(y, m, d)` / `getAdCalendar(y, m, d)` | `NepaliDate` |
| `getBsMonth(y, m)` / `getAdMonth(y, m)` | `NepaliMonthInfo` |
| `getBsCalendarsInAdMonth(y, m)` | `Array<NepaliDate \| null>` - a whole month in one pass |
| `getAdCalendarsInBsMonth(y, m)` | `Array<NepaliDate>` - the mirror of it |
| `isAdDateConvertible(y, m, d)` | `boolean` - the year range alone is not a sufficient check |
| `getTotalDaysInBsMonth(y, m)` / `getTotalDaysInAdMonth(y, m)` | `number` |
| `addDaysToBsDate(y, m, d, days)` | `NepaliDate` |
| `getBsDaysBetween(...)` / `getAdDaysBetween(...)` | `number` |
| `compareBsDates(y1, m1, d1, y2, m2, d2)` | `number` (`<0`, `0`, `>0`) |
| `getBsYearRange()` / `getAdYearRange()` | `YearRange` |
| `getAdYearRangeForBsYears(first, last)` | `YearRange` - the AD years covering a BS range |

### Names & formatting

`getWeekdayName`, `getBsMonthName`, `getAdMonthName`, `formatBsDate`, `formatAdDate`,
`formatBsDateByPattern`, `formatAdDateByPattern`, `formatTimeEnglish`, `formatTimeNepali`.

### ISO 8601, wire text & digits

`bsDateTimeToIso`, `adDateTimeToIso`, `bsDateTimeFromIso`, `adDateTimeFromIso` persist an instant in
UTC. For a plain calendar date or a wall-clock time, use the fixed-pattern helpers instead:
`formatBsDateText`, `parseBsDateText`, `formatTimeOfDay`, `parseTimeOfDay`. They produce the same
strings the Kotlin and Swift builds persist, so a payload written by one reads on any other. Both
parsers return `null` rather than throwing.

`localizeDigits` and `toLatinDigits` convert between Latin and Devanagari numerals.

### Events, holidays & working days

No event data ships with the library: you pass your own list, and a policy pairs it with the
weekdays an institution never opens. Weekday numbers are **1-based-Sunday**, unlike `Date.getDay()`.

```js
import { createEvent, createCalendarPolicy } from '@nepali-date-picker/core';

const office = createCalendarPolicy([7], [            // Saturday off
  createEvent(2082, 6, 3, 'Constitution Day', 'governmentPublic'),
]);

office.statusOf(2082, 6, 3).isNonWorking;          // true
office.eventsIn(2082, 6);                          // a month, in date order
office.monthStatus(2082, 6);                       // one entry per day
office.workingDaysBetween(2082, 1, 1, 2082, 2, 1); // end exclusive
office.addWorkingDays(2082, 1, 1, 10);             // Excel WORKDAY semantics
```

| Function | Returns |
| --- | --- |
| `createEvent(y, m, d, name, kind)` | `NepaliEvent` - `kind` sets whether it closes the day |
| `createDetailedEvent(y, m, d, name, kind, closesOffices, id, payload)` | `NepaliEvent` - the event decides for itself |
| `expandEventDays(event, days)` | `Array<NepaliEvent>` - one entry per day of a span |
| `expandEventThrough(event, endY, endM, endD)` | `Array<NepaliEvent>` - the same span, stated by its end |
| `createCalendarPolicy(weeklyOffDays, events)` | `NepaliCalendarPolicyInfo` |

| Policy method | Returns |
| --- | --- |
| `statusOf(y, m, d)` | `NepaliDayStatusInfo` - `isWeeklyOff`, `isNonWorking`, `primaryKind`, `names`, `events`, `closures` |
| `eventsOn(y, m, d)` / `eventsIn(y, m)` | `Array<NepaliEvent>` |
| `monthStatus(y, m)` | `Array<NepaliDayStatusInfo>` - index 0 is day 1 |
| `isWeeklyOff(dayOfWeek)` / `isNonWorkingDay(y, m, d)` | `boolean` |
| `workingDaysBetween(...)` | `number` |
| `nextWorkingDay(y, m, d)` / `addWorkingDays(y, m, d, days)` | `NepaliDate` |

`kind` is `"governmentPublic"`, `"religious"`, `"regional"` or `"observance"`. The first three close
the day by default and an observance does not, but `createDetailedEvent` overrides either way: a
regional holiday closes one district and not the next, and a school programme closes nothing.

An event covers one day, so something that runs longer is an array of entries. Give it an `id` and
the days can be folded back into one row:

```js
import { createDetailedEvent, expandEventDays, createCalendarPolicy } from '@nepali-date-picker/core';

const dashain = createDetailedEvent(2082, 6, 17, 'Dashain', 'religious', true, 'dashain-2082', null);

const policy = createCalendarPolicy([7], expandEventDays(dashain, 10));  // Asoj 17 through 26
// expandEventThrough(dashain, 2082, 6, 26) is the same span, stated by its end.

const agenda = [...new Map(policy.eventsIn(2082, 6).map((e) => [e.id ?? e.name, e])).values()];
```

A span running out of Chaitra into Baisakh yields entries in both years, so each is reported by the
year that asks for it.

## Looking for a UI?

Use [`@nepali-date-picker/web-component`](https://www.npmjs.com/package/@nepali-date-picker/web-component)
for a `<nepali-date-picker>` calendar element that works in React, Vue, Angular, Svelte, and plain HTML.

## License

MPL-2.0 © Shiva Thapa ([@shivathapaa](https://github.com/shivathapaa))
