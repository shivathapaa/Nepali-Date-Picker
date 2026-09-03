import { defineConfig } from 'vite';

// Relative base so the built site works whether it is served from the domain root (local preview)
// or nested under /demo on GitHub Pages.
export default defineConfig({
  base: './',
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
});
