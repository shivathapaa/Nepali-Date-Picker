/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

// Builds the single-file CDN bundle. The library build in vite.config.ts externalizes `lit` and the
// conversion engine so consumer bundlers can dedupe them; a browser loading the package straight
// from a CDN has no resolver for those bare specifiers, so this pass inlines everything.
//
// The output is ESM rather than IIFE or UMD on purpose: every browser that implements custom
// elements v1 and shadow DOM also supports module scripts, apart from Chrome 54 to 60 from 2016,
// which cannot render these elements anyway. A global build would add weight for nobody.
//
// esbuild rather than a second Vite config: for this input it renames across the whole bundle where
// Rollup cannot, which is 20% smaller raw and 6% smaller over the wire, on the one artifact whose
// entire purpose is being downloaded.
//
// The entry is the built dist/index.js, not src/index.ts, for two reasons: the CDN file is then the
// same code consumers get from the registry rather than a second compile of the sources, and the
// `sideEffects` paths in package.json are declared against dist, so bundling src would let the bare
// element imports be dropped as dead code.

import { build } from 'esbuild';
import { gzipSync } from 'node:zlib';
import { readFileSync } from 'node:fs';
import { join, resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const packageRoot = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const outfile = join(packageRoot, 'dist', 'nepali-date-picker.bundled.js');
const { version } = JSON.parse(readFileSync(join(packageRoot, 'package.json'), 'utf8'));

await build({
  entryPoints: [join(packageRoot, 'dist', 'index.js')],
  outfile,
  bundle: true,
  format: 'esm',
  target: 'es2021',
  minify: true,
  sourcemap: false,
  // Keeps the bundled dependencies' license headers, which minification would otherwise drop.
  legalComments: 'inline',
  banner: {
    js: `/*! @nepali-date-picker/web-component ${version} | MPL-2.0 | https://github.com/shivathapaa/Nepali-Date-Picker */`,
  },
});

const bytes = readFileSync(outfile);
const kb = (n) => `${(n / 1024).toFixed(1)} kB`;
console.log(`nepali-date-picker.bundled.js  ${kb(bytes.length)} │ gzip: ${kb(gzipSync(bytes, { level: 9 }).length)}`);
