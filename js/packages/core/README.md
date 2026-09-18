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

// Locale-preset formatting
formatBsDate(2081, 5, 24, 3, 'en', 'full', 'full', 'full', null);
// "Tuesday, Asar 24, 2081"

// Unicode-pattern formatting (day-of-year `D`, week-of-year `w` resolved for you)
formatBsDateByPattern('yyyy-MM-dd EEEE', 2081, 5, 24, 'ne');
// "२०८१-०५-२४ मंगलबार"

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

### ISO 8601 & digits

`bsDateTimeToIso`, `adDateTimeToIso`, `bsDateTimeFromIso`, `adDateTimeFromIso`, `localizeDigits`,
`toLatinDigits`.

## Looking for a UI?

Use [`@nepali-date-picker/web-component`](https://www.npmjs.com/package/@nepali-date-picker/web-component)
for a `<nepali-date-picker>` calendar element that works in React, Vue, Angular, Svelte, and plain HTML.

## License

MPL-2.0 © Shiva Thapa ([@shivathapaa](https://github.com/shivathapaa))
