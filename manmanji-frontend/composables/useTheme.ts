// ============================================================
// composables/useTheme.ts — 主题切换组合函数
// 对 useThemeStore 的轻量封装，在组件 mounted 时初始化
//
// 为什么不在组件中直接用 useThemeStore()？
// 因为这个 composable 封装了 onMounted(init) 逻辑，
// 避免了每个使用主题的组件都要手动调用 init()
// ============================================================

export function useTheme() {
  const store = useThemeStore()

  // onMounted: Vue 生命周期钩子，组件挂载到 DOM 后执行
  // 此时可以安全访问 localStorage（客户端环境）
  onMounted(() => {
    store.init()
  })

  return {
    theme: computed(() => store.theme),       // 当前主题名
    isDark: computed(() => store.isDark),     // 是否深色（便捷布尔值）
    toggleTheme: store.toggle,                // 切换方法
  }
}
