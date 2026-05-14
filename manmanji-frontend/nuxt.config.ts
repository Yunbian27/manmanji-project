export default defineNuxtConfig({
  compatibilityDate: '2026-05-14',

  modules: [
    // Pinia configured manually via plugins/pinia.ts (module incompatible with Nuxt 4)
    '@vueuse/nuxt',
  ],

  css: [
    '~/assets/css/tokens.css',
    '~/assets/css/base.css',
    '~/assets/css/transitions.css',
    '~/assets/css/light-theme.css',
  ],

  runtimeConfig: {
    public: {
      apiBase: 'http://localhost:8080',
    },
  },

  app: {
    head: {
      script: [
        {
          innerHTML: `(function(){try{var t=localStorage.getItem('mannote-theme')||'dark';document.documentElement.setAttribute('data-theme',t)}catch(e){}})()`,
          type: 'text/javascript',
        },
      ],
      link: [
        {
          rel: 'preconnect',
          href: 'https://fonts.googleapis.com',
        },
        {
          rel: 'preconnect',
          href: 'https://fonts.gstatic.com',
          crossorigin: '',
        },
        {
          rel: 'stylesheet',
          href: 'https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400&display=swap',
        },
      ],
    },
  },

  nitro: {
    devProxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },

  imports: {
    dirs: ['stores'],
  },

  components: [
    { path: '~/components/common', prefix: '' },
    { path: '~/components/layout', prefix: '' },
    { path: '~/components/article', prefix: '' },
    { path: '~/components/ui', prefix: '' },
  ],

  vite: {
    optimizeDeps: {
      include: ['markdown-it', 'pinia'],
    },
  },

  devtools: { enabled: true },
  sourcemap: false,
})
