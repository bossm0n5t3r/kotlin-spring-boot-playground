import { defineConfig } from 'astro/config';
import node from '@astrojs/node';

export default defineConfig({
  output: 'server',
  adapter: node({ mode: 'standalone' }),
  server: {
    port: Number(process.env.PORT) || 1033,
  },
  base: process.env.PUBLIC_BASE_PATH || '/',
});
