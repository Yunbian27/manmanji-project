export function useTheme() {
  const store = useThemeStore()

  onMounted(() => {
    store.init()
  })

  return {
    theme: computed(() => store.theme),
    isDark: computed(() => store.isDark),
    toggleTheme: store.toggle,
  }
}
