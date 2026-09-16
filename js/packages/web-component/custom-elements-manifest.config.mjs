/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

/**
 * The analyzer treats every key of a Lit `static properties` block as a public attribute, and it
 * also picks up assignments like `this.cal.onSelect = ...` as class fields. Neither is part of the
 * element contract, and `custom-elements.json` is the input to the generated React types, so the
 * noise has to be removed before it reaches a published type.
 *
 * The surviving rule is deliberately strict: a field is public API only when it is backed by an
 * observed attribute, which is exactly what a `static properties` entry without `state: true`
 * produces.
 */
function publicContractOnly() {
  const isInternal = (name) => typeof name === 'string' && name.startsWith('_');

  return {
    name: 'nepali-date-picker-public-contract-only',
    packageLinkPhase({ customElementsManifest }) {
      for (const module of customElementsManifest.modules ?? []) {
        for (const declaration of module.declarations ?? []) {
          if (!declaration.tagName) continue;

          declaration.attributes = (declaration.attributes ?? []).filter(
            (attribute) => !isInternal(attribute.name) && !isInternal(attribute.fieldName),
          );

          declaration.members = (declaration.members ?? []).filter((member) => {
            if (member.privacy === 'private' || member.privacy === 'protected') return false;
            if (isInternal(member.name)) return false;
            return member.kind === 'method' || Boolean(member.attribute);
          });
        }
      }
    },
  };
}

export default {
  globs: ['src/*.ts'],
  outdir: '.',
  litelement: true,
  plugins: [publicContractOnly()],
};
