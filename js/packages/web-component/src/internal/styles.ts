/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * see http://mozilla.org/MPL/2.0/
 */

import { css } from 'lit';

/** Themeable design tokens. Every element exposes the same `--ndp-*` custom properties. */
export const tokens = css`
  :host {
    --ndp-font: system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;
    --ndp-bg: #ffffff;
    --ndp-text: #1b1b1f;
    --ndp-muted: #6b6b70;
    --ndp-accent: #2f6fed;
    --ndp-on-accent: #ffffff;
    --ndp-hover: rgba(47, 111, 237, 0.12);
    --ndp-in-range: rgba(47, 111, 237, 0.14);
    --ndp-today-ring: #2f6fed;
    --ndp-border: #d3d4d8;
    --ndp-error: #ba1a1a;
    --ndp-radius: 12px;
    font-family: var(--ndp-font);
    color: var(--ndp-text);
    box-sizing: border-box;
  }
  *,
  *::before,
  *::after {
    box-sizing: border-box;
  }
`;

/** The month calendar (header, weekday row, day grid, footer). Shared by every calendar-based element. */
export const calendarStyles = css`
  .surface {
    background: var(--ndp-bg);
    border-radius: var(--ndp-radius);
    padding: 12px;
    user-select: none;
  }
  .header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }
  .label {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 600;
    font-size: 0.95rem;
  }
  button {
    font: inherit;
    color: inherit;
    background: transparent;
    border: 0;
    cursor: pointer;
    border-radius: 999px;
  }
  select {
    font: inherit;
    color: inherit;
    background: transparent;
    border: 1px solid var(--ndp-border);
    border-radius: 8px;
    padding: 2px 4px;
    cursor: pointer;
  }
  .nav {
    width: 32px;
    height: 32px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 1.1rem;
    line-height: 1;
  }
  .nav:hover:not(:disabled) {
    background: var(--ndp-hover);
  }
  .nav:disabled {
    opacity: 0.35;
    cursor: default;
  }
  [role='grid'] {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 2px;
  }
  .weekday {
    text-align: center;
    font-size: 0.72rem;
    font-weight: 600;
    color: var(--ndp-muted);
    padding: 4px 0;
  }
  .day {
    aspect-ratio: 1;
    min-width: 36px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 0.9rem;
    border-radius: 999px;
    position: relative;
  }
  .day:hover:not([aria-disabled='true']) {
    background: var(--ndp-hover);
  }
  .day:focus-visible {
    outline: 2px solid var(--ndp-accent);
    outline-offset: 1px;
  }
  .day[aria-disabled='true'] {
    color: var(--ndp-muted);
    opacity: 0.4;
    cursor: default;
  }
  .day.today {
    box-shadow: inset 0 0 0 1.5px var(--ndp-today-ring);
  }
  .day.selected,
  .day.range-start,
  .day.range-end {
    background: var(--ndp-accent);
    color: var(--ndp-on-accent);
  }
  .day.in-range:not(.range-start):not(.range-end) {
    background: var(--ndp-in-range);
    border-radius: 0;
  }
  .blank {
    visibility: hidden;
  }
  .footer {
    margin-top: 8px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;
  }
  .english {
    color: var(--ndp-muted);
    font-size: 0.78rem;
  }
  .link {
    padding: 4px 10px;
    color: var(--ndp-accent);
    font-weight: 600;
    font-size: 0.85rem;
  }
  .link:hover {
    background: var(--ndp-hover);
  }
`;

/** Text fields and their labels / error messages. Shared by the field and input elements. */
export const fieldStyles = css`
  .field {
    display: inline-flex;
    flex-direction: column;
    gap: 4px;
  }
  .field label {
    font-size: 0.78rem;
    color: var(--ndp-muted);
  }
  .row {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }
  input {
    font: inherit;
    color: var(--ndp-text);
    background: var(--ndp-bg);
    border: 1px solid var(--ndp-border);
    border-radius: 8px;
    padding: 8px 10px;
    min-width: 9.5rem;
  }
  input:focus-visible {
    outline: 2px solid var(--ndp-accent);
    outline-offset: 0;
    border-color: var(--ndp-accent);
  }
  .invalid input {
    border-color: var(--ndp-error);
  }
  .error {
    color: var(--ndp-error);
    font-size: 0.75rem;
    min-height: 1em;
  }
  .icon-button {
    width: 36px;
    height: 36px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border: 1px solid var(--ndp-border);
    border-radius: 8px;
    background: var(--ndp-bg);
    cursor: pointer;
    color: var(--ndp-text);
  }
  .icon-button:hover {
    background: var(--ndp-hover);
  }
`;

/** Dialog backdrop / surface and the docked popover positioning. */
export const overlayStyles = css`
  .backdrop {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 16px;
    z-index: 1000;
  }
  .dialog {
    background: var(--ndp-bg);
    border-radius: calc(var(--ndp-radius) + 4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.24);
    max-width: 360px;
    width: 100%;
    padding: 16px;
  }
  .dialog.fullscreen {
    max-width: none;
    width: 100%;
    height: 100%;
    border-radius: 0;
    display: flex;
    flex-direction: column;
  }
  .dialog-title {
    font-size: 0.8rem;
    color: var(--ndp-muted);
  }
  .dialog-headline {
    font-size: 1.5rem;
    font-weight: 600;
    margin: 4px 0 12px;
  }
  .actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 12px;
  }
  .actions button {
    padding: 8px 14px;
    color: var(--ndp-accent);
    font-weight: 600;
    border-radius: 8px;
    background: transparent;
    border: 0;
    cursor: pointer;
    font: inherit;
  }
  .actions button:hover:not(:disabled) {
    background: var(--ndp-hover);
  }
  .actions button:disabled {
    color: var(--ndp-muted);
    cursor: default;
  }
  .anchor {
    position: relative;
    display: inline-block;
  }
  .popover {
    position: absolute;
    top: calc(100% + 4px);
    left: 0;
    z-index: 900;
    background: var(--ndp-bg);
    border-radius: var(--ndp-radius);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.18);
  }
`;
