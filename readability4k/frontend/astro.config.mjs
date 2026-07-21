// @ts-check
import { defineConfig } from 'astro/config';
import node from '@astrojs/node';

// https://astro.build/config
const basePath = process.env.PUBLIC_BASE_PATH || '/';

export default defineConfig({
  output: 'server',
  adapter: node({ mode: 'standalone' }),
  server: { port: 1021 },
  base: basePath,
});
