export default defineNuxtRouteMiddleware((to) => {
  if (to.path === '/login') return

  // 服务端：检查 cookie 是否存在（无 cookie → 直接重定向，无页面闪烁）
  if (import.meta.server) {
    const event = useRequestEvent()
    const cookieHeader = event?.headers.get('cookie') || ''
    if (!cookieHeader.includes('mannote-token=')) {
      return navigateTo(`/login?redirect=${encodeURIComponent(to.path)}`)
    }
    return
  }

  // 客户端：用 store 校验 JWT 有效性
  const authStore = useAuthStore()
  if (!authStore.isAuthenticated) {
    return navigateTo(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
  }
})
