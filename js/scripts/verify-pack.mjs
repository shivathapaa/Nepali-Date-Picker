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
if (!existsSync(coreDist) || !existsSync(wcDist)) {
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
  return join(dest, JSON.parse(out)[0].filename);
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
        devDependencies: { vite: '^5.4.0' },
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
import { NepaliDatePicker } from '@nepali-date-picker/web-component';
if (typeof NepaliDatePicker !== 'function') {
  throw new Error('NepaliDatePicker export is not a class');
}
`,
  );

  run('npm', ['install', '--no-audit', '--no-fund'], consumer);
  run('node', ['core-check.mjs'], consumer);
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
