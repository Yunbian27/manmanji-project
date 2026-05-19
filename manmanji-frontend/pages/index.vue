<!--
  pages/index.vue — 项目介绍页
  未登录用户统一入口，已登录自动跳转 /home
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

// SSR: 直接检查 cookie（store 在服务端未被水合，isAuthenticated 恒为 false）
if (import.meta.server) {
  const event = useRequestEvent()
  const cookieHeader = event?.headers.get('cookie') || ''
  if (cookieHeader.includes('mannote-token=')) {
    await navigateTo('/home', { replace: true })
  }
} else if (auth.isAuthenticated) {
  await navigateTo('/home', { replace: true })
}

function handleGetStarted() {
  router.push('/login')
}

function handleLearnMore() {
  // TODO: 滚动到下方详细介绍区
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
  padding: var(--space-3xl);
}

.hero-title {
  font-size: var(--display-xxl);
  font-weight: var(--weight-bold);
  color: var(--ink);
  margin-bottom: var(--space-md);
}

.hero-subtitle {
  font-size: var(--text-display-md);
  font-weight: var(--weight-medium);
  color: var(--body);
  margin-bottom: var(--space-lg);
}

.hero-desc {
  font-size: var(--text-body-lg);
  color: var(--muted);
  margin-bottom: var(--space-3xl);
  line-height: var(--leading-relaxed);
}

.hero-actions {
  display: flex;
  gap: var(--space-md);
  justify-content: center;
}
</style>
