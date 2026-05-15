// ============================================================
// plugins/pinia.ts — Pinia 状态管理插件
// Nuxt 插件：在应用启动时自动执行，为 Vue 应用安装 Pinia
//
// 为什么手动注册而不是用 @pinia/nuxt 模块？
// @pinia/nuxt 当前与 Nuxt 4 不兼容，所以改用原生 Pinia API 手动安装
// 功能完全一样，只是少了自动导入 defineStore（需要在 stores 文件中手动 import）
// ============================================================

import { createPinia } from 'pinia'

export default defineNuxtPlugin((nuxtApp) => {
  // createPinia() 创建 Pinia 实例（全局状态管理器）
  const pinia = createPinia()
  // nuxtApp.vueApp.use() 相当于 Vue 3 的 app.use()，安装插件
  nuxtApp.vueApp.use(pinia)
})
