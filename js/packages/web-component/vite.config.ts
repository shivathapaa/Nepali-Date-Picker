/// <reference types="vitest/config" />
import { defineConfig } from 'vitest/config';
import { resolve } from 'node:path';

const root = import.meta.dirname;

export default defineConfig({
  build: {
    lib: {
      entry: {
        index: resolve(root, 'src/index.ts'),
        'nepali-date-picker': resolve(root, 'src/nepali-date-picker.ts'),
        'nepali-date-range-picker': resolve(root, 'src/nepali-date-range-picker.ts'),
        'nepali-date-picker-dialog': resolve(root, 'src/nepali-date-picker-dialog.ts'),
        'nepali-date-picker-docked': resolve(root, 'src/nepali-date-picker-docked.ts'),
        'nepali-date-field': resolve(root, 'src/nepali-date-field.ts'),
        'nepali-date-range-field': resolve(root, 'src/nepali-date-range-field.ts'),
        'nepali-wheel-date-picker': resolve(root, 'src/nepali-wheel-date-picker.ts'),
      },
      formats: ['es'],
    },
    rollupOptions: {
      // Keep lit and the conversion engine external so consumer bundlers dedupe them.
      external: [/^lit($|\/)/, '@nepali-date-picker/core'],
    },
    sourcemap: true,
    target: 'es2021',
    minify: false,
  },
  test: {
    environment: 'jsdom',
    include: ['test/**/*.test.ts'],
    globals: false,
  },
});
