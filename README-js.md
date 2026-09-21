# Nepali Date Picker

[![npm web-component](https://img.shields.io/npm/v/@nepali-date-picker/web-component?label=npm%20web-component&logo=npm&color=CB3837)](https://www.npmjs.com/package/@nepali-date-picker/web-component)
[![npm core](https://img.shields.io/npm/v/@nepali-date-picker/core?label=npm%20core&logo=npm&color=CB3837)](https://www.npmjs.com/package/@nepali-date-picker/core)
[![License: MPL-2.0](https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg)](https://mozilla.org/MPL/2.0/)

A **Bikram Sambat (Nepali) date picker** for the web, plus a headless **BS ↔ AD conversion and
formatting engine**. Works in **React, Vue, Angular, Svelte, plain HTML, and Node** - standard DOM
custom elements, no framework required.

**[▶ Live demo](https://shivathapaa.github.io/Nepali-Date-Picker/demo/)** - every variant and option.

Two packages. Install whichever you need (the UI package already includes the engine):

| Package | What it is | Install when you need… |
| --- | --- | --- |
| **[`@nepali-date-picker/web-component`](https://www.npmjs.com/package/@nepali-date-picker/web-component)** | The `<nepali-date-picker>` custom elements: inline calendar, browsable `<nepali-calendar>`, dialog, docked, range, text field, wheel. | A date picker or a month calendar on screen. |
| **[`@nepali-date-picker/core`](https://www.npmjs.com/package/@nepali-date-picker/core)** | Only the conversion / formatting functions. No UI. | BS ↔ AD math with your own UI, or on the server. |

- **Localized.** English / Nepali text, Latin / Devanagari digits.
- **Accessible.** Real DOM, keyboard-navigable, ARIA grid, screen-reader labels.
- **TypeScript types** ship with both packages.
- **Supported range:** BS **1970–2100**, AD **1913–2043**. Values outside throw / are rejected.

> **Indexing (important):** months and weekdays are **1-based**. Month `1` = Baisakh … `12` = Chaitra.
> Weekday `1` = Sunday … `7` = Saturday. `era`: `1` = AD, `2` = BS.

> **Other platforms.** Both packages are generated from the Kotlin Multiplatform `:core` engine, the
> same one behind the Kotlin library (see the [main README](./README.md)), the iOS Swift package
> ([README-spm.md](./README-spm.md)), and
> [`nepali_calendar_utils`](https://github.com/shivathapaa/nepali_calendar_utils) on PyPI, so a date
> converts identically across JavaScript, Kotlin, Swift and Python. The element sources live in
> [`js/`](./js) and a showcase using every one of them is in [`sample/jsApp`](./sample/jsApp).

---

# Part 1 - `@nepali-date-picker/web-component` (UI)

## Install the web component

```bash
npm install @nepali-date-picker/web-component
```

No bundler? Load the self-contained build from a CDN. It inlines Lit and the conversion engine, so
there is nothing left for the browser to resolve:

```html
<script type="module" src="https://cdn.jsdelivr.net/npm/@nepali-date-picker/web-component"></script>
<nepali-date-picker value="2081-05-24"></nepali-date-picker>
```

313 kB minified, 82 kB gzipped, all seven elements. It is ES-module only: every browser that
implements custom elements also supports module scripts. This file is browser-only; on a server,
import the package itself (see [Server rendering](#server-rendering)).

## Registering the elements

Import the package once (side-effect import). This defines **all** the custom elements; then use the
tags anywhere in your markup:

```js
import '@nepali-date-picker/web-component';
```

To register only the element(s) you use, import the matching **subpath** instead. The saving is
modest: the shared conversion engine is most of the payload, so one element is only a few kB gzipped
lighter than all seven.

| Import | Registers |
| --- | --- |
| `import '@nepali-date-picker/web-component';` | all elements below |
| `import '@nepali-date-picker/web-component/nepali-date-picker';` | `<nepali-date-picker>` |
| `import '@nepali-date-picker/web-component/nepali-calendar';` | `<nepali-calendar>` |
| `import '@nepali-date-picker/web-component/nepali-date-range-picker';` | `<nepali-date-range-picker>` |
| `import '@nepali-date-picker/web-component/nepali-date-picker-dialog';` | `<nepali-date-picker-dialog>` |
| `import '@nepali-date-picker/web-component/nepali-date-picker-docked';` | `<nepali-date-picker-docked>` |
| `import '@nepali-date-picker/web-component/nepali-date-field';` | `<nepali-date-field>` |
| `import '@nepali-date-picker/web-component/nepali-date-range-field';` | `<nepali-date-range-field>` |
| `import '@nepali-date-picker/web-component/nepali-wheel-date-picker';` | `<nepali-wheel-date-picker>` |

There is also `@nepali-date-picker/web-component/bundle`: the same self-contained file the CDN serves,
with Lit and the engine inlined. Reach for it only when you want that build from npm without a
bundler; the default entry is the right import everywhere else.

## Shared conventions

- **All date values are Bikram Sambat `YYYY-MM-DD` strings** (e.g. `"2081-05-24"`). On typed inputs,
  `YYYY/MM/DD` and Devanagari digits are also accepted.
- Every element's **`change` event bubbles and crosses the shadow boundary**, so you can listen on the
  element itself or on any ancestor (even `document`).
- Setting `language="ne"` switches both text **and** digits to Nepali / Devanagari.
- `min` / `max` bound the selectable dates (inclusive).
- **`calendar-system` picks which calendar is displayed**, `"bs"` (default) or `"ad"`. Only the
  display changes: `value`, `start`, `end`, `min`, `max` and every `change` payload stay Bikram
  Sambat, so switching keeps the same day selected. Calendar elements also take
  `show-calendar-toggle` to let the user do the switching, and `show-adjacent-month-days` to
  fill the grid's empty cells with the neighbouring months. Switching fades the month header and the
  grid in, matching the Compose pickers; the animation is skipped under
  `prefers-reduced-motion: reduce`, and paging months is never animated. _(3.2.0)_
- **`show-secondary-date` pairs every day with the other calendar.** The displayed calendar's day
  stays large, its counterpart sits small in the corner of the same cell, and the month header gains
  a second line naming the months the other calendar straddles, for example "Sep/Oct 2026" under
  Asoj 2083. This is the web twin of Compose's `NepaliDatePickerWithEnglishDate`. The second header
  line also appears with `show-calendar-toggle` alone, since a switch puts a second calendar in play
  either way. _(3.3.0)_

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
| `calendar-system` | `calendarSystem` | `"bs"` \| `"ad"` | `"bs"` | Calendar the grid displays. |
| `show-calendar-toggle` | `showCalendarToggle` | boolean | `false` | Show the `B.S.` / `A.D.` switch. |
| `show-adjacent-month-days` | `showAdjacentMonthDays` | boolean | `false` | Fill the grid's empty cells with the neighbouring months' days, drawn faded. Clicking one picks that day and moves the grid to its month. |
| `show-secondary-date` | `showSecondaryDate` | boolean | `false` | Pair every day with the same day in the other calendar, drawn small in the corner of the cell, and name that calendar's months under the month header. |
| `events` | `events` | string | `""` | Days to mark, as JSON. See [Marking days](#marking-days-with-events-and-holidays). |
| `weekly-off-days` | `weeklyOffDays` | string | `""` | Weekdays the institution never opens, e.g. `7` or `7,1`. Sunday is 1. |

**Events:** `change` → `NepaliDatePickerChangeDetail`.

```html
<nepali-date-picker value="2081-05-24" min="2081-01-01" max="2081-12-30" show-english></nepali-date-picker>
<script type="module">
  import '@nepali-date-picker/web-component';
  document.querySelector('nepali-date-picker')
    .addEventListener('change', (e) => console.log(e.detail.bsIso, e.detail.adIso));
</script>
```

Every cell carrying both dates, the web twin of `NepaliDatePickerWithEnglishDate`:

```html
<nepali-date-picker value="2083-06-02" show-secondary-date></nepali-date-picker>
```

### `<nepali-calendar>` - browsable month calendar

Where `<nepali-date-picker>` asks for a date, this one is read. It fills the width it is given,
pages month by month, shows both calendars' numbers and the neighbouring months' days by default,
and can write the picked day and the month's events out under the grid. _(3.3.0)_

| Attribute | Property | Type | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `value` | string | `""` | Picked BS date `YYYY-MM-DD`. Empty = none. |
| `language` | `language` | `"en"` \| `"ne"` | `"en"` | Text + digit script. |
| `calendar-system` | `calendarSystem` | `"bs"` \| `"ad"` | `"bs"` | Calendar the grid displays. |
| `show-calendar-toggle` | `showCalendarToggle` | boolean | `false` | Show the `B.S.` / `A.D.` switch. |
| `show-secondary-date` | `showSecondaryDate` | boolean | **`true`** | Pair every day with the same day in the other calendar. |
| `show-adjacent-month-days` | `showAdjacentMonthDays` | boolean | **`true`** | Fill the grid's empty cells with the neighbouring months' days. |
| `show-day-summary` | `showDaySummary` | boolean | `false` | Write the picked day out under the grid: shut or working, why, and what is on it. |
| `show-month-events` | `showMonthEvents` | boolean | `false` | List the month's events, a span gathered into one clickable line. |
| `events` | `events` | string | `""` | Days to mark, as JSON. See [Marking days](#marking-days-with-events-and-holidays). |
| `weekly-off-days` | `weeklyOffDays` | string | `""` | Weekdays the institution never opens, e.g. `7` or `7,1`. Sunday is 1. |

**Events:** `day-select` → `NepaliDaySelectDetail` (`bs`, `ad`, `bsIso`, `adIso`, `isWeeklyOff`,
`isNonWorking`, `events`); `event-select` → `NepaliEventSelectDetail` (`event` with its `id` and
`payload` untouched, plus `firstBsIso` / `lastBsIso` for the days the line covers).

```html
<nepali-calendar show-day-summary show-month-events weekly-off-days="7"></nepali-calendar>
<script type="module">
  import '@nepali-date-picker/web-component';
  document.querySelector('nepali-calendar')
    .addEventListener('event-select', (e) => console.log(e.detail.event.id));
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
| `calendar-system` | `calendarSystem` | `"bs"` \| `"ad"` | `"bs"` | Calendar the grid displays. |
| `show-calendar-toggle` | `showCalendarToggle` | boolean | `false` | Show the `B.S.` / `A.D.` switch. |
| `show-adjacent-month-days` | `showAdjacentMonthDays` | boolean | `false` | Fill the grid's empty cells with the neighbouring months' days, drawn faded. Clicking one picks that day and moves the grid to its month. |
| `show-secondary-date` | `showSecondaryDate` | boolean | `false` | Pair every day with the same day in the other calendar, drawn small in the corner of the cell. The headline then carries the Gregorian date on a second line. |
| `events` | `events` | string | `""` | Days to mark, as JSON. See [Marking days](#marking-days-with-events-and-holidays). |
| `weekly-off-days` | `weeklyOffDays` | string | `""` | Weekdays the institution never opens, e.g. `7` or `7,1`. Sunday is 1. |
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
| `calendar-system` | `calendarSystem` | `"bs"` \| `"ad"` | `"bs"` | Calendar the grid displays. |
| `show-calendar-toggle` | `showCalendarToggle` | boolean | `false` | Show the `B.S.` / `A.D.` switch. |
| `show-adjacent-month-days` | `showAdjacentMonthDays` | boolean | `false` | Fill the grid's empty cells with the neighbouring months' days, drawn faded. Clicking one picks that day and moves the grid to its month. |
| `show-secondary-date` | `showSecondaryDate` | boolean | `false` | Pair every day with the same day in the other calendar, drawn small in the corner of the cell, and name that calendar's months under the month header. |
| `events` | `events` | string | `""` | Days to mark, as JSON. See [Marking days](#marking-days-with-events-and-holidays). |
| `weekly-off-days` | `weeklyOffDays` | string | `""` | Weekdays the institution never opens, e.g. `7` or `7,1`. Sunday is 1. |
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
| `calendar-system` | `calendarSystem` | `"bs"` \| `"ad"` | `"bs"` | Calendar the grid displays. |
| `show-calendar-toggle` | `showCalendarToggle` | boolean | `false` | Show the `B.S.` / `A.D.` switch. |
| `show-adjacent-month-days` | `showAdjacentMonthDays` | boolean | `false` | Fill the grid's empty cells with the neighbouring months' days, drawn faded. Clicking one picks that day and moves the grid to its month. |
| `show-secondary-date` | `showSecondaryDate` | boolean | `false` | Pair every day with the same day in the other calendar, drawn small in the corner of the cell, and name that calendar's months under the month header. |
| `events` | `events` | string | `""` | Days to mark, as JSON. See [Marking days](#marking-days-with-events-and-holidays). |
| `weekly-off-days` | `weeklyOffDays` | string | `""` | Weekdays the institution never opens, e.g. `7` or `7,1`. Sunday is 1. |

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
| `calendar-system` | `calendarSystem` | `"bs"` \| `"ad"` | `"bs"` | Calendar the user types in. |
| `label` | `label` | string | `""` | Field label. |

**Events:** `change` → `NepaliDatePickerChangeDetail` when the typed date becomes valid;
`invalid` → `NepaliDateFieldInvalidDetail` when rejected. The host also toggles a `.invalid` class.

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
| `calendar-system` | `calendarSystem` | `"bs"` \| `"ad"` | `"bs"` | Calendar the user types in. |

**Events:** `change` → `NepaliDateRangeChangeDetail` whenever a valid start or end changes;
`invalid` → `NepaliDateFieldInvalidDetail` from whichever inner field rejected the input.

```html
<nepali-date-range-field start="2081-05-10" end="2081-05-20" start-label="From" end-label="To"></nepali-date-range-field>
```

### `<nepali-wheel-date-picker>` - spinner

Three scroll-snapping columns (year / month / day). The day column resizes to the selected month.
Scroll, click an item, or use `ArrowUp` / `ArrowDown` on a focused column.

| Attribute | Property | Type | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `value` | string | `""` | Selected BS date. Empty starts the wheels on today without setting the property. |
| `language` | `language` | `"en"` \| `"ne"` | `"en"` | Text + digit script. |
| `disabled` | `disabled` | boolean | `false` | Read-only + dimmed. |
| `calendar-system` | `calendarSystem` | `"bs"` \| `"ad"` | `"bs"` | Calendar the wheels spin in. |
| `show-calendar-toggle` | `showCalendarToggle` | boolean | `false` | Show the `B.S.` / `A.D.` switch above the wheels. |

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

Both text field elements also emit `invalid` → `CustomEvent<NepaliDateFieldInvalidDetail>`
(`{ message: string }`, already localized); on `<nepali-date-range-field>` it arrives from whichever
inner field rejected the input. `<nepali-date-picker-dialog>` also emits `cancel` (no detail).

For typing a listener without spelling out the `CustomEvent` wrapper, the package exports
`NepaliDatePickerChangeEvent`, `NepaliDateRangeChangeEvent`, and `NepaliDateFieldInvalidEvent`:

```ts
import type { NepaliDatePickerChangeEvent } from '@nepali-date-picker/web-component';

picker.addEventListener('change', (event) => {
  console.log((event as NepaliDatePickerChangeEvent).detail.bsIso);
});
```

## Marking days with events and holidays

Two channels, and they never collide: **a colour says what the day is, dots say what is scheduled on
it.** A weekly off day repeats fifty-two times a year, so it is coloured and never dotted, which
leaves all three dot slots for an app's own events.

```html
<nepali-date-picker
  value="2083-06-02"
  weekly-off-days="7"
  events='[
    {"date":"2083-06-03","name":"Constitution Day","kind":"governmentPublic"},
    {"date":"2083-06-05","name":"Standup","indicate":true,"color":"#42a5f5"},
    {"date":"2083-06-05","name":"Aama'"'"'s birthday","indicate":true,"color":"#ff7043"}
  ]'>
</nepali-date-picker>
```

`weekly-off-days` is a comma-separated list where **Sunday is 1 and Saturday is 7**, so Nepal's
office week is `7` and a school closed Saturday and Sunday is `7,1`. JavaScript's own `getDay()`
numbers Sunday `0`; a list written that way would close nothing, so out-of-range numbers are
dropped rather than silently shifting the week.

Each entry of `events`:

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `date` | string | required | `YYYY-MM-DD` in Bikram Sambat. An entry without a usable date is ignored. |
| `name` | string | - | Announced after the date, so the marking is never colour-only. |
| `kind` | `"governmentPublic"` \| `"religious"` \| `"regional"` \| `"observance"` | `"observance"` | Picks the colour, and the default for `closesOffices`. |
| `closesOffices` | boolean | from `kind` | Whether the institution is shut. A programme is named without closing anything. |
| `color` | string | from `kind` | A CSS colour for this entry's dot. |
| `indicate` | boolean | `false` | Whether the day draws a dot for this. Leave it off for a holiday, which already colours the day. |
| `endDate` | string | - | Last day of a span, `YYYY-MM-DD` and included in it. Wins over `days`. |
| `days` | number | `1` | How many days a span covers, counting the first. Capped at 366. |

An entry carrying `endDate` or `days` marks every day of its span, so a ten-day festival or a week
of leave stays one line of JSON:

```html
<nepali-date-picker
  events='[
    {"date":"2083-06-17","endDate":"2083-06-26","name":"Dashain","kind":"religious"},
    {"date":"2083-07-02","days":5,"name":"Tihar","kind":"religious"}
  ]'>
</nepali-date-picker>
```

A span crossing into the next month or the next year keeps marking, and an end before the start
marks the one day rather than disappearing.

**A named closure outranks the week**, because "Dashain" says more about the day than "Saturday"
does. An event that leaves the institution open does not, so a Saturday carrying only a programme
still reads as a Saturday:

```html
<!-- Saturday, coloured as Dashain -->
<nepali-date-picker weekly-off-days="7"
  events='[{"date":"2083-06-07","name":"Dashain","kind":"religious"}]'></nepali-date-picker>

<!-- Saturday, still coloured as a Saturday -->
<nepali-date-picker weekly-off-days="7"
  events='[{"date":"2083-06-07","name":"Annual programme","kind":"observance"}]'></nepali-date-picker>
```

Malformed JSON, or an entry without a parseable date, is ignored rather than thrown, so a bad feed
leaves the calendar plain instead of breaking the page. Setting the property (rather than the
attribute) takes an array directly:

```js
document.querySelector('nepali-date-picker').events = JSON.stringify([
  { date: '2083-06-03', name: 'Constitution Day', kind: 'governmentPublic' },
]);
```

Colours come from the `--ndp-holiday-*` and `--ndp-weekly-off` custom properties in
[Theming](#theming), so a dark theme needs no per-event colours at all.

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
| `--ndp-weekly-off` | `#ba1a1a` | The number on a weekly off day. |
| `--ndp-holiday-public` | `#ba1a1a` | A `governmentPublic` day, and its dots. |
| `--ndp-holiday-religious` | `#2f6fed` | A `religious` day, and its dots. |
| `--ndp-holiday-regional` | `#7a5ea8` | A `regional` day, and its dots. |
| `--ndp-holiday-observance` | `#6b6b70` | An `observance` day, and its dots. |
| `--ndp-holiday-container` | `rgba(186,26,26,.12)` | The disc behind a tinted day. |
| `--ndp-radius` | `12px` | Corner radius. |

Not every element reads every property: a text field has no day grid to tint. Each documents its own
subset in the shipped `custom-elements.json`, which editors with custom-element support (WebStorm, or
VS Code with the Lit plugin) use for per-tag completion.

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

The elements are ordinary DOM, so every framework renders them as-is and there is no wrapper package
to install. React is the only one that needs anything extra.

<details open>
<summary><b>Plain HTML / vanilla JS</b></summary>

```html
<script type="module">import '@nepali-date-picker/web-component';</script>
<nepali-date-picker value="2081-05-24"></nepali-date-picker>
```
</details>

<details>
<summary><b>jQuery</b></summary>

No plugin needed. `.val()` does not reach a custom element's property, so assign it on the DOM node;
jQuery passes the event payload straight through on `.on()`:

```html
<script type="module" src="https://cdn.jsdelivr.net/npm/@nepali-date-picker/web-component"></script>
<nepali-date-picker id="picker"></nepali-date-picker>

<script>
  $('#picker')[0].value = '2081-05-24';
  $('#picker').on('change', (event) => console.log(event.detail.bsIso));
</script>
```
</details>

<details>
<summary><b>React and Next.js</b></summary>

Import the types entry once anywhere in the project and the tags type-check in TSX. It contains no
runtime code:

```tsx
import '@nepali-date-picker/web-component';
import '@nepali-date-picker/web-component/react';

export function Picker() {
  return (
    <nepali-date-picker
      value="2081-05-24"
      language="ne"
      showEnglish
      onChange={(event) => console.log(event.nativeEvent.detail.bsIso)}
    />
  );
}
```

Two things about React are easy to get wrong, so the shipped types encode both:

- **`onChange` hands you a React synthetic event, not the `CustomEvent`.** The payload is at
  `event.nativeEvent.detail`; `event.detail` is `undefined`.
- **`invalid` and `cancel` never reach a prop.** React forwards only the events in its own synthetic
  set, so `onInvalid` and `onCancel` are absent from the shipped types rather than type-checking and
  silently doing nothing. Subscribe through a ref instead:

```tsx
const ref = useRef<NepaliDateField>(null);

useEffect(() => {
  const el = ref.current;
  const onInvalid = (event: Event) =>
    setError((event as NepaliDateFieldInvalidEvent).detail.message);
  el?.addEventListener('invalid', onInvalid);
  return () => el?.removeEventListener('invalid', onInvalid);
}, []);
```

On React 18 and earlier every prop is written as a string attribute, so set non-string properties
through the same ref (`ref.current.showEnglish = true`).

In the Next.js App Router, put `'use client'` at the top of the file that renders the elements.
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
<summary><b>Svelte and SvelteKit</b></summary>

Nothing to configure. Svelte compiles to real DOM, so the elements and their events work directly
and `event.detail` is the payload itself.

```svelte
<script>
  import '@nepali-date-picker/web-component';
</script>

<!-- Svelte 5 -->
<nepali-date-picker value="2081-05-24" onchange={(e) => console.log(e.detail)} />

<!-- Svelte 4 -->
<nepali-date-picker value="2081-05-24" on:change={(e) => console.log(e.detail)} />
```
</details>

### Server rendering

`import '@nepali-date-picker/web-component'` is safe to run on a server. Lit installs a DOM shim on
Node, so importing the package during a Next.js, SvelteKit, Nuxt, or Astro server render does not
throw; the tags stay unupgraded until the browser runs the module. No `typeof window` guard, no
`ssr: false`, and no dynamic import are needed.

The CDN bundle is the one exception. It inlines Lit's browser build, so importing it on a server
fails with `HTMLElement is not defined`. Load it from a `<script type="module">` tag only.

---

# Part 2 - `@nepali-date-picker/core` (engine)

Just the date math and formatting, no UI. Runs in the browser, Node, Deno, and any bundler (Vite,
webpack, Rollup, esbuild, Next.js). Zero runtime dependencies. Ships TypeScript types.

## Install the engine

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
- `digitScript`: `"latin"` | `"devanagari"` | `null`. Accepted by `formatBsDate` / `formatAdDate`,
  but currently ignored by them: digits follow `language`. See
  [Format a date (preset)](#format-a-date-preset).

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
getAdCalendar(2026, 9, 17); // NepaliDate - full breakdown for an AD date, no BS round trip

// EnglishYearRange alone is not enough: the calendars start mid-year relative to each other,
// so 1913-01-01 through 1913-04-12 sit inside the range yet cannot be converted.
isAdDateConvertible(1913, 4, 12); // false
isAdDateConvertible(1913, 4, 13); // true
```

## Month & day counts

```ts
getBsMonth(2081, 5);              // NepaliMonthInfo - details for Bhadra 2081
getAdMonth(2026, 9);              // NepaliMonthInfo - details for September 2026
getTotalDaysInBsMonth(2081, 10);  // number, e.g. 30
getTotalDaysInAdMonth(2024, 2);   // number, e.g. 29
```

### Converting a whole month at once

Resolving a grid a day at a time pays the converter's day-walk once per cell. These convert a
whole month in a single pass, and are what the calendar elements use:

```ts
getBsCalendarsInAdMonth(2026, 9); // Array<NepaliDate | null>, null before the 1913-04-13 anchor
getAdCalendarsInBsMonth(2083, 6); // Array<NepaliDate>, the mirror of it

// Gregorian years covering the same span of real days as a BS year range.
getAdYearRangeForBsYears(1970, 2100); // YearRange { first: 1913, last: 2043 }
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
weekday-name and month-name widths, and a digit script:

```ts
formatBsDate(2081, 5, 24, 2, 'en', 'full', 'full', 'full', null);
// "Monday, Bhadra 24, 2081"

formatAdDate(2024, 9, 9, 2, 'en', 'full', 'medium', 'full', null);
// "Mon, September 9, 2024"
```

The weekday is **not** derived from the date, it is the 4th argument, so pass the real one (from
`convertAdToBs(...).dayOfWeek`, or `getBsCalendar(...).dayOfWeek`) or the name will not match the
date. `formatBsDateByPattern` below has no such trap.

The `dateFormat` style decides the layout, using BS `2081-05-24` with full widths:

| `dateFormat` | Output |
| --- | --- |
| `'full'` | `Monday, Bhadra 24, 2081` |
| `'long'` | `Bhadra 24, 2081` |
| `'medium'` | `2081 Bhadra 24` |
| `'short_mdy'` | `05/24/2081` |
| `'short_ymd'` | `2081/05/24` |
| `'compact_mdy'` | `05/24/81` |
| `'compact_ymd'` | `81/05/24` |

Two things about the width arguments are easy to misread:

- `monthName` only distinguishes `'short'` (`Bha`) from everything else. `'medium'` and `'full'` both
  give `Bhadra`. `weekDayName` does honour all three (`M` / `Mon` / `Monday`).
- **`digitScript` is currently ignored by these two functions.** They render digits from `language`,
  so `'en'` always gives Latin digits and `'ne'` always gives Devanagari, whatever you pass. Pass
  `null` and reach for `localizeDigits` when you need to force a script.

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
| `D` | day of the year | `150` |
| `w` | week of the year | `23` |
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

## Date and time text (the wire format)

A UTC timestamp is the wrong shape for a plain calendar date or a wall-clock time. For those, use
the fixed-pattern text helpers. These are the same strings the Kotlin, Android and Swift builds
persist, so a payload written by one reads on any other:

```ts
formatBsDateText(2082, 2, 14, 'yyyy-mm-dd', 'latin');  // "2082-02-14"
formatBsDateText(2082, 2, 14, 'dd/mm/yyyy', null);     // "14/02/2082"
formatBsDateText(2082, 2, 14, 'yyyy-mm-dd', 'devanagari'); // "२०८२-०२-१४"

parseBsDateText('2082-02-14', 'yyyy-mm-dd'); // NepaliDateParts { year, month, dayOfMonth }
parseBsDateText('2082-2-14', 'yyyy-mm-dd');  // null - every pattern is exactly 10 characters
parseBsDateText('२०८२-०२-१४', 'yyyy-mm-dd'); // Devanagari input is folded to Latin first

formatTimeOfDay(9, 30, 0, 0);            // "09:30:00"
formatTimeOfDay(23, 59, 59, 123456789);  // "23:59:59.123456789"
parseTimeOfDay('09:30:00');              // NepaliTime { hour, minute, second, nanosecond }
parseTimeOfDay('24:00:00');              // null
```

`pattern` is one of `yyyy-mm-dd` (the default, and the only form valid on the wire), `yyyy/mm/dd`,
`dd/mm/yyyy`, `dd-mm-yyyy`. Anything else is read as `yyyy-mm-dd`. Both parsers return `null`
rather than throwing, and neither trims whitespace.

`parseBsDateText` allows day 32, because some Bikram Sambat months run that long. Check the day
against the real month with `getTotalDaysInBsMonth` when it matters.

### Talking to a Kotlin backend

The optional `nepali-date-picker-serialization` Maven artifact gives Kotlin services `KSerializer`s
for these types. Those serializers do not ship to npm, and they do not need to: they read and write
exactly the strings above, so `JSON.parse` on this side is enough.

| Type | On the wire | Produce it here with |
| --- | --- | --- |
| `SimpleDate` | `"2082-02-14"` | `formatBsDateText(y, m, d, 'yyyy-mm-dd', 'latin')` |
| `SimpleDate` (struct form) | `{"year":2082,"month":2,"dayOfMonth":14}` | a plain object literal |
| `SimpleTime` | `"09:30:00"`, `"23:59:59.123456789"` | `formatTimeOfDay(h, m, s, ns)` |
| `CustomCalendar` | 12-field object: `year`, `month`, `dayOfMonth`, `era`, `firstDayOfMonth`, `lastDayOfMonth`, `totalDaysInMonth`, `dayOfWeekInMonth`, `dayOfWeek`, `dayOfYear`, `weekOfMonth`, `weekOfYear` | `JSON.stringify(convertAdToBs(…))` |
| `CalendarSystem` | `1` for AD, `2` for BS | the `era` field |
| `NepaliCalendarEvent` | `{"date":"2082-01-01","name":"…","kind":"GovernmentPublic"}`, plus `closesOffices`, `id` and `payload` when set | see the note below |
| `NepaliDayStatus` | `{"isWeeklyOff":false,"events":[…]}` | - |

A `NepaliDate` returned by any conversion function is a plain object, so `JSON.stringify` on it
already produces the `CustomCalendar` shape field for field, in that order.
The last five fields are optional on the way back into Kotlin and default to `-1`, so a payload that
omits them still decodes.

`CustomCalendar` is a fully resolved calendar record, not just a date. When all you mean is a day,
send the `SimpleDate` string.

**The one field that is spelled differently.** Kotlin writes `kind` as the enum's own name, so
`GovernmentPublic`, `Religious`, `Regional`, `Observance`, capitalised. Everything on this side uses
`governmentPublic`, `religious`, `regional`, `observance`. Kotlin rejects an unknown name rather
than guessing, so convert at the boundary:

```ts
const toWire = (kind: string) => kind.charAt(0).toUpperCase() + kind.slice(1);
const fromWire = (kind: string) => kind.charAt(0).toLowerCase() + kind.slice(1);
```

The event's `date` is the `YYYY-MM-DD` string above, so `kind` is the only field needing this.

`endDate` has no counterpart on the wire. A Kotlin event covers exactly one day, and a span is
written out as one entry per day carrying the same `name`, `kind` and `id`. Expand a span before
sending it, and collapse the entries back by `id` on the way in.

## Digits

```ts
localizeDigits('2081/05/24', 'devanagari'); // "२०८१/०५/२४"
localizeDigits('Today is 2024', 'devanagari'); // "Today is २०२४"
toLatinDigits('२०८१ सोमबार');                 // "2081 सोमबार"
```

---

## Events, holidays and working days

The engine models a calendar's events, so a Node service can answer "is this a working day?" the
same way the picker paints it. **No event data ships with the library**: you pass your own.

```ts
import { createEvent, createDetailedEvent, createCalendarPolicy } from '@nepali-date-picker/core';

// One thing on one day. `kind` picks the colour and the default for closesOffices.
const constitutionDay = createEvent(2082, 6, 3, 'Constitution Day', 'governmentPublic');

// The long form spells out everything the short one leaves to the kind.
const programme = createDetailedEvent(
  2082, 6, 5, 'Annual programme', 'religious',
  false,               // closesOffices: this one does not shut the school
  'evt-42',            // id, handed back untouched
  '{"images":["a.png"]}' // payload, an opaque string the engine never parses
);

// A policy is one institution: the weekdays it never opens, plus its events.
const office = createCalendarPolicy([7], [constitutionDay, programme]);    // Saturday off
const school = createCalendarPolicy([7, 1], [constitutionDay]);            // Sat + Sun off
```

Weekday numbers are **1-based-Sunday**, unlike `Date.getDay()`. A list written the 0-based way would
close nothing at all, so `createCalendarPolicy([0, 6], [])` throws rather than failing quietly.

An event covers one day, so something that runs longer is an array of entries. Expand it once and
hand the result straight to a policy:

```ts
import { expandEventDays, expandEventThrough } from '@nepali-date-picker/core';

const dashain = createDetailedEvent(2082, 6, 17, 'Dashain', 'religious', true, 'dashain-2082', null);

expandEventDays(dashain, 10);                  // ten entries, Asoj 17 through 26
expandEventThrough(dashain, 2082, 6, 26);      // the same span, stated by its end

const festivalCalendar = createCalendarPolicy([7], expandEventDays(dashain, 10));

// Fold the days back into one agenda row by the id they share.
const agenda = [
  ...new Map(festivalCalendar.eventsIn(2082, 6).map((e) => [e.id ?? e.name, e])).values(),
];
```

A span running out of Chaitra into Baisakh yields entries in both years, so each is reported by the
year that asks for it.

```ts
const status = office.statusOf(2082, 6, 3);
status.isWeeklyOff;   // the week closes the day
status.isNonWorking;  // closed for either reason, counted once
status.primaryKind;   // "governmentPublic", or null when only the week closes it
status.names;         // ["Constitution Day"], strongest kind first
status.events;        // the same events in full, with your id and payload
status.closures;      // only the ones that actually shut the door

office.eventsOn(2082, 6, 3);      // one day
office.eventsIn(2082, 6);         // a whole month, in date order
office.monthStatus(2082, 6);      // one entry per day; index 0 is day 1
office.isWeeklyOff(7);            // true, Saturday
office.isNonWorkingDay(2082, 6, 3); // closed for either reason
office.weeklyOffDays;             // [7], the weekdays it was built with, sorted
```

`monthStatus` resolves the month's first weekday once and walks forward, so a grid costs one
conversion instead of one per cell.

```ts
office.workingDaysBetween(2082, 1, 1, 2082, 2, 1); // end exclusive
office.nextWorkingDay(2082, 1, 1);                 // NepaliDate; the date itself if it works
office.addWorkingDays(2082, 1, 1, 10);             // Excel WORKDAY semantics; negatives walk back
```

A day that is both a weekly off day and a holiday is skipped once, not twice, and an event that does
not close is not skipped at all: a week of programmes is still five working days.

## TypeScript

Both packages ship declarations.

```ts
import type {
  NepaliDate, NepaliMonthInfo, NepaliTime, NepaliDateTime, YearRange,
  NepaliEvent, NepaliDayStatusInfo, NepaliCalendarPolicyInfo,
} from '@nepali-date-picker/core';

import type {
  CalendarDate,                   // { year, month, dayOfMonth }
  NepaliLanguage,                 // "en" | "ne"
  NepaliDatePickerChangeDetail,   // single-date change payload
  NepaliDateRangeChangeDetail,    // range change payload
  NepaliDateFieldInvalidDetail,   // invalid payload
  NepaliDatePickerChangeEvent,    // the CustomEvent wrappers, for listener callbacks
  NepaliDateRangeChangeEvent,
  NepaliDateFieldInvalidEvent,
  NepaliDatePicker, NepaliDatePickerDialog, NepaliDatePickerDocked,
  NepaliDateRangePicker, NepaliDateField, NepaliDateRangeField, NepaliWheelDatePicker,
} from '@nepali-date-picker/web-component';
```

The `events` attribute is a JSON **string**, so it needs no type of its own. Declare the shape you
build it from yourself:

```ts
type NepaliEventKind = 'governmentPublic' | 'religious' | 'regional' | 'observance';

interface NepaliEventInput {
  date: string;            // "YYYY-MM-DD" Bikram Sambat
  name?: string;
  kind?: NepaliEventKind;
  closesOffices?: boolean;
  color?: string;
  indicate?: boolean;
  endDate?: string;
  days?: number;
}
```

Element instances are typed via `HTMLElementTagNameMap`, so `document.querySelector('nepali-date-picker')`
is typed as `NepaliDatePicker` (with `.value`, `.show()`, etc.).

For the tags themselves in TSX, add `import '@nepali-date-picker/web-component/react';` once. See
[React and Next.js](#framework-usage).

## Complete `core` export list

Classes `NepaliDate`, `NepaliDateParts`, `NepaliMonthInfo`, `NepaliTime`, `NepaliDateTime`,
`YearRange`, `NepaliEvent`, `NepaliDayStatusInfo`, `NepaliCalendarPolicyInfo`; functions
`getBsYearRange`, `getAdYearRange`, `getTodayBs`, `getTodayAd`, `getCurrentTime`, `convertAdToBs`,
`convertBsToAd`, `getBsCalendar`, `getAdCalendar`, `getBsMonth`, `getAdMonth`,
`getBsCalendarsInAdMonth`, `getAdCalendarsInBsMonth`, `isAdDateConvertible`,
`getAdYearRangeForBsYears`,
`getTotalDaysInBsMonth`, `getTotalDaysInAdMonth`, `addDaysToBsDate`, `getBsDaysBetween`,
`getAdDaysBetween`, `compareBsDates`, `getWeekdayName`, `getBsMonthName`, `getAdMonthName`,
`formatBsDate`, `formatAdDate`, `formatBsDateByPattern`, `formatAdDateByPattern`, `formatTimeEnglish`,
`formatTimeNepali`, `bsDateTimeToIso`, `adDateTimeToIso`, `bsDateTimeFromIso`, `adDateTimeFromIso`,
`formatBsDateText`, `parseBsDateText`, `formatTimeOfDay`, `parseTimeOfDay`,
`localizeDigits`, `toLatinDigits`, `createEvent`, `createDetailedEvent`, `expandEventDays`,
`expandEventThrough`, `createCalendarPolicy`.

## Links

- **Live demo:** https://shivathapaa.github.io/Nepali-Date-Picker/demo/
- **Source & issues:** https://github.com/shivathapaa/Nepali-Date-Picker

## License

[MPL-2.0](https://mozilla.org/MPL/2.0/) © Shiva Thapa ([@shivathapaa](https://github.com/shivathapaa))
