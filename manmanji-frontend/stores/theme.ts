// ============================================================
// stores/theme.ts — 主题状态管理（Pinia Store）
// 管理深色/浅色主题切换，状态持久化到 localStorage
//
// Pinia Store 是 Vue 3 的全局状态管理方案（Vue 2 时代的 Vuex 的替代品）
// 使用 Composition API 风格（setup store）而非 Options API 风格
// ============================================================

import { defineStore } from 'pinia'

// defineStore 的第一个参数 'theme' 是 store 的唯一标识符
// 第二个参数是一个 setup 函数（类似 Vue 组件的 setup）
export const useThemeStore = defineStore('theme', () => {
  // ---- 状态（state）----
  // ref() 包裹的值变为响应式，变化时自动更新 UI
  const theme = ref<'dark' | 'light'>('dark')  // 默认深色

  // ---- 方法（actions）----
  /** 初始化：从 localStorage 读取保存的主题偏好 */
  function init() {
    // import.meta.client 是 Vite 的环境变量，true 表示在浏览器端运行
    // 在服务端渲染(SSR)时此值为 false，避免访问 localStorage 报错
    if (import.meta.client) {
      const saved = localStorage.getItem('mannote-theme')
      theme.value = saved === 'light' ? 'light' : 'dark'
      applyTheme()
    }
  }

  /** 切换主题：dark → light 或 light → dark */
  function toggle() {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
    if (import.meta.client) {
      localStorage.setItem('mannote-theme', theme.value)
      applyTheme()
    }
  }

  /** 应用主题到 DOM：设置 <html data-theme="..."> 属性 */
  function applyTheme() {
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  // ---- 计算属性（getters）----
  // computed() 根据其他值自动计算，有缓存
  const isDark = computed(() => theme.value === 'dark')

  // 暴露给外部使用的状态和方法
  return { theme, isDark, init, toggle }
})
