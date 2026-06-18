<!--
  pages/index.vue — 项目介绍页（Landing Page）
  未登录用户统一入口，已登录按角色跳转 /home 或 /admin
-->
<template>
  <div class="landing-page">
    <div class="landing-hero">
      <h1 class="hero-title">慢慢记</h1>
      <p class="hero-subtitle">专注技术，慢下来思考</p>
      <p class="hero-desc">一个专注于深度技术内容创作与分享的个人知识库</p>
      <div class="hero-actions">
        <AppButton variant="primary" @click="handleGetStarted">开始使用</AppButton>
        <AppButton variant="secondary" @click="handleLearnMore">了解更多</AppButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'blank' })

const router = useRouter()
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
  }
} else if (auth.isAuthenticated) {
  await navigateTo(auth.isAdmin ? '/admin' : '/home', { replace: true })
}

function handleGetStarted() {
  router.push('/login')
}

function handleLearnMore() {
  // TODO: scroll to details section
}
</script>

<style scoped>
.landing-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--canvas);
}

.landing-hero {
  text-align: center;
  max-width: 600px;
  padding: var(--spacing-xxl);
}

.hero-title {
  font-size: 52px;
  font-weight: var(--weight-semibold);
  color: var(--ink);
  margin-bottom: var(--spacing-md);
  line-height: 1.15;
}

.hero-subtitle {
  font-size: var(--heading-4);
  font-weight: var(--weight-medium);
  color: var(--steel);
  margin-bottom: var(--spacing-md);
}

.hero-desc {
  font-size: var(--subtitle);
  color: var(--muted);
  margin-bottom: var(--spacing-xxl);
  line-height: var(--leading-body);
}

.hero-actions {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
}
</style>
