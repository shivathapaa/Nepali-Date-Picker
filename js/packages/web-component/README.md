# @nepali-date-picker/web-component

Framework-agnostic Bikram Sambat (Nepali) date picker **custom elements** that work in **React, Vue,
Angular, Svelte, and plain HTML**.

Built on [Lit](https://lit.dev) and powered by
[`@nepali-date-picker/core`](https://www.npmjs.com/package/@nepali-date-picker/core), the same
conversion engine used by the Kotlin, Android, and Python builds, so dates agree across every
platform.

**[▶ Live demo](https://shivathapaa.github.io/Nepali-Date-Picker/demo/)** - every variant and use-case.

- Real DOM (not a canvas), keyboard-navigable, screen-reader labelled (ARIA grid pattern).
- English / Nepali text and Latin / Devanagari digits.
- Selectable-range limits (`min` / `max`), theming through CSS custom properties.
- Holidays, weekly off days and your own events marked on the grid, colour and dots kept apart.

## Install

```bash
npm install @nepali-date-picker/web-component
```

Import once (registers every element), then use the tags anywhere:

```html
<script type="module">
  import '@nepali-date-picker/web-component';
</script>

<nepali-date-picker value="2081-05-24" language="ne"></nepali-date-picker>
```

To register only one element, import its subpath:
`import '@nepali-date-picker/web-component/nepali-date-field';`. The shared conversion engine is most
of the payload, so this saves a few kB gzipped rather than a lot.

Without a bundler, load the self-contained build from a CDN (313 kB minified, 82 kB gzipped, all
seven elements, nothing left to resolve):

```html
<script type="module" src="https://cdn.jsdelivr.net/npm/@nepali-date-picker/web-component"></script>
<nepali-date-picker value="2081-05-24"></nepali-date-picker>
```

Importing the package on a server is safe: Lit's Node DOM shim keeps the import from throwing, and
the tags upgrade once the browser runs the module. The CDN build is the exception, being browser-only.

## Components

| Element | Purpose |
| --- | --- |
| `<nepali-date-picker>` | Inline month calendar. |
| `<nepali-calendar>` | Browsable month calendar: both calendars' numbers, the picked day written out, the month's events listed. |
| `<nepali-date-picker-dialog>` | Modal calendar with confirm / cancel (add `fullscreen`). |
| `<nepali-date-picker-docked>` | Text field with an anchored calendar popover. |
| `<nepali-date-range-picker>` | Start / end range selection in the calendar. |
| `<nepali-date-field>` | Typed text field with inline validation, no calendar. |
| `<nepali-date-range-field>` | Two validated fields for a start / end range. |
| `<nepali-wheel-date-picker>` | Scrolling year / month / day wheels. |

### Common attributes

All date values are Bikram Sambat `YYYY-MM-DD` strings; Devanagari digits are accepted on input.
`calendar-system` changes only what is displayed, never what is reported, so switching keeps the
same day selected.

| Attribute | Type | Applies to | Description |
| --- | --- | --- | --- |
| `value` | `string` | all single-date elements | Selected date. |
| `start` / `end` | `string` | range elements | Selected range ends. |
| `language` | `"en"` \| `"ne"` | all | Text language and digit script. |
| `min` / `max` | `string` | all | Selectable bounds. |
| `disabled` | `boolean` | all | Read-only, dimmed. |
| `show-english` | `boolean` | calendar elements | Show the Gregorian equivalent. |
| `calendar-system` | `"bs"` \| `"ad"` | all | Calendar shown or typed in. Values stay Bikram Sambat. |
| `show-calendar-toggle` | `boolean` | calendar elements, wheel | Show the `B.S.` / `A.D.` switch. |
| `show-adjacent-month-days` | `boolean` | calendar elements | Fill the empty cells with the neighbouring months' days, drawn faded. Each is announced with its own month and a note that it moves the grid. |
| `show-secondary-date` | `boolean` | calendar elements | Pair every day with the same day in the other calendar, drawn small in the corner of the cell, and name that calendar's months under the month header. Both dates are announced. |
| `events` | `string` | calendar elements | Days to mark, as JSON. See [Marking days](#marking-days). |
| `weekly-off-days` | `string` | calendar elements | Weekdays the institution never opens, e.g. `7` or `7,1`. Sunday is 1. |
| `open` | `boolean` | dialog | Whether the dialog is shown (or call `.show()` / `.close()`). |
| `fullscreen` | `boolean` | dialog | Full-screen layout. |
| `label` | `string` | docked, field | Field label. |

### Events

All events bubble and cross the shadow boundary, so you can listen on the element or any ancestor.

| Event | Emitted by | Detail |
| --- | --- | --- |
| `change` | single-date elements | `NepaliDatePickerChangeDetail`: `bs`, `ad`, `bsIso`, `adIso`, `formatted` |
| `change` | range elements | `NepaliDateRangeChangeDetail`: `start`, `end`, `startBsIso`, `endBsIso`, `startAdIso`, `endAdIso` |
| `invalid` | the text field elements | `NepaliDateFieldInvalidDetail`: `message`, already localized |
| `cancel` | `<nepali-date-picker-dialog>` | none, dismissed without confirming |

```js
picker.addEventListener('change', (e) => console.log(e.detail.bsIso, e.detail.adIso));
```

`NepaliDatePickerChangeEvent`, `NepaliDateRangeChangeEvent`, and `NepaliDateFieldInvalidEvent` are
exported for typing a listener without writing the `CustomEvent` wrapper out.

### Marking days

A colour says what the day is; dots say what is scheduled on it. A weekly off day is coloured and
never dotted, which leaves the three dot slots for an app's own events.

```html
<nepali-date-picker
  weekly-off-days="7,1"
  events='[
    {"date":"2083-06-03","name":"Constitution Day","kind":"governmentPublic"},
    {"date":"2083-06-05","name":"Standup","indicate":true,"color":"#42a5f5"}
  ]'>
</nepali-date-picker>
```

`weekly-off-days` numbers **Sunday 1 through Saturday 7**, unlike `Date.getDay()`; numbers outside
that range are dropped rather than shifting the week. Each event takes `date` (required,
`YYYY-MM-DD` Bikram Sambat), `name`, `kind` (`governmentPublic` / `religious` / `regional` /
`observance`), `closesOffices`, `color` and `indicate`. Names are announced after the date, so the
marking is never carried by colour alone, and malformed JSON leaves the calendar plain instead of
throwing.

A named closure outranks the week, so a Saturday that is also Dashain is coloured as Dashain; an
event that leaves the institution open does not, so a Saturday carrying only a programme still reads
as a Saturday.

Something that runs longer than a day stays one entry: add `endDate` (inclusive) or `days`, and
every day of the span is marked the same way.

```html
<nepali-date-picker
  events='[
    {"date":"2083-06-17","endDate":"2083-06-26","name":"Dashain","kind":"religious"},
    {"date":"2083-07-02","days":5,"name":"Tihar","kind":"religious"}
  ]'>
</nepali-date-picker>
```

A span crossing into the next month or the next year keeps marking, an end before the start marks
the one day rather than disappearing, and a span is capped at 366 days so a mistyped count cannot
fill the grid for a year.

### Carrying your own record on an event

Two fields travel with an entry and come back untouched: `id`, which correlates it to your own
record and gathers the days of a span into one line, and `payload`, an opaque string nothing here
parses.

```html
<nepali-calendar
  show-day-summary
  show-month-events
  events='[
    {"date":"2083-06-17","days":4,"name":"Indra Jatra","kind":"religious",
     "id":"indra-jatra",
     "payload":"{\"description\":\"Masked dance at Basantapur\",\"imageUrl\":\"https://example.org/jatra.jpg\"}"}
  ]'>
</nepali-calendar>
```

```js
calendar.addEventListener('event-select', (e) => {
  const record = JSON.parse(e.detail.event.payload);
  // The URL is yours to render: <img src={record.imageUrl}> , a background, an icon, anything.
  banner.src = record.imageUrl;
});
```

Images are a good example of what `payload` is for. Nothing in this package fetches or draws them:
keep the URL in the payload, read it back on `day-select` or `event-select`, and render it with your
own `<img>`, CSS background or icon component. The same applies on every other platform, where the
payload is the same opaque string.

### Browsing a month

`<nepali-calendar>` is the read-a-month element rather than the pick-a-date one. It shows both
calendars' numbers and the neighbouring months' days by default, and can stack the picked day and
the month's list under the grid.

```html
<nepali-calendar show-day-summary show-month-events weekly-off-days="7"></nepali-calendar>
```

| Attribute | Default | What it does |
| --- | --- | --- |
| `show-secondary-date` | on | Pairs every cell with the same day in the other calendar. |
| `show-adjacent-month-days` | on | Fills the grid's corners with the neighbouring months' days. |
| `show-day-summary` | off | Writes the picked day out: closed or working, why, and what is on it. |
| `show-month-events` | off | Lists the month's events, a span gathered into one clickable line. |
| `show-calendar-toggle` | off | Draws the `B.S.` / `A.D.` switch. |

It fires `day-select` (the day in both calendars, whether the institution is shut, and everything
named on it) and `event-select` (the entry behind a clicked line, `id` and `payload` included).

### Keyboard (calendar elements)

Arrow keys move by day / week (crossing months), `Home` / `End` jump to the week edges,
`PageUp` / `PageDown` change month, `Enter` / `Space` select. Wheel columns respond to `ArrowUp` /
`ArrowDown`.

### Theming

The `--ndp-*` custom properties inherit, so set them on one element or on `:root` to theme the whole
page:

```css
:root {
  --ndp-accent: #d6336c;
  --ndp-today-ring: #d6336c;
  --ndp-radius: 16px;
  --ndp-font: 'Inter', sans-serif;
}
```

Also available: `--ndp-bg`, `--ndp-text`, `--ndp-muted`, `--ndp-on-accent`, `--ndp-hover`,
`--ndp-in-range`, `--ndp-border`, `--ndp-error`, and for marked days `--ndp-weekly-off`,
`--ndp-holiday-public`, `--ndp-holiday-religious`, `--ndp-holiday-regional`,
`--ndp-holiday-observance`, `--ndp-holiday-container`. Each element documents the subset it actually reads
in the bundled `custom-elements.json`, which editors with custom-element support (WebStorm, or VS
Code with the Lit plugin) use for per-tag completion of attributes, events and these properties.

## Framework usage

No wrapper package is needed for any framework. React is the only one with anything to know.

### React and Next.js

Import the types entry once anywhere in the project to type the tags in TSX. It ships no runtime code:

```tsx
import '@nepali-date-picker/web-component';
import '@nepali-date-picker/web-component/react';

export function Picker() {
  return (
    <nepali-date-picker
      value="2081-05-24"
      showEnglish
      onChange={(event) => console.log(event.nativeEvent.detail.bsIso)}
    />
  );
}
```

React hands `onChange` a synthetic event, so the payload is at `event.nativeEvent.detail`, not
`event.detail`. React also forwards only the events in its own synthetic set, so `invalid` and
`cancel` never reach a prop; reach them with a ref and `addEventListener`. On React 18 and earlier
every prop is written as a string attribute, so set non-string properties through that same ref.

In the Next.js App Router, add `'use client'` to the file that renders the elements.

### Vue 3

```js
// vite.config.js - tell Vue these tags are custom elements
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

### Angular

Add `CUSTOM_ELEMENTS_SCHEMA`, `import '@nepali-date-picker/web-component';` once, then
`<nepali-date-picker [attr.value]="value" (change)="onChange($event)">`.

### Svelte and SvelteKit

Nothing to configure, on either version:

```svelte
<script>import '@nepali-date-picker/web-component';</script>

<!-- Svelte 5 -->
<nepali-date-picker value="2081-05-24" onchange={(e) => console.log(e.detail)} />

<!-- Svelte 4 -->
<nepali-date-picker value="2081-05-24" on:change={(e) => console.log(e.detail)} />
```

### jQuery

```js
$('#picker')[0].value = '2081-05-24';
$('#picker').on('change', (event) => console.log(event.detail.bsIso));
```

## Full documentation

Every attribute, event, theming hook and engine function, plus server rendering notes:
**[README-js.md](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/README-js.md)**.

## License

MPL-2.0 © Shiva Thapa ([@shivathapaa](https://github.com/shivathapaa))
