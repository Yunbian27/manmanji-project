// 角色隔离：管理员只能访问后台，普通用户不能访问后台
// 服务端通过解码 JWT cookie 获取角色，无需等待客户端 store 恢复
export default defineNuxtRouteMiddleware(async (to) => {
  if (import.meta.server) {
    const event = useRequestEvent()
    const cookieHeader = event?.headers.get('cookie') || ''
    const tokenMatch = cookieHeader.match(/mannote-token=([^;]+)/)
    const token = tokenMatch?.[1]
    if (!token) return
    const { decodeJwtPayload } = await import('~/utils/jwt')
    const payload = decodeJwtPayload(token)
    const isAdmin = payload?.role === 'ADMIN'

    const adminOnly = ['/admin'].some(p => to.path.startsWith(p))
    const userOnly = ['/home', '/write', '/manage'].some(p => to.path.startsWith(p))

    if (isAdmin && userOnly) return navigateTo('/admin')
    if (!isAdmin && adminOnly) return navigateTo('/home')
    return
  }

  const auth = useAuthStore()
  if (!auth.isAuthenticated) return

  const adminOnly = ['/admin'].some(p => to.path.startsWith(p))
  const userOnly = ['/home', '/write', '/manage'].some(p => to.path.startsWith(p))

  if (auth.isAdmin && userOnly) {
    return navigateTo('/admin')
  }
  if (!auth.isAdmin && adminOnly) {
    return navigateTo('/home')
  }
})
