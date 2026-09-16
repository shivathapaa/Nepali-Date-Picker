/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

// custom-elements.json is committed and is what the published React typings are generated from, so
// it is only trustworthy if it still matches the elements. These tests compare it against the live
// classes rather than against a snapshot of itself.

import { describe, it, expect } from 'vitest';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import type { CSSResult, CSSResultGroup } from 'lit';
import type { ReactiveElement } from 'lit';
import '../src/index.js';
import {
  NepaliDateField,
  NepaliDatePicker,
  NepaliDatePickerDialog,
  NepaliDatePickerDocked,
  NepaliDateRangeField,
  NepaliDateRangePicker,
  NepaliWheelDatePicker,
} from '../src/index.js';
import { tokens } from '../src/internal/styles.js';

interface ManifestAttribute {
  name: string;
  fieldName?: string;
}
interface ManifestEvent {
  name: string;
  type?: { text?: string };
}
interface ManifestDeclaration {
  tagName?: string;
  name: string;
  attributes?: ManifestAttribute[];
  events?: ManifestEvent[];
  cssProperties?: { name: string }[];
}

// Resolved from the working directory rather than import.meta.url: under the jsdom environment
// that URL is an http one, which node:fs cannot read.
const manifest = JSON.parse(
  readFileSync(resolve(process.cwd(), 'custom-elements.json'), 'utf8'),
) as { modules: { declarations?: ManifestDeclaration[] }[] };

const declarations = new Map(
  manifest.modules
    .flatMap((module) => module.declarations ?? [])
    .filter((declaration): declaration is ManifestDeclaration & { tagName: string } =>
      Boolean(declaration.tagName),
    )
    .map((declaration) => [declaration.tagName, declaration]),
);

const elements: [string, typeof ReactiveElement][] = [
  ['nepali-date-picker', NepaliDatePicker],
  ['nepali-date-range-picker', NepaliDateRangePicker],
  ['nepali-date-picker-dialog', NepaliDatePickerDialog],
  ['nepali-date-picker-docked', NepaliDatePickerDocked],
  ['nepali-date-field', NepaliDateField],
  ['nepali-date-range-field', NepaliDateRangeField],
  ['nepali-wheel-date-picker', NepaliWheelDatePicker],
];

/** Mirrors Lit's own property-to-attribute naming so the expectation is derived, not hardcoded. */
function observedAttributes(element: typeof ReactiveElement): string[] {
  const names: string[] = [];
  for (const [property, options] of element.elementProperties) {
    if (options.attribute === false) continue;
    names.push(typeof options.attribute === 'string' ? options.attribute : String(property).toLowerCase());
  }
  return names.sort();
}

function flatten(styles: CSSResultGroup | undefined): CSSResult[] {
  if (!styles) return [];
  return Array.isArray(styles) ? styles.flatMap((entry) => flatten(entry as CSSResultGroup)) : [styles as CSSResult];
}

describe('custom-elements.json', () => {
  it('describes every registered element and nothing else', () => {
    expect([...declarations.keys()].sort()).toEqual(elements.map(([tag]) => tag).sort());
  });

  it.each(elements)('%s attributes match the element contract', (tag, element) => {
    const documented = (declarations.get(tag)?.attributes ?? []).map((a) => a.name).sort();
    expect(documented).toEqual(observedAttributes(element));
  });

  it.each(elements)('%s leaks no internal reactive state', (tag) => {
    const declaration = declarations.get(tag)!;
    const leaked = [
      ...(declaration.attributes ?? []).map((a) => a.name),
      ...(declaration.attributes ?? []).map((a) => a.fieldName ?? ''),
    ].filter((name) => name.startsWith('_'));
    expect(leaked).toEqual([]);
  });

  it('types every event payload it documents', () => {
    for (const declaration of declarations.values()) {
      for (const event of declaration.events ?? []) {
        expect(event.type?.text, `${declaration.tagName} ${event.name}`).toMatch(/^CustomEvent(<.+>)?$/);
      }
    }
  });

  it.each(elements)('%s documents only custom properties its styles read', (tag, element) => {
    const css = flatten(element.styles).map((style) => style.cssText).join('\n');
    const read = new Set([...css.matchAll(/var\(--_?ndp-([a-z-]+)/g)].map((match) => `--ndp-${match[1]}`));
    const documented = (declarations.get(tag)?.cssProperties ?? []).map((property) => property.name);
    expect(documented.length, `${tag} documents no custom properties`).toBeGreaterThan(0);
    expect(documented.filter((name) => !read.has(name))).toEqual([]);
  });
});

describe('theming', () => {
  it.each(elements)('%s takes its token defaults from the shared block', (_tag, element) => {
    const styles = flatten(element.styles);
    expect(styles).toContain(tokens);

    // A `--ndp-*` default declared in an element's own `:host` block wins over an inherited value,
    // which would silently break page-level theming such as `:root { --ndp-accent: ... }`. Only the
    // shared token block may declare them, and it does so through `var(--ndp-*, fallback)`.
    const own = styles
      .filter((style) => style !== tokens)
      .map((style) => style.cssText)
      .join('\n');
    expect(own.match(/--ndp-[a-z-]+\s*:/g) ?? []).toEqual([]);
  });
});
