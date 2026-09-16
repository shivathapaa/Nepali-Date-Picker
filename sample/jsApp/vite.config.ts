import { defineConfig } from 'vite';
import { fileURLToPath, URL } from 'node:url';

const repoRoot = fileURLToPath(new URL('../../', import.meta.url));

// Relative base so the built site works whether it is served from the domain root (local preview)
// or nested under /demo on GitHub Pages.
export default defineConfig({
  base: './',
  resolve: {
    alias: {
      '@nepali-date-picker/core': fileURLToPath(
        new URL('../../js/packages/core/dist/NepaliDatePickerKmp-nepali-date-picker-core.mjs', import.meta.url),
      ),
      '@nepali-date-picker/web-component': fileURLToPath(
        new URL('../../js/packages/web-component/dist/index.js', import.meta.url),
      ),
    },
  },
  server: {
    // Allow the dev server to serve the aliased dist files, which sit above this sample's root.
    fs: { allow: [repoRoot] },
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
});
