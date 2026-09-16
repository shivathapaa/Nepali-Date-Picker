# Nepali Date Picker

[![License: MPL-2.0](https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg)](https://mozilla.org/MPL/2.0/)

A **Bikram Sambat (Nepali) date picker** for the web, plus a headless **BS ↔ AD conversion and
formatting engine**. Works in **React, Vue, Angular, Svelte, plain HTML, and Node** - standard DOM
custom elements, no framework required.

**[▶ Live demo](https://shivathapaa.github.io/Nepali-Date-Picker/demo/)** - every variant and option.

Two packages. Install whichever you need (the UI package already includes the engine):

| Package | What it is | Install when you need… |
| --- | --- | --- |
| **`@nepali-date-picker/web-component`** | The `<nepali-date-picker>` custom elements: inline calendar, dialog, docked, range, text field, wheel. | A date picker on screen. |
| **`@nepali-date-picker/core`** | Only the conversion / formatting functions. No UI. | BS ↔ AD math with your own UI, or on the server. |

- **Localized.** English / Nepali text, Latin / Devanagari digits.
- **Accessible.** Real DOM, keyboard-navigable, ARIA grid, screen-reader labels.
- **TypeScript types** ship with both packages.
- **Supported range:** BS **1970–2100**, AD **1913–2043**. Values outside throw / are rejected.

> **Indexing (important):** months and weekdays are **1-based**. Month `1` = Baisakh … `12` = Chaitra.
> Weekday `1` = Sunday … `7` = Saturday. `era`: `1` = AD, `2` = BS.

> **Where the code lives.** Both packages are generated from the Kotlin Multiplatform `:core` engine
> in [shivathapaa/Nepali-Date-Picker](https://github.com/shivathapaa/Nepali-Date-Picker). The
> TypeScript sources for the elements are under [`js/`](./js), and a showcase that uses every one of
> them is in [`sample/jsApp`](./sample/jsApp).

> **Other platforms.** The same calendar tables power the Kotlin Multiplatform library (see the
> [main README](./README.md)), the iOS Swift package (see [README-spm.md](./README-spm.md)), and
> [`nepali_calendar_utils`](https://github.com/shivathapaa/nepali_calendar_utils) on PyPI, so results
> match across JavaScript, Kotlin, Swift and Python.

---

# Part 1 - `@nepali-date-picker/web-component` (UI)

## Install

```bash
npm install @nepali-date-picker/web-component
```

## Registering the elements

Import the package once (side-effect import). This defines **all** the custom elements; then use the
tags anywhere in your markup:

```js
import '@nepali-date-picker/web-component';
```

To pull in only the element(s) you use (smaller bundle), import the matching **subpath** instead:

| Import | Registers |
| --- | --- |
| `import '@nepali-date-picker/web-component';` | all elements below |
| `import '@nepali-date-picker/web-component/nepali-date-picker';` | `<nepali-date-picker>` |
| `import '@nepali-date-picker/web-component/nepali-date-range-picker';` | `<nepali-date-range-picker>` |
| `import '@nepali-date-picker/web-component/nepali-date-picker-dialog';` | `<nepali-date-picker-dialog>` |
| `import '@nepali-date-picker/web-component/nepali-date-picker-docked';` | `<nepali-date-picker-docked>` |
| `import '@nepali-date-picker/web-component/nepali-date-field';` | `<nepali-date-field>` |
| `import '@nepali-date-picker/web-component/nepali-date-range-field';` | `<nepali-date-range-field>` |
| `import '@nepali-date-picker/web-component/nepali-wheel-date-picker';` | `<nepali-wheel-date-picker>` |

## Shared conventions

- **All date values are Bikram Sambat `YYYY-MM-DD` strings** (e.g. `"2081-05-24"`). On typed inputs,
  `YYYY/MM/DD` and Devanagari digits are also accepted.
- Every element's **`change` event bubbles and crosses the shadow boundary**, so you can listen on the
  element itself or on any ancestor (even `document`).
- Setting `language="ne"` switches both text **and** digits to Nepali / Devanagari.
- `min` / `max` bound the selectable dates (inclusive).

---

## Components

### `<nepali-date-picker>` - inline calendar

An always-visible month calendar.

| Attribute | Property | Type | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `value` | string | `""` | Selected BS date `YYYY-MM-DD`. Empty = none. |
| `language` | `language` | `"en"` \| `"ne"` | `"en"` | Text + digit script. |
| `min` | `min` | string | `""` | Earliest selectable date, or `""` for no lower bound. |
| `max` | `max` | string | `""` | Latest selectable date, or `""` for no upper bound. |
| `disabled` | `disabled` | boolean | `false` | Read-only + dimmed (reflected to attribute). |
| `show-english` | `showEnglish` | boolean | `false` | Show the Gregorian equivalent under the grid. |

**Events:** `change` → `NepaliDatePickerChangeDetail`.

```html
<nepali-date-picker value="2081-05-24" min="2081-01-01" max="2081-12-30" show-english></nepali-date-picker>
<script type="module">
  import '@nepali-date-picker/web-component';
  document.querySelector('nepali-date-picker')
    .addEventListener('change', (e) => console.log(e.detail.bsIso, e.detail.adIso));
</script>
```

### `<nepali-date-picker-dialog>` - modal

A modal calendar with a headline and OK / Cancel actions. Add `fullscreen` for the full-screen layout.

| Attribute | Property | Type | Default | Description |
| --- | --- | --- | --- | --- |
| `open` | `open` | boolean | `false` | Whether the dialog is shown (reflected). Or use `.show()` / `.close()`. |
| `value` | `value` | string | `""` | Selected / initial BS date. |
| `language` | `language` | `"en"` \| `"ne"` | `"en"` | Text + digit script. |
| `min` / `max` | `min` / `max` | string | `""` | Selectable bounds. |
| `fullscreen` | `fullscreen` | boolean | `false` | Full-screen layout. |
| `show-english` | `showEnglish` | boolean | `false` | Show the Gregorian equivalent. |
| `heading` | `heading` | string | localized "Select Nepali Date" | Dialog title text. |

**Methods:** `show(): void`, `close(): void`.
**Events:** `change` → `NepaliDatePickerChangeDetail` (on confirm); `cancel` (no detail) when dismissed
via Cancel, the backdrop, or `Escape`.

```html
<button id="open">Pick a date</button>
<nepali-date-picker-dialog id="dlg" value="2081-05-24" show-english></nepali-date-picker-dialog>
<script type="module">
  import '@nepali-date-picker/web-component';
  const dlg = document.getElementById('dlg');
  document.getElementById('open').addEventListener('click', () => dlg.show());
  dlg.addEventListener('change', (e) => console.log('picked', e.detail.bsIso));
  dlg.addEventListener('cancel', () => console.log('dismissed'));
</script>
```

### `<nepali-date-picker-docked>` - field + popover

A text field the user can type into, with a 📅 button that opens the calendar in an anchored popover.
Closes on outside click or `Escape`.

| Attribute | Property | Type | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `value` | string | `""` | Selected BS date. |
| `language` | `language` | `"en"` \| `"ne"` | `"en"` | Text + digit script. |
| `min` / `max` | `min` / `max` | string | `""` | Selectable bounds. |
| `disabled` | `disabled` | boolean | `false` | Read-only + dimmed. |
| `show-english` | `showEnglish` | boolean | `false` | Show the Gregorian equivalent in the popover. |
| `label` | `label` | string | `""` | Field label. |

**Events:** `change` → `NepaliDatePickerChangeDetail` (fires when a valid date is typed or clicked).

```html
<nepali-date-picker-docked label="Appointment date" value="2081-05-24"></nepali-date-picker-docked>
```

### `<nepali-date-range-picker>` - range calendar

First click sets the start, second sets the end (an earlier second click swaps them); a third click
starts a new range.

| Attribute | Property | Type | Default | Description |
| --- | --- | --- | --- | --- |
| `start` | `start` | string | `""` | Selected start BS date. |
| `end` | `end` | string | `""` | Selected end BS date. |
| `language` | `language` | `"en"` \| `"ne"` | `"en"` | Text + digit script. |
| `min` / `max` | `min` / `max` | string | `""` | Selectable bounds. |
| `disabled` | `disabled` | boolean | `false` | Read-only + dimmed. |
| `show-english` | `showEnglish` | boolean | `false` | Show the Gregorian equivalent. |

**Events:** `change` → `NepaliDateRangeChangeDetail` on each pick.

```html
<nepali-date-range-picker start="2081-05-10" end="2081-05-18" show-english></nepali-date-range-picker>
<script type="module">
  import '@nepali-date-picker/web-component';
  document.querySelector('nepali-date-range-picker')
    .addEventListener('change', (e) => console.log(e.detail.startBsIso, e.detail.endBsIso));
</script>
```

### `<nepali-date-field>` - typed text field

A single text field with inline validation and localized error messages. No calendar, so it drops into
forms. Accepts `YYYY/MM/DD` and Devanagari digits.

| Attribute | Property | Type | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `value` | string | `""` | Selected BS date. |
| `language` | `language` | `"en"` \| `"ne"` | `"en"` | Text + digit script + error language. |
| `min` / `max` | `min` / `max` | string | `""` | Allowed bounds. |
| `disabled` | `disabled` | boolean | `false` | Read-only + dimmed. |
| `label` | `label` | string | `""` | Field label. |

**Events:** `change` → `NepaliDatePickerChangeDetail` when the typed date becomes valid;
`invalid` → `{ message: string }` when rejected. The host also toggles a `.invalid` class.

```html
<nepali-date-field label="Date of birth" value="2050-04-12"></nepali-date-field>
<script type="module">
  import '@nepali-date-picker/web-component';
  const f = document.querySelector('nepali-date-field');
  f.addEventListener('change',  (e) => console.log('valid',   e.detail.bsIso));
  f.addEventListener('invalid', (e) => console.log('invalid', e.detail.message));
</script>
```

### `<nepali-date-range-field>` - two typed fields

Two `<nepali-date-field>`s for a start / end range, with a cross-field check that the end is not before
the start (localized error). The end field's lower bound follows the chosen start automatically.

| Attribute | Property | Type | Default | Description |
| --- | --- | --- | --- | --- |
| `start` | `start` | string | `""` | Start BS date. |
| `end` | `end` | string | `""` | End BS date. |
| `language` | `language` | `"en"` \| `"ne"` | `"en"` | Text + digit script + error language. |
| `min` / `max` | `min` / `max` | string | `""` | Allowed bounds. |
| `start-label` | `startLabel` | string | localized "Start date" | Start field label. |
| `end-label` | `endLabel` | string | localized "End date" | End field label. |
| `disabled` | `disabled` | boolean | `false` | Read-only + dimmed. |

**Events:** `change` → `NepaliDateRangeChangeDetail` whenever a valid start or end changes.

```html
<nepali-date-range-field start="2081-05-10" end="2081-05-20" start-label="From" end-label="To"></nepali-date-range-field>
```

### `<nepali-wheel-date-picker>` - spinner

Three scroll-snapping columns (year / month / day). The day column resizes to the selected month.
Scroll, click an item, or use `ArrowUp` / `ArrowDown` on a focused column.

| Attribute | Property | Type | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `value` | string | today | Selected BS date. |
| `language` | `language` | `"en"` \| `"ne"` | `"en"` | Text + digit script. |
| `disabled` | `disabled` | boolean | `false` | Read-only + dimmed. |

**Events:** `change` → `NepaliDatePickerChangeDetail` whenever the selection changes.

```html
<nepali-wheel-date-picker value="2081-05-24"></nepali-wheel-date-picker>
```

---

## Event payloads

Single-date elements (`<nepali-date-picker>`, `-dialog`, `-docked`, `-field`, `-wheel-date-picker`)
emit `change` with:

```ts
interface NepaliDatePickerChangeDetail {
  bs: { year: number; month: number; dayOfMonth: number };  // Bikram Sambat
  ad: { year: number; month: number; dayOfMonth: number };  // Gregorian
  bsIso: string;      // "YYYY-MM-DD" Bikram Sambat
  adIso: string;      // "YYYY-MM-DD" Gregorian
  formatted: string;  // human-readable BS date in the current language
}
```

Range elements (`<nepali-date-range-picker>`, `<nepali-date-range-field>`) emit `change` with (each
end is `null` until chosen):

```ts
interface NepaliDateRangeChangeDetail {
  start: { year; month; dayOfMonth } | null;
  end:   { year; month; dayOfMonth } | null;
  startBsIso: string | null;  endBsIso: string | null;
  startAdIso: string | null;  endAdIso: string | null;
}
```

`<nepali-date-field>` also emits `invalid` → `CustomEvent<{ message: string }>`.
`<nepali-date-picker-dialog>` also emits `cancel` (no detail).

## Keyboard (calendar elements)

| Key | Action |
| --- | --- |
| Arrow keys | Move by day / week, crossing month boundaries |
| `Home` / `End` | Jump to the start / end of the week |
| `PageUp` / `PageDown` | Previous / next month |
| `Enter` / `Space` | Select the focused day |
| `ArrowUp` / `ArrowDown` | Move a focused wheel column |
| `Escape` | Close the dialog / docked popover |

## Theming

Every element reads the same `--ndp-*` CSS custom properties. They **inherit**, so you can set them on
the element, or on any ancestor (e.g. `body`) - handy for a global or dark theme.

| Property | Default | Controls |
| --- | --- | --- |
| `--ndp-font` | system UI stack | Font family. |
| `--ndp-bg` | `#ffffff` | Surface / popover / dialog background. |
| `--ndp-text` | `#1b1b1f` | Primary text. |
| `--ndp-muted` | `#6b6b70` | Weekday headers, secondary text. |
| `--ndp-accent` | `#2f6fed` | Selection background, links. |
| `--ndp-on-accent` | `#ffffff` | Text on the accent color. |
| `--ndp-hover` | `rgba(47,111,237,.12)` | Hover background. |
| `--ndp-in-range` | `rgba(47,111,237,.14)` | Range-span background. |
| `--ndp-today-ring` | `#2f6fed` | Ring around today. |
| `--ndp-border` | `#d3d4d8` | Field / select borders. |
| `--ndp-error` | `#ba1a1a` | Validation error color. |
| `--ndp-radius` | `12px` | Corner radius. |

```css
/* Brand accent */
nepali-date-picker {
  --ndp-accent: #d6336c;
  --ndp-today-ring: #d6336c;
  --ndp-radius: 16px;
  --ndp-font: 'Inter', sans-serif;
}

/* Dark theme, applied from an ancestor */
body.dark nepali-date-picker,
body.dark nepali-date-picker-dialog {
  --ndp-bg: #171a21;
  --ndp-text: #e8eaef;
  --ndp-muted: #9aa2b1;
  --ndp-border: #262b35;
  --ndp-accent: #6ea0ff;
  --ndp-on-accent: #0f1115;
  --ndp-today-ring: #6ea0ff;
}
```

## Controlling from JavaScript

Every attribute is also a property. Set the property for non-string values / dynamic updates:

```js
const picker = document.querySelector('nepali-date-picker');
picker.value = '2082-01-01';   // set selection
console.log(picker.value);     // read it back

const range = document.querySelector('nepali-date-range-picker');
range.start = '2081-05-10';
range.end   = '2081-05-18';

const dlg = document.querySelector('nepali-date-picker-dialog');
dlg.show();    // open
dlg.close();   // close
```

## Framework usage

<details open>
<summary><b>Plain HTML / vanilla JS</b></summary>

```html
<script type="module">import '@nepali-date-picker/web-component';</script>
<nepali-date-picker value="2081-05-24"></nepali-date-picker>
```
</details>

<details>
<summary><b>React (19+)</b></summary>

```tsx
import '@nepali-date-picker/web-component';

export function Picker() {
  return (
    <nepali-date-picker
      value="2081-05-24"
      onChange={(e: CustomEvent) => console.log(e.detail)}
    />
  );
}
```

React 19 sets custom-element properties and forwards events natively. On React 18 or earlier, set
non-string props and attach the `change` listener through a `ref`.
</details>

<details>
<summary><b>Vue 3</b></summary>

```js
// vite.config.js - tell Vue this tag is a custom element
vue({ template: { compilerOptions: { isCustomElement: (t) => t.startsWith('nepali-') } } });
```

```vue
<script setup>
import '@nepali-date-picker/web-component';
</script>
<template>
  <nepali-date-picker value="2081-05-24" @change="(e) => console.log(e.detail)" />
</template>
```
</details>

<details>
<summary><b>Angular</b></summary>

Add `CUSTOM_ELEMENTS_SCHEMA` to the module / component, import the package once, then:

```html
<nepali-date-picker [attr.value]="value" (change)="onChange($event)"></nepali-date-picker>
```
</details>

<details>
<summary><b>Svelte</b></summary>

```svelte
<script>import '@nepali-date-picker/web-component';</script>
<nepali-date-picker value="2081-05-24" on:change={(e) => console.log(e.detail)} />
```
</details>

---

# Part 2 - `@nepali-date-picker/core` (engine)

Just the date math and formatting, no UI. Runs in the browser, Node, Deno, and any bundler (Vite,
webpack, Rollup, esbuild, Next.js). Zero runtime dependencies. Ships TypeScript types.

## Install

```bash
npm install @nepali-date-picker/core
```

```ts
import { convertAdToBs, getTodayBs, formatBsDateByPattern } from '@nepali-date-picker/core';
```

## Return types

Conversions and queries return rich objects (all fields are numbers):

```ts
NepaliDate {        // a full BS or AD date
  year, month, dayOfMonth,
  era,               // 1 = AD, 2 = BS
  firstDayOfMonth, lastDayOfMonth, totalDaysInMonth,
  dayOfWeekInMonth, dayOfWeek,   // dayOfWeek: 1 = Sunday … 7 = Saturday
  dayOfYear, weekOfMonth, weekOfYear
}

NepaliMonthInfo {   // month-level details
  year, month, totalDaysInMonth, firstDayOfMonth, lastDayOfMonth,
  daysFromStartOfWeekToFirstOfMonth
}

NepaliTime { hour, minute, second, nanosecond }

NepaliDateTime { calendar: NepaliDate, time: NepaliTime }

YearRange { first, last }
```

## String argument values

These string arguments are case-insensitive:

- `language`: `"en"` (English + Latin digits) | `"ne"` (Nepali + Devanagari digits)
- name width (the `format` / `weekDayName` / `monthName` args): `"short"` | `"medium"` | `"full"`
- `dateFormat`: `"full"` | `"long"` | `"medium"` | `"short_mdy"` | `"short_ymd"` | `"compact_mdy"` | `"compact_ymd"`
- `digitScript`: `"latin"` | `"devanagari"` | `null` (follow `language`)

## Ranges

```ts
getBsYearRange(); // YearRange { first: 1970, last: 2100 }
getAdYearRange(); // YearRange { first: 1913, last: 2043 }
```

## Today & current time

```ts
getTodayBs();     // NepaliDate - today in Asia/Kathmandu, Bikram Sambat
getTodayAd();     // NepaliDate - today, Gregorian
getCurrentTime(); // NepaliTime - { hour, minute, second, nanosecond }
```

## Conversion

```ts
convertAdToBs(2024, 9, 9);  // NepaliDate { year: 2081, month: 5, dayOfMonth: 24, era: 2, ... }
convertBsToAd(2081, 5, 24); // NepaliDate { year: 2024, month: 9, dayOfMonth: 9,  era: 1, ... }
getBsCalendar(2082, 4, 16); // NepaliDate - full breakdown for a BS date
```

## Month & day counts

```ts
getBsMonth(2081, 5);              // NepaliMonthInfo - details for Bhadra 2081
getTotalDaysInBsMonth(2081, 10);  // number, e.g. 30
getTotalDaysInAdMonth(2024, 2);   // number, e.g. 29
```

## Arithmetic

```ts
// Handles month / year overflow and underflow for you.
addDaysToBsDate(2081, 3, 15, 10);  // NepaliDate - 10 days later
addDaysToBsDate(2081, 3, 15, -5);  // NepaliDate - 5 days earlier
```

## Compare & count between

```ts
compareBsDates(2081, 5, 24, 2081, 6, 1); // number: <0 first is earlier, 0 equal, >0 later
getBsDaysBetween(2081, 5, 1, 2081, 6, 1); // number of days between two BS dates
getAdDaysBetween(2024, 9, 1, 2024, 10, 1); // number of days between two AD dates
```

## Names

```ts
getWeekdayName(2, 'full', 'ne');    // "सोमबार"  (2 = Monday)
getWeekdayName(5, 'medium', 'en');  // "Thu"
getBsMonthName(12, 'full', 'ne');   // "चैत"
getAdMonthName(6, 'full', 'en');    // "June"
```

## Format a date (preset)

`formatBsDate` / `formatAdDate` take the date, its weekday, the language, a `dateFormat` style, the
weekday-name and month-name widths, and a digit script (`null` = follow language):

```ts
formatBsDate(2081, 5, 24, 3, 'en', 'full', 'full', 'full', null);
// "Tuesday, Asar 24, 2081"

formatAdDate(2024, 9, 9, 2, 'en', 'medium', 'medium', 'medium', null);
// e.g. "Mon, Sep 9, 2024"
```

## Format a date (Unicode pattern)

`formatBsDateByPattern` / `formatAdDateByPattern` resolve everything (including day-of-year `D` and
week-of-year `w`) from just the date:

```ts
formatBsDateByPattern('yyyy-MM-dd EEEE', 2081, 5, 24, 'en'); // "2081-05-24 Monday"
formatBsDateByPattern('yyyy-MM-dd EEEE', 2081, 5, 24, 'ne'); // "२०८१-०५-२४ सोमबार"
formatAdDateByPattern('EEEE, MMM d, yyyy', 2024, 9, 9, 'en'); // "Monday, Sep 9, 2024"
```

**Placeholders**

| Token | Meaning | Example |
| --- | --- | --- |
| `yyyy` / `yy` | 4- / 2-digit year | `2081` / `81` |
| `MMMM` / `MMM` | full / short month name | `Bhadra` / `Bha` |
| `MM` / `M` | 2-digit / bare month | `05` / `5` |
| `dd` / `d` | 2-digit / bare day | `24` / `24` |
| `D` | day of the year | `147` |
| `w` | week of the year | `22` |
| `EEEE` | full weekday name | `Monday` |
| `E` | medium weekday name | `Mon` |
| `EEEEE` | short weekday name | `M` |
| `ee` / `e` | 2-digit / bare weekday number | `02` / `2` |

## Format time

```ts
formatTimeEnglish(14, 5, 0, 0, true);  // "2:05 PM"   (hour, minute, second, nanosecond, use12Hour)
formatTimeNepali(14, 5, 0, 0, true);   // localized Nepali time string
formatTimeEnglish(14, 5, 0, 0, false); // "14:05"
```

## ISO 8601 (persist & restore)

Store a BS or AD date-time as a UTC ISO string, then read it back:

```ts
const iso = bsDateTimeToIso(2081, 5, 24, 14, 5, 0, 0); // ISO 8601 UTC string, e.g. "…Z"
adDateTimeToIso(2024, 9, 9, 14, 5, 0, 0);              // same, from an AD date-time

bsDateTimeFromIso(iso);        // NepaliDateTime { calendar: NepaliDate (BS), time: NepaliTime }
adDateTimeFromIso(iso);        // NepaliDateTime { calendar: NepaliDate (AD), time: NepaliTime }
bsDateTimeFromIso(iso).calendar; // NepaliDate
```

## Digits

```ts
localizeDigits('2081/05/24', 'devanagari'); // "२०८१/०५/२४"
localizeDigits('Today is 2024', 'devanagari'); // "Today is २०२४"
toLatinDigits('२०८१ सोमबार');                 // "2081 सोमबार"
```

---

## TypeScript

Both packages ship declarations. Common imports:

```ts
import type {
  NepaliDate, NepaliMonthInfo, NepaliTime, NepaliDateTime, YearRange,
} from '@nepali-date-picker/core';

import type {
  CalendarDate,                   // { year, month, dayOfMonth }
  NepaliLanguage,                 // "en" | "ne"
  NepaliDatePickerChangeDetail,   // single-date change payload
  NepaliDateRangeChangeDetail,    // range change payload
  NepaliDatePicker, NepaliDatePickerDialog, NepaliDatePickerDocked,
  NepaliDateRangePicker, NepaliDateField, NepaliDateRangeField, NepaliWheelDatePicker,
} from '@nepali-date-picker/web-component';
```

Typing a custom-element `change` event:

```ts
picker.addEventListener('change', (e) => {
  const detail = (e as CustomEvent<NepaliDatePickerChangeDetail>).detail;
  console.log(detail.bsIso, detail.adIso, detail.formatted);
});
```

Element instances are typed via `HTMLElementTagNameMap`, so `document.querySelector('nepali-date-picker')`
is typed as `NepaliDatePicker` (with `.value`, `.show()`, etc.).

## Complete export list

**`@nepali-date-picker/web-component`** - classes `NepaliDatePicker`, `NepaliDateRangePicker`,
`NepaliDatePickerDialog`, `NepaliDatePickerDocked`, `NepaliDateField`, `NepaliDateRangeField`,
`NepaliWheelDatePicker`; types `CalendarDate`, `NepaliLanguage`, `NepaliDatePickerChangeDetail`,
`NepaliDateRangeChangeDetail`.

**`@nepali-date-picker/core`** - classes `NepaliDate`, `NepaliMonthInfo`, `NepaliTime`,
`NepaliDateTime`, `YearRange`; functions `getBsYearRange`, `getAdYearRange`, `getTodayBs`,
`getTodayAd`, `getCurrentTime`, `convertAdToBs`, `convertBsToAd`, `getBsCalendar`, `getBsMonth`,
`getTotalDaysInBsMonth`, `getTotalDaysInAdMonth`, `addDaysToBsDate`, `getBsDaysBetween`,
`getAdDaysBetween`, `compareBsDates`, `getWeekdayName`, `getBsMonthName`, `getAdMonthName`,
`formatBsDate`, `formatAdDate`, `formatBsDateByPattern`, `formatAdDateByPattern`, `formatTimeEnglish`,
`formatTimeNepali`, `bsDateTimeToIso`, `adDateTimeToIso`, `bsDateTimeFromIso`, `adDateTimeFromIso`,
`localizeDigits`, `toLatinDigits`.

## Links

- **Live demo:** https://shivathapaa.github.io/Nepali-Date-Picker/demo/
- **Source & issues:** https://github.com/shivathapaa/Nepali-Date-Picker

## License

[MPL-2.0](https://mozilla.org/MPL/2.0/) © Shiva Thapa ([@shivathapaa](https://github.com/shivathapaa))
