# `jsApp` - the web showcase

A Vite site that demonstrates the two npm packages, `@nepali-date-picker/web-component` and
`@nepali-date-picker/core`: all seven custom elements, theming, locale switching, form integration,
and the headless conversion engine on its own.

This is the same site deployed to the live
[`/demo/`](https://shivathapaa.github.io/Nepali-Date-Picker/demo/) page by the Pages workflow.

Elements on show:

```
<nepali-date-picker>          <nepali-date-picker-dialog>   <nepali-date-picker-docked>
<nepali-date-range-picker>    <nepali-date-field>           <nepali-date-range-field>
<nepali-wheel-date-picker>
```

Two cards cover the display options every calendar element takes: `calendar-system` /
`show-calendar-toggle` for showing the Gregorian calendar, and `show-adjacent-month-days` for filling
the grid's empty cells with the neighbouring months. Both leave the reported value in Bikram Sambat,
which the event log under the showcase makes visible.

## Layout

```
jsApp/
├── index.html      The showcase markup, one tab panel per element group
├── src/main.ts     Tab switching, locale sync, event logging, the engine demo
├── styles.css
└── vite.config.ts  Aliases the packages to their built dist
```

## How it consumes the packages

`vite.config.ts` aliases the bare package specifiers to the **built output** in `js/packages/*/dist`
rather than to source, so the sample loads exactly what npm would ship:

```
@nepali-date-picker/core            -> js/packages/core/dist/NepaliDatePickerKmp-nepali-date-picker-core.mjs
@nepali-date-picker/web-component   -> js/packages/web-component/dist/index.js
```

That output does not exist until the packages are built, so build first, then run the sample. The
`base` is relative so the built site works both from a domain root and nested under `/demo`.

## Running

```bash
# 1. build the engine and the web component, from the repository root
cd js
npm install          # first time only
npm run build

# 2. run the showcase
cd ../sample/jsApp
npm install          # first time only
npm run dev
```

Vite prints a URL, by default `http://localhost:5173/`. Rebuild the packages (`cd js && npm run
build`) after changing library source, then the sample picks the change up.

Production build:

```bash
npm run build        # static site into sample/jsApp/dist/
npm run preview      # serve that build locally
```

Requires **Node.js 18 or newer**.

## Related check

The showcase links the packages from source, so it cannot catch a broken `exports` map, a missing
`files` entry or an incomplete `dist`. `npm run verify:pack` in `js/` closes that gap: it packs both
packages into tarballs, installs them into a throwaway consumer, round-trips a conversion and
bundles the web component, exactly as a real `npm install` would. It is a required gate in the
publish workflow.
