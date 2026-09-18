/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

/** How long the arriving calendar takes to settle. Matches the Compose pickers. */
export const CALENDAR_SWITCH_DURATION_MS = 220;

/** Decelerating, so the calendar arrives quickly and comes to rest gently. */
const CALENDAR_SWITCH_EASING = 'cubic-bezier(.05,.7,.1,1)';

/** How small the arriving calendar starts, so it settles into place rather than appearing flat. */
const CALENDAR_SWITCH_INITIAL_SCALE = 0.94;

/**
 * The month header and the day grid: everything a calendar switch rewrites. The `B.S.` / `A.D.`
 * switch itself is left alone, since it is the control that was just pressed, and so is the footer,
 * which does not depend on the calendar.
 */
const SWITCHED_REGIONS = '.header, [role="grid"]';

/**
 * Fades and scales a region that has just been re-rendered with different content back in.
 *
 * Lit swaps the DOM synchronously, so there is no outgoing frame to cross-dissolve with; the content
 * changes in one go and then arrives. That is the same gesture the Compose pickers make, and for the
 * same reason: holding the old calendar on screen means holding a month the new calendar cannot
 * express.
 *
 * Deliberately not tied to the state change. A rapid double-tap just restarts the animation and can
 * never leave the calendar itself mid-switch.
 */
export function fadeInRerenderedRegion(root: ParentNode | null | undefined): void {
  if (!root || prefersReducedMotion()) return;
  const regions = [...root.querySelectorAll<HTMLElement>(SWITCHED_REGIONS)];
  // Older engines, and jsdom under the test runner, have no Web Animations API.
  if (regions.some((region) => typeof region.animate !== 'function')) return;
  for (const region of regions) {
    region.animate(
      [
        { opacity: '0', transform: `scale(${CALENDAR_SWITCH_INITIAL_SCALE})` },
        { opacity: '1', transform: 'scale(1)' },
      ],
      { duration: CALENDAR_SWITCH_DURATION_MS, easing: CALENDAR_SWITCH_EASING },
    );
  }
}

function prefersReducedMotion(): boolean {
  return (
    typeof matchMedia === 'function' && matchMedia('(prefers-reduced-motion: reduce)').matches
  );
}
