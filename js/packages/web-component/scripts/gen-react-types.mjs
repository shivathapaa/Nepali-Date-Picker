/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

// Emits dist/react.d.ts (the JSX typings) and dist/react.js (an empty module so a single
// side-effect import pulls the typings in) from custom-elements.json, so the element contract is
// described in exactly one place.

import { readFileSync, writeFileSync, existsSync } from 'node:fs';
import { join, resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const packageRoot = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const manifestPath = join(packageRoot, 'custom-elements.json');
const distDir = join(packageRoot, 'dist');

// Verified against React 19.3.0: only events already in React's delegated synthetic set reach an
// `on*` prop on a custom element. Arbitrary and non-delegated events (`invalid`, `cancel`) never
// fire there no matter how they are dispatched, so generating props for them would ship dead API
// that type-checks and silently does nothing.
const reactDeliveredEvents = new Set(['change', 'input', 'click', 'focus', 'blur']);

// Types that come from the DOM lib rather than the package's own type module.
const builtinTypes = new Set(['string', 'number', 'boolean']);

if (!existsSync(manifestPath)) {
  console.error('custom-elements.json is missing. Run `npm run analyze` first.');
  process.exit(1);
}
if (!existsSync(distDir)) {
  console.error('dist/ is missing. Run the library build before generating the React typings.');
  process.exit(1);
}

const manifest = JSON.parse(readFileSync(manifestPath, 'utf8'));
const elements = manifest.modules
  .flatMap((module) => module.declarations ?? [])
  .filter((declaration) => declaration.tagName)
  .sort((a, b) => a.tagName.localeCompare(b.tagName));

if (elements.length === 0) {
  console.error('custom-elements.json describes no custom elements.');
  process.exit(1);
}

const detailOf = (event) => {
  const match = /^CustomEvent<(.+)>$/.exec(event.type?.text ?? '');
  return match ? match[1] : null;
};

const propertyTypes = new Set();
const detailTypes = new Set();
for (const element of elements) {
  for (const member of element.members ?? []) {
    if (member.kind === 'field' && member.type?.text && !builtinTypes.has(member.type.text)) {
      propertyTypes.add(member.type.text);
    }
  }
  for (const event of element.events ?? []) {
    if (!reactDeliveredEvents.has(event.name)) continue;
    const detail = detailOf(event);
    if (detail) detailTypes.add(detail);
  }
}

const propsName = (element) => `${element.name}Props`;

function renderElement(element) {
  const fields = (element.members ?? []).filter((member) => member.kind === 'field');
  const delivered = (element.events ?? []).filter((event) => reactDeliveredEvents.has(event.name));
  const undelivered = (element.events ?? []).filter((event) => !reactDeliveredEvents.has(event.name));

  const lines = [];
  lines.push('/**');
  lines.push(` * Props accepted by \`<${element.tagName}>\` in JSX.`);
  if (undelivered.length > 0) {
    const names = undelivered.map((event) => `\`${event.name}\``).join(' and ');
    const pronoun = undelivered.length === 1 ? 'it' : 'them';
    lines.push(' *');
    lines.push(` * React never delivers ${names} to an \`on\`-prefixed prop on a custom element, so`);
    lines.push(` * subscribe to ${pronoun} through a \`ref\` and \`addEventListener\` instead.`);
  }
  lines.push(' */');
  lines.push(`export interface ${propsName(element)} extends ElementProps<${element.name}> {`);

  for (const field of fields) {
    const attribute = (element.attributes ?? []).find((entry) => entry.fieldName === field.name);
    const description = field.description ?? attribute?.description;
    if (description) lines.push(`  /** ${description.replace(/\s+/g, ' ')} */`);
    lines.push(`  ${field.name}?: ${field.type?.text ?? 'string'};`);
  }

  for (const event of delivered) {
    const detail = detailOf(event);
    lines.push(
      `  /** Fires on the \`${event.name}\` event. The payload is \`event.nativeEvent.detail\`. */`,
    );
    lines.push(
      `  on${event.name[0].toUpperCase()}${event.name.slice(1)}?: CustomEventHandler<${element.name}, ${detail ?? 'unknown'}>;`,
    );
  }

  lines.push('}');
  return lines.join('\n');
}

const typeImports = [...propertyTypes, ...detailTypes].sort();
const classImports = elements.map((element) => element.name).sort();

const output = `/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

// Generated from custom-elements.json by scripts/gen-react-types.mjs. Do not edit by hand.
//
// Import this module once anywhere in a React project to teach TSX about the elements:
//
//   import '@nepali-date-picker/web-component/react';
//
// Props are declared under their property names. The dash-cased attribute forms
// (\`show-english\`, \`start-label\`) also work at runtime, but the property form avoids the
// string coercion an attribute goes through.

import type { DetailedHTMLProps, HTMLAttributes, SyntheticEvent } from 'react';
import type {
${classImports.map((name) => `  ${name},`).join('\n')}
} from './index.js';
import type {
${typeImports.map((name) => `  ${name},`).join('\n')}
} from './types.js';

// React's own \`onChange\` on \`HTMLAttributes\` is a \`FormEventHandler\`, which is not what these
// elements dispatch, so it is removed before the element props widen it.
type ElementProps<El> = Omit<DetailedHTMLProps<HTMLAttributes<El>, El>, 'onChange'>;

/**
 * Handler for an event React delivers through its synthetic system. React wraps the dispatched
 * \`CustomEvent\`, so the payload lives on \`event.nativeEvent.detail\` rather than \`event.detail\`.
 */
type CustomEventHandler<El, Detail> = (event: SyntheticEvent<El, CustomEvent<Detail>>) => void;

${elements.map(renderElement).join('\n\n')}

interface NepaliDatePickerIntrinsicElements {
${elements.map((element) => `  '${element.tagName}': ${propsName(element)};`).join('\n')}
}

// React 19 moved the JSX namespace into the \`react\` module; React 18 and earlier keep it global.
// Declaring both keeps one generated file correct on either version.
declare module 'react' {
  namespace JSX {
    interface IntrinsicElements extends NepaliDatePickerIntrinsicElements {}
  }
}

declare global {
  namespace JSX {
    interface IntrinsicElements extends NepaliDatePickerIntrinsicElements {}
  }
}
`;

writeFileSync(join(distDir, 'react.d.ts'), output);
writeFileSync(
  join(distDir, 'react.js'),
  '// Type-only entry point. The declarations live in react.d.ts; importing this module is what\n' +
    '// makes TypeScript load them.\nexport {};\n',
);

console.log(`react.d.ts: ${elements.length} elements, ${typeImports.length} imported types`);
