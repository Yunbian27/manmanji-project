export default defineNuxtRouteMiddleware((to) => {
  if (to.path === '/' || to.path === '/login') return

  // 服务端：检查 cookie 是否存在（无 cookie → 重定向到介绍页）
  if (import.meta.server) {
    const event = useRequestEvent()
    const cookieHeader = event?.headers.get('cookie') || ''
    if (!cookieHeader.includes('mannote-token=')) {
      return navigateTo('/')
    }
    return
  }

  // 客户端：用 store 校验 JWT 有效性
  const authStore = useAuthStore()
  if (!authStore.isAuthenticated) {
    return navigateTo('/')
  }
})
