// ============================================================
// nuxt.config.ts — Nuxt 总配置文件
// 相当于 Spring Boot 的 application.yml，控制整个项目的构建和运行
// ============================================================

export default defineNuxtConfig({
  compatibilityDate: '2026-05-14',

  // 纯 SPA 模式：所有路由客户端渲染，nginx try_files 回退到 index.html
  routeRules: {
    '/**': { ssr: false },
  },

  // --------------------------------------------------
  // modules: Nuxt 插件模块（在构建时自动注入功能）
  // @pinia/nuxt 因与 Nuxt 4 不兼容，改用 plugins/pinia.ts 手动注册
  // @vueuse/nuxt: 提供 useMediaQuery 等工具函数
  // --------------------------------------------------
  modules: [
    '@vueuse/nuxt',
    ['unplugin-icons/nuxt', { compiler: 'vue3', autoInstall: false }],
  ],

  // --------------------------------------------------
  // css: 全局 CSS 文件（每个页面都会加载）
  // 加载顺序：tokens(变量定义) → base(重置) → transitions(动画)
  // --------------------------------------------------
  css: [
    '~/assets/css/tokens.css',          // CSS 变量定义（颜色、字号、间距等）
    '~/assets/css/base.css',            // 全局基础样式和 reset
    '~/assets/css/markdown-content.css', // Markdown 渲染统一样式
    'katex/dist/katex.min.css',         // KaTeX 数学公式样式
    '~/assets/css/transitions.css',     // Vue 过渡动画
    'highlight.js/styles/atom-one-dark.css',
  ],

  // --------------------------------------------------
  // runtimeConfig: 运行时配置（可在组件中通过 useRuntimeConfig() 读取）
  // public 下的配置会暴露给客户端
  // --------------------------------------------------
  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE ?? '',
    },
  },

  // --------------------------------------------------
  // imports: 自动导入配置
  // Nuxt 默认自动导入 composables/ 目录，这里增加 stores/ 目录
  // 这样 useAuthStore() 不需要手动 import
  // --------------------------------------------------
  imports: {
    dirs: ['stores'],
  },

  // --------------------------------------------------
  // components: 组件自动导入配置
  // prefix: '' 表示不加目录前缀，即 TopNav.vue → <TopNav />
  // 不加的话 Nuxt 4 会变成 <LayoutTopNav />
  // --------------------------------------------------
  components: [
    { path: '~/components/common', prefix: '' },
    { path: '~/components/layout', prefix: '' },
    { path: '~/components/article', prefix: '' },
    { path: '~/components/ui', prefix: '' },
    { path: '~/components/editor', prefix: '' },
  ],

  // --------------------------------------------------
  // app: 应用级配置
  // head.link: Google Fonts 字体加载
  // --------------------------------------------------
  app: {
    head: {
      link: [
        { rel: 'preconnect', href: 'https://fonts.googleapis.com' },
        { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: '' },
        {
          rel: 'stylesheet',
          href: 'https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&family=JetBrains+Mono:wght@400&display=swap',
        },
      ],
    },
  },

  // --------------------------------------------------
  // nitro: Nuxt 的服务端引擎（Nitro）配置
  // devProxy: 开发模式下将 /api 请求代理到后端，避免跨域问题
  // --------------------------------------------------
  nitro: {
    devProxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },


  // --------------------------------------------------
  // vite: 构建工具配置
  // optimizeDeps.include: 预打包这些依赖，避免开发时页面因新发现依赖而刷新
  // --------------------------------------------------
  vite: {
    optimizeDeps: {
      include: ['markdown-it', 'pinia', 'highlight.js'],
    },
  },

  devtools: { enabled: true },  // 开启 Vue DevTools
  sourcemap: false,              // 生产环境不生成 sourcemap（减小体积）
})
