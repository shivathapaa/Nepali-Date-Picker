# Nepali Date Picker for JavaScript & the web

Bikram Sambat (Nepali) date tools for the web, generated from the same Kotlin `:core` engine as the
Android and Python builds, so conversions match everywhere.

| Package | Install | What it is |
| --- | --- | --- |
| [`@nepali-date-picker/core`](https://www.npmjs.com/package/@nepali-date-picker/core) | `npm i @nepali-date-picker/core` | BS↔AD conversion + formatting engine for Node and the browser. Zero dependencies, TypeScript types. |
| [`@nepali-date-picker/web-component`](https://www.npmjs.com/package/@nepali-date-picker/web-component) | `npm i @nepali-date-picker/web-component` | `<nepali-date-picker>` element suite (inline, dialog, docked, range, field, wheel) for React, Vue, Angular, Svelte, and plain HTML. |

**[▶ Live demo](https://shivathapaa.github.io/Nepali-Date-Picker/demo/)** - every variant and use-case.

This page is a quick start. The complete web guide, every element, attribute, event, theming hook
and engine function, is **[README-js.md](../README-js.md)**. To run the showcase locally, see
[`sample/jsApp`](../sample/jsApp).

## Picker UI

```html
<script type="module">
  import '@nepali-date-picker/web-component';
</script>

<nepali-date-picker value="2081-05-24" language="ne"></nepali-date-picker>
<script>
  document.querySelector('nepali-date-picker')
    .addEventListener('change', (e) => console.log(e.detail)); // { bs, ad, bsIso, adIso, formatted }
</script>
```

Every calendar element can display the Gregorian calendar instead, with `calendar-system="ad"` or a
`B.S.` / `A.D.` switch the user can flip, and can fill the grid's empty cells with the neighbouring
months' days. The value stays Bikram Sambat either way:

```html
<nepali-date-picker
  value="2083-06-01"
  show-calendar-toggle
  show-adjacent-month-days
></nepali-date-picker>
```

No bundler? Swap that import for the self-contained build (82 kB gzipped, all seven elements):
`<script type="module" src="https://cdn.jsdelivr.net/npm/@nepali-date-picker/web-component"></script>`.

Works in any framework (custom elements are standard DOM). Full attribute / event / theming reference
and framework snippets: **[web-component README](packages/web-component/README.md)**.

## Conversion engine (no UI)

```ts
import { convertAdToBs, convertBsToAd, getTodayBs, formatBsDateByPattern } from '@nepali-date-picker/core';

convertAdToBs(2024, 9, 9);   // { year: 2081, month: 5, dayOfMonth: 24, ... }
getTodayBs();                // today in Asia/Kathmandu, Bikram Sambat
formatBsDateByPattern('yyyy-MM-dd EEEE', 2081, 5, 24, 'ne'); // "२०८१-०५-२४ सोमबार"
```

Full API: **[core README](packages/core/README.md)**.

## License

MPL-2.0 © Shiva Thapa ([@shivathapaa](https://github.com/shivathapaa))
