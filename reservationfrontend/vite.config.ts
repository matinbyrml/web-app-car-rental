import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],

  /** ①  SPA lives at the root when you open :4173 directly */
  base: '/',

  server: {
    port: 4173,
    /** ②  Forward everything that should still hit the Gateway */
    proxy: {
      '/api':          'http://localhost:8084',
      '/me':           'http://localhost:8084',
      '/csrf':         'http://localhost:8084',
      '/serverLogin':  'http://localhost:8084',
      '/logout':       'http://localhost:8084',
      '/oauth2':       'http://localhost:8084',
      '/login':        'http://localhost:8084'
    }
  }
});
