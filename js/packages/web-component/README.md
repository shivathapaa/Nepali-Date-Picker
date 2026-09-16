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
of the payload, so this saves about 3 kB gzipped rather than a lot.

Without a bundler, load the self-contained build from a CDN (232 kB minified, 64 kB gzipped, all
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
| `<nepali-date-picker-dialog>` | Modal calendar with confirm / cancel (add `fullscreen`). |
| `<nepali-date-picker-docked>` | Text field with an anchored calendar popover. |
| `<nepali-date-range-picker>` | Start / end range selection in the calendar. |
| `<nepali-date-field>` | Typed text field with inline validation, no calendar. |
| `<nepali-date-range-field>` | Two validated fields for a start / end range. |
| `<nepali-wheel-date-picker>` | Scrolling year / month / day wheels. |

### Common attributes

All date values are Bikram Sambat `YYYY-MM-DD` strings; Devanagari digits are accepted on input.

| Attribute | Type | Applies to | Description |
| --- | --- | --- | --- |
| `value` | `string` | all single-date elements | Selected date. |
| `start` / `end` | `string` | range elements | Selected range ends. |
| `language` | `"en"` \| `"ne"` | all | Text language and digit script. |
| `min` / `max` | `string` | all | Selectable bounds. |
| `disabled` | `boolean` | all | Read-only, dimmed. |
| `show-english` | `boolean` | calendar elements | Show the Gregorian equivalent. |
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
`--ndp-in-range`, `--ndp-border`, `--ndp-error`. Each element documents the subset it actually reads
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
// vite.config.js
vue({ template: { compilerOptions: { isCustomElement: (t) => t === 'nepali-date-picker' } } });
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
