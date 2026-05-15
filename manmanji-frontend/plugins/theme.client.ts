// ============================================================
// plugins/theme.client.ts — 主题初始化插件（仅客户端）
// 文件名中的 .client 表示此插件只在浏览器端执行（不参与 SSR）
//
// 为什么需要两个主题初始化？
// 1. nuxt.config.ts 中的 inline script → 在 Vue 加载前同步执行（防闪烁）
// 2. 这个插件 → Vue 加载后同步 Pinia store 状态
// 两者协作确保主题切换时 UI 和数据一致
// ============================================================

export default defineNuxtPlugin(() => {
  const store = useThemeStore()
  store.init()  // 读取 localStorage，设置 data-theme 属性
})
