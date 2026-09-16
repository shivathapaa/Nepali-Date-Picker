/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

// Compile-only assertions for the generated dist/react.d.ts. `tsc --noEmit` over this file is the
// test: every `@ts-expect-error` below fails the build if the mistake it describes stops being an
// error, which is what keeps the generated props honest.

import '@nepali-date-picker/web-component/react';
import type { NepaliDatePickerChangeDetail } from '@nepali-date-picker/web-component';

export function AllElements() {
  return (
    <>
      <nepali-date-picker
        value="2081-05-24"
        language="ne"
        min="2081-01-01"
        max="2081-12-30"
        disabled={false}
        showEnglish
        onChange={(event) => {
          const detail: NepaliDatePickerChangeDetail = event.nativeEvent.detail;
          console.info(detail.bsIso, detail.adIso, detail.bs.year);
        }}
      />
      <nepali-date-range-picker
        start="2081-05-01"
        end="2081-05-24"
        onChange={(event) => {
          console.info(event.nativeEvent.detail.startBsIso, event.nativeEvent.detail.endBsIso);
        }}
      />
      <nepali-date-picker-dialog open heading="Pick a date" fullscreen />
      <nepali-date-picker-docked label="Joined on" />
      <nepali-date-field label="Date of birth" />
      <nepali-date-range-field startLabel="From" endLabel="To" />
      <nepali-wheel-date-picker value="2081-05-24" />
    </>
  );
}

export function TypeErrors() {
  return (
    <>
      {/* @ts-expect-error `language` only accepts the two supported scripts. */}
      <nepali-date-picker language="fr" />
      {/* @ts-expect-error `showEnglish` is a boolean property, not a string. */}
      <nepali-date-picker showEnglish="yes" />
      {/* @ts-expect-error the wheel picker has no `min` bound. */}
      <nepali-wheel-date-picker min="2081-01-01" />
      {/* @ts-expect-error `_error` is internal state, never part of the element contract. */}
      <nepali-date-field _error="boom" />
      <nepali-date-picker
        onChange={(event) => {
          // @ts-expect-error React wraps the CustomEvent, so the payload is not on the synthetic event.
          console.info(event.detail);
        }}
      />
    </>
  );
}
