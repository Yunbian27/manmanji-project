<!--
  pages/index.vue — 入口重定向
  已登录 → /home（管理员 → /admin），未登录 → /about
-->
<script setup lang="ts">
const auth = useAuthStore()

if (import.meta.server) {
  const event = useRequestEvent()
  const cookieHeader = event?.headers.get('cookie') || ''
  const tokenMatch = cookieHeader.match(/mannote-token=([^;]+)/)
  const token = tokenMatch?.[1]
  if (token) {
    const { decodeJwtPayload } = await import('~/utils/jwt')
    const payload = decodeJwtPayload(token)
    await navigateTo(payload?.role === 'ADMIN' ? '/admin' : '/home', { replace: true })
  } else {
    await navigateTo('/about', { replace: true })
  }
} else {
  if (auth.isAuthenticated) {
    await navigateTo(auth.isAdmin ? '/admin' : '/home', { replace: true })
  } else {
    await navigateTo('/about', { replace: true })
  }
}
</script>
