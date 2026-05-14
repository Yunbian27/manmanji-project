import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref<'dark' | 'light'>('dark')

  function init() {
    if (import.meta.client) {
      const saved = localStorage.getItem('mannote-theme')
      theme.value = saved === 'light' ? 'light' : 'dark'
      applyTheme()
    }
  }

  function toggle() {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
    if (import.meta.client) {
      localStorage.setItem('mannote-theme', theme.value)
      applyTheme()
    }
  }

  function applyTheme() {
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  const isDark = computed(() => theme.value === 'dark')

  return { theme, isDark, init, toggle }
})
