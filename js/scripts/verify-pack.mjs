// Pre-publish smoke test: pack both packages into tarballs, install them into a throwaway
// consumer, and prove the shipped artifact works.

import { execFileSync } from 'node:child_process';
import { mkdtempSync, writeFileSync, rmSync, existsSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const jsRoot = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const coreDir = join(jsRoot, 'packages', 'core');
const wcDir = join(jsRoot, 'packages', 'web-component');

const coreDist = join(coreDir, 'dist', 'NepaliDatePickerKmp-nepali-date-picker-core.mjs');
const wcDist = join(wcDir, 'dist', 'index.js');
const wcBundle = join(wcDir, 'dist', 'nepali-date-picker.bundled.js');
const wcReactTypes = join(wcDir, 'dist', 'react.d.ts');
if (!existsSync(coreDist) || !existsSync(wcDist) || !existsSync(wcBundle) || !existsSync(wcReactTypes)) {
  console.error('Missing build output. Run `npm run build` (from js/) before verify:pack.');
  process.exit(1);
}

const cleanup = [];
function run(cmd, args, cwd) {
  execFileSync(cmd, args, { cwd, stdio: 'inherit' });
}
function pack(dir, dest) {
  const out = execFileSync('npm', ['pack', '--json', '--pack-destination', dest], {
    cwd: dir,
    encoding: 'utf8',
  });
  // npm 11 reports an array of packed entries, npm 12 an object keyed by package name. Reading only
  // one of those shapes fails inside the publish workflow, which is the single place that installs a
  // newer npm than the one Node ships with.
  const parsed = JSON.parse(out);
  const entry = Array.isArray(parsed) ? parsed[0] : Object.values(parsed)[0];
  if (!entry?.filename) {
    throw new Error(`npm pack --json returned an unrecognised shape: ${out.slice(0, 200)}`);
  }
  return join(dest, entry.filename);
}

try {
  const packDest = mkdtempSync(join(tmpdir(), 'ndp-pack-'));
  cleanup.push(packDest);
  const coreTgz = pack(coreDir, packDest);
  const wcTgz = pack(wcDir, packDest);
  console.log(`packed:\n  ${coreTgz}\n  ${wcTgz}`);

  const consumer = mkdtempSync(join(tmpdir(), 'ndp-consumer-'));
  cleanup.push(consumer);

  // core is depended on by web-component at its registry version, which is not published during a
  // first-ever release. The override forces that transitive dependency to the local tarball too, so
  // npm never reaches the registry for it.
  writeFileSync(
    join(consumer, 'package.json'),
    JSON.stringify(
      {
        name: 'ndp-verify-consumer',
        version: '0.0.0',
        private: true,
        type: 'module',
        dependencies: {
          '@nepali-date-picker/core': `file:${coreTgz}`,
          '@nepali-date-picker/web-component': `file:${wcTgz}`,
        },
        overrides: { '@nepali-date-picker/core': `file:${coreTgz}` },
        devDependencies: { vite: '^8.0.0', jsdom: '^30.0.0' },
      },
      null,
      2,
    ),
  );

  // Runtime check of the headless core: a round-trip conversion is deterministic without hardcoding
  // the BS tables, and today's date must land inside the advertised year range.
  writeFileSync(
    join(consumer, 'core-check.mjs'),
    `import { convertAdToBs, convertBsToAd, getTodayBs, getBsYearRange } from '@nepali-date-picker/core';
const bs = convertAdToBs(2024, 1, 15);
if (![bs.year, bs.month, bs.dayOfMonth].every(Number.isInteger) || bs.era !== 2) {
  throw new Error('convertAdToBs returned an unexpected shape: ' + JSON.stringify(bs));
}
const back = convertBsToAd(bs.year, bs.month, bs.dayOfMonth);
if (back.year !== 2024 || back.month !== 1 || back.dayOfMonth !== 15) {
  throw new Error('AD->BS->AD round-trip drifted: ' + JSON.stringify(back));
}
const today = getTodayBs();
const range = getBsYearRange();
if (today.year < range.first || today.year > range.last) {
  throw new Error('getTodayBs() out of range: ' + today.year);
}
console.log('core runtime OK (round-trip 2024-01-15, today BS ' + today.year + ')');
`,
  );

  // Bundler check of the web component: importing the entry pulls in every element module, so vite
  // build fails if the exports map or any dist file the barrel references is missing. Build time
  // only bundles, it never executes the DOM registration, so no jsdom is needed.
  writeFileSync(
    join(consumer, 'index.html'),
    '<!doctype html><meta charset="utf-8"><script type="module" src="./entry.js"></script>\n',
  );
  writeFileSync(
    join(consumer, 'entry.js'),
    `import '@nepali-date-picker/web-component';
import '@nepali-date-picker/web-component/react';
import { NepaliDatePicker } from '@nepali-date-picker/web-component';
if (typeof NepaliDatePicker !== 'function') {
  throw new Error('NepaliDatePicker export is not a class');
}
`,
  );

  // The CDN bundle is the one artifact a browser loads without a resolver, so it is checked for the
  // two properties that would make it useless there: a leftover bare import, and an element that
  // never registers. jsdom stands in for the browser because the bundle inlines Lit's browser build
  // and so needs real DOM globals, not Lit's server shim.
  writeFileSync(
    join(consumer, 'bundle-check.mjs'),
    `import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { JSDOM } from 'jsdom';

const tags = [
  'nepali-date-picker',
  'nepali-date-range-picker',
  'nepali-date-picker-dialog',
  'nepali-date-picker-docked',
  'nepali-date-field',
  'nepali-date-range-field',
  'nepali-wheel-date-picker',
];

// Resolved through the exports map on the ESM condition, so a broken './bundle' entry fails here
// rather than in a consumer's browser.
//
// The capture has to look like a module specifier to count. Minified prose reaches the bundle too
// ("...-day scan from " + x + "..."), and a message that happens to end in \`from\` is not an import
// left behind. Kept in step with the same check in test/bundle.test.ts.
const bundleUrl = import.meta.resolve('@nepali-date-picker/web-component/bundle');
const source = readFileSync(fileURLToPath(bundleUrl), 'utf8');
const specifier = /(?:\\bfrom\\s*|\\bimport\\s*)["']([^"'.\\/][^"']*)["']/g;
const bareImport = Array.from(source.matchAll(specifier))
  .map((match) => match[1])
  .find((candidate) => /^@?[a-zA-Z][\\w@\\/.-]*$/.test(candidate));
if (bareImport) {
  throw new Error('bundle still imports the bare specifier ' + bareImport + '; a CDN cannot resolve it');
}

// Every DOM global jsdom offers is installed, not a hand-picked list: Lit touches Document,
// CSSStyleSheet and friends at module scope, and Node's own Event / CustomEvent classes are not
// interchangeable with jsdom's, so jsdom has to win.
const dom = new JSDOM('<!doctype html><nepali-date-picker></nepali-date-picker>');
for (const key of Object.getOwnPropertyNames(dom.window)) {
  if (key === 'undefined' || key === 'globalThis' || key === 'navigator') continue;
  try {
    globalThis[key] = dom.window[key];
  } catch {
    // Read-only globals such as \`window\` itself are already correct or unused here.
  }
}
Object.defineProperty(globalThis, 'navigator', { value: dom.window.navigator, configurable: true });

await import(bundleUrl);

const missing = tags.filter((tag) => !dom.window.customElements.get(tag));
if (missing.length > 0) {
  throw new Error('bundle registered no element for: ' + missing.join(', '));
}
const el = dom.window.document.querySelector('nepali-date-picker');
if (el.constructor === dom.window.HTMLElement) {
  throw new Error('the existing <nepali-date-picker> tag was never upgraded');
}
console.log('bundle OK (' + tags.length + ' elements registered, no bare imports)');
`,
  );

  run('npm', ['install', '--no-audit', '--no-fund'], consumer);
  run('node', ['core-check.mjs'], consumer);
  run('node', ['bundle-check.mjs'], consumer);
  run('npx', ['vite', 'build', '--logLevel', 'warn'], consumer);
  if (!existsSync(join(consumer, 'dist', 'index.html'))) {
    throw new Error('vite build produced no output; web-component did not resolve');
  }

  console.log('\nverify:pack PASS - both tarballs install and work in a fresh consumer.');
} catch (err) {
  console.error('\nverify:pack FAIL:', err.message);
  process.exitCode = 1;
} finally {
  for (const dir of cleanup) rmSync(dir, { recursive: true, force: true });
}
