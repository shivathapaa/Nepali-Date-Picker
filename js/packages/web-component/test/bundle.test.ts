/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

// Checks the two build artifacts a consumer receives but no other test touches: the single-file CDN
// bundle and the generated React typings. They live in dist, so this file must not import anything
// from src, or the elements would already be registered before the bundle got a chance to.

import { describe, it, expect, beforeAll } from 'vitest';
import { existsSync, readFileSync, statSync } from 'node:fs';
import { resolve } from 'node:path';
import { pathToFileURL } from 'node:url';
import { gzipSync } from 'node:zlib';

const bundlePath = resolve(process.cwd(), 'dist', 'nepali-date-picker.bundled.js');
const reactTypesPath = resolve(process.cwd(), 'dist', 'react.d.ts');
const built = existsSync(bundlePath) && existsSync(reactTypesPath);

if (!built) {
  console.warn('dist is missing, skipping the build-artifact tests. Run `npm run build` to include them.');
}

const tags = [
  'nepali-date-picker',
  'nepali-date-range-picker',
  'nepali-date-picker-dialog',
  'nepali-date-picker-docked',
  'nepali-date-field',
  'nepali-date-range-field',
  'nepali-wheel-date-picker',
];

describe.skipIf(!built)('the CDN bundle', () => {
  let source = '';

  beforeAll(async () => {
    source = readFileSync(bundlePath, 'utf8');
    await import(pathToFileURL(bundlePath).href);
  });

  it('resolves nothing at runtime', () => {
    // A bare specifier left in the output is exactly what makes a file unusable from a plain CDN
    // URL, and it is invisible in a bundler-based test.
    const bare = /(?:\bfrom\s*|\bimport\s*)["']([^"'./][^"']*)["']/.exec(source);
    expect(bare?.[1] ?? null).toBeNull();
  });

  it.each(tags)('registers %s', (tag) => {
    expect(customElements.get(tag)).toBeTypeOf('function');
  });

  it('upgrades a tag that was already in the document', async () => {
    document.body.innerHTML = '<nepali-date-picker value="2081-05-24"></nepali-date-picker>';
    const el = document.querySelector('nepali-date-picker')!;
    await (el as unknown as { updateComplete: Promise<unknown> }).updateComplete;
    expect(el.constructor).not.toBe(HTMLElement);
    expect(el.shadowRoot?.querySelector('.surface')).toBeTruthy();
  });

  it('stays within its size budget', () => {
    // The whole point of this file is being downloaded, so a doubling should fail the build rather
    // than quietly ship. Roughly 79% of it is the shared Kotlin engine and does not shrink.
    const gzipped = gzipSync(readFileSync(bundlePath), { level: 9 }).length;
    expect(gzipped, `bundle is ${(gzipped / 1024).toFixed(1)} kB gzipped`).toBeLessThan(80 * 1024);
    expect(statSync(bundlePath).size).toBeLessThan(300 * 1024);
  });
});

describe.skipIf(!built)('the generated React typings', () => {
  it.each(tags)('declares %s', (tag) => {
    expect(readFileSync(reactTypesPath, 'utf8')).toContain(`'${tag}':`);
  });

  it('exposes the payload only through nativeEvent', () => {
    // React delivers `change` as a synthetic event, so typing the handler with a plain CustomEvent
    // would compile and then hand the user an undefined `detail` at runtime.
    const source = readFileSync(reactTypesPath, 'utf8');
    expect(source).toContain('SyntheticEvent<El, CustomEvent<Detail>>');
    expect(source).not.toMatch(/onInvalid\?:|onCancel\?:/);
  });
});
