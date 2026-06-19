<!--
  pages/publish-success.vue — 文章发布成功页
  路由：/publish-success?articleId=X
-->
<template>
  <div class="success-page">
    <!-- 顶部导航栏 -->
    <header class="success-topbar">
      <NuxtLink to="/home" class="brand-logo">
        <img src="/favicon.svg" alt="慢慢记" class="logo-mark" />
        <span class="logo-text">慢慢记</span>
      </NuxtLink>
      <div v-if="auth.isAuthenticated" ref="avatarContainer" class="avatar-wrapper">
        <div class="nav-avatar" :title="auth.user?.nickname" @click="showDropdown = !showDropdown">
          <img v-if="auth.user?.avatarUrl && !avatarError" :src="auth.user!.avatarUrl" class="avatar-img" @error="avatarError = true" />
          <IconLucideUser v-else />
        </div>
        <Transition name="dropdown">
          <div v-if="showDropdown" class="avatar-dropdown">
            <div class="dropdown-user">
              <img v-if="auth.user?.avatarUrl && !avatarError" :src="auth.user!.avatarUrl" class="dropdown-avatar" @error="avatarError = true" />
              <span v-else class="dropdown-avatar dropdown-avatar--fallback"><IconLucideUser /></span>
              <div>
                <p class="dropdown-nickname">{{ auth.user?.nickname }}</p>
                <p class="dropdown-username">{{ auth.user?.username }}</p>
              </div>
            </div>
            <button class="dropdown-item" @click="router.push('/manage?tab=profile')">
              <IconLucideUser />个人资料
            </button>
            <button class="dropdown-item" @click="router.push('/manage?tab=articles')">
              <IconLucideFileText />内容管理
            </button>
            <div class="dropdown-divider" />
            <button class="dropdown-item dropdown-item-danger" @click="handleLogout">
              <IconLucideLogOut />退出登录
            </button>
          </div>
        </Transition>
      </div>
    </header>

    <!-- 居中主体 -->
    <div class="success-body">
      <div class="success-card">
        <div class="check-circle">
          <IconLucideCheck class="check-icon" />
        </div>
        <h1 class="success-title">发布成功！正在审核中</h1>
        <p class="success-desc">文章已提交，审核通过后将公开展示</p>
        <div class="success-actions">
          <button class="btn btn-primary" @click="viewArticle">查看文章</button>
          <button class="btn btn-secondary" @click="router.push('/manage')">管理文章</button>
          <button class="btn btn-secondary" @click="router.push('/write')">再写一篇</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onClickOutside } from '@vueuse/core'
import IconLucideCheck from '~icons/lucide/check'
import IconLucideFileText from '~icons/lucide/file-text'
import IconLucideLogOut from '~icons/lucide/log-out'
import IconLucideUser from '~icons/lucide/user'

definePageMeta({ layout: 'blank' })

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const articleId = computed(() => Number(route.query.articleId) || 0)

const showDropdown = ref(false)
const avatarError = ref(false)
const avatarContainer = ref<HTMLElement | null>(null)

onClickOutside(avatarContainer, () => {
  showDropdown.value = false
})

function viewArticle() {
  if (articleId.value > 0) {
    router.push(`/home?articleId=${articleId.value}`)
  } else {
    router.push('/home')
  }
}

async function handleLogout() {
  const { logout } = useAuth()
  await logout()
  showDropdown.value = false
  navigateTo('/login')
}
</script>

<style scoped>
.success-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--canvas);
}

/* ── Topbar ── */
.success-topbar {
  height: var(--topbar-height);
  background: var(--canvas);
  padding: 0 var(--spacing-xxl);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--ink);
  text-decoration: none;
}

.logo-mark {
  width: 28px;
  height: 28px;
  border-radius: var(--rounded-md);
}

.logo-text {
  font-size: 17px;
  font-weight: var(--weight-semibold);
}

/* ── Avatar ── */
.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.nav-avatar {
  width: 36px; height: 36px;
  border-radius: var(--rounded-full);
  background: var(--surface);
  color: var(--ink);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--body-sm);
  font-weight: var(--weight-medium);
  cursor: pointer;
  transition: background 0.15s var(--ease);
}

.nav-avatar:hover { background: var(--hairline-soft); }

.avatar-img { width: 100%; height: 100%; object-fit: cover; border-radius: var(--rounded-full); }

.avatar-dropdown {
  position: absolute;
  top: calc(100% + 16px);
  right: 0;
  min-width: 280px;
  padding: var(--spacing-md);
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-lg);
  box-shadow: rgba(15, 15, 15, 0.16) 0px 16px 48px -8px;
  z-index: 100;
}

.dropdown-user {
  display: flex; align-items: center; gap: var(--spacing-sm);
  padding: 16px 20px;
}

.dropdown-avatar {
  width: 36px; height: 36px;
  border-radius: var(--rounded-full);
  flex-shrink: 0;
  object-fit: cover;
}

.dropdown-avatar--fallback {
  background: var(--surface);
  color: var(--ink);
  display: flex; align-items: center; justify-content: center;
}

.dropdown-nickname {
  font-size: var(--body-sm);
  font-weight: var(--weight-medium);
  color: var(--ink);
  margin: 0;
  line-height: 1.4;
}

.dropdown-username {
  font-size: var(--caption);
  color: var(--muted);
  margin: 2px 0 0;
  line-height: 1.4;
}

.dropdown-divider {
  height: 1px;
  background: var(--hairline);
  margin: 8px 0;
}

.dropdown-item {
  display: flex; align-items: center; gap: var(--spacing-xs);
  width: 100%;
  padding: 12px 20px;
  border: none;
  border-radius: var(--rounded-sm);
  background: transparent;
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  font-weight: var(--weight-regular);
  line-height: 1.4;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s var(--ease);
}

.dropdown-item:hover { background: var(--hairline-soft); }

.dropdown-item-danger:hover { color: var(--semantic-error); }

.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s var(--ease), transform 0.15s var(--ease);
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

/* ── Body ── */
.success-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: var(--spacing-xxxl);
}

.check-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 3px solid var(--semantic-success);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--spacing-xl);
}

.check-icon {
  width: 40px;
  height: 40px;
  color: var(--semantic-success);
  stroke-width: 2.5;
}

.success-title {
  font-family: var(--font-sans);
  font-size: var(--heading-2);
  font-weight: var(--weight-semibold);
  line-height: var(--leading-heading);
  color: var(--ink);
  margin: 0 0 var(--spacing-sm);
}

.success-desc {
  font-family: var(--font-sans);
  font-size: var(--body-md);
  color: var(--steel);
  margin: 0 0 var(--spacing-xxl);
}

.success-actions {
  display: flex;
  gap: var(--spacing-md);
}

.btn {
  height: 40px;
  padding: 0 var(--spacing-xl);
  border-radius: var(--rounded-md);
  font-family: var(--font-sans);
  font-size: var(--button-md);
  font-weight: var(--weight-medium);
  cursor: pointer;
  transition: background-color 0.15s var(--ease), color 0.15s var(--ease), border-color 0.15s var(--ease);
  white-space: nowrap;
}

.btn-primary {
  border: none;
  background: var(--primary);
  color: var(--on-primary);
}

.btn-primary:hover { background: var(--primary-pressed); }

.btn-secondary {
  border: 1px solid var(--hairline-strong);
  background: transparent;
  color: var(--ink);
}

.btn-secondary:hover { background: var(--hairline-soft); }
</style>
