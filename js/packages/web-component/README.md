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

To pull in only one element, import its subpath: `import '@nepali-date-picker/web-component/nepali-date-field';`.

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

- `change` - single-date elements emit [`NepaliDatePickerChangeDetail`](./src/types.ts) (`bs`, `ad`,
  `bsIso`, `adIso`, `formatted`); range elements emit `NepaliDateRangeChangeDetail` (`start`, `end`,
  `startBsIso`, `endBsIso`, `startAdIso`, `endAdIso`). Bubbles and crosses the shadow boundary.
- `invalid` - `<nepali-date-field>` emits `{ message }` when the typed date is rejected.
- `cancel` - `<nepali-date-picker-dialog>` emits when dismissed without confirming.

```js
picker.addEventListener('change', (e) => console.log(e.detail.bsIso, e.detail.adIso));
```

### Keyboard (calendar elements)

Arrow keys move by day / week (crossing months), `Home` / `End` jump to the week edges,
`PageUp` / `PageDown` change month, `Enter` / `Space` select. Wheel columns respond to `ArrowUp` /
`ArrowDown`.

### Theming

Set CSS custom properties on the element (they inherit through the shadow boundary):

```css
nepali-date-picker,
nepali-date-range-picker {
  --ndp-accent: #d6336c;
  --ndp-today-ring: #d6336c;
  --ndp-radius: 16px;
  --ndp-font: 'Inter', sans-serif;
}
```

Also available: `--ndp-bg`, `--ndp-text`, `--ndp-muted`, `--ndp-on-accent`, `--ndp-hover`,
`--ndp-in-range`, `--ndp-border`, `--ndp-error`.

## Framework usage

### React (19+)

```tsx
import '@nepali-date-picker/web-component';

export function Picker() {
  return <nepali-date-picker value="2081-05-24" onChange={(e: CustomEvent) => console.log(e.detail)} />;
}
```

React 19 sets custom-element properties and forwards events natively. On React 18 or earlier, set
non-string props and attach the `change` listener through a `ref`.

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

### Svelte

```svelte
<script>import '@nepali-date-picker/web-component';</script>
<nepali-date-picker value="2081-05-24" on:change={(e) => console.log(e.detail)} />
```

## Development

From the `js/` workspace root:

```bash
npm run build          # compile the engine and build this package
npm run test           # unit tests
```

The live showcase of every variant lives in `sample/jsApp` (run `npm install && npm run dev`
there after building the packages).

## License

MPL-2.0 © Shiva Thapa ([@shivathapaa](https://github.com/shivathapaa))
