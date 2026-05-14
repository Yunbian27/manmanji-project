export default defineNuxtPlugin(() => {
  const store = useThemeStore()
  store.init()
})
