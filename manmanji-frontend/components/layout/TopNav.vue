<!--
  TopNav.vue — 顶部导航栏（Notion DESIGN.md 规范）
  用于 default 布局，study 布局已不使用
  sticky 定位，51px 高
-->
<template>
  <header class="top-nav">
    <div class="nav-left">
      <div class="nav-logo">
        <span class="logo-icon">慢</span>
        <span class="logo-text">慢慢记</span>
      </div>

      <nav class="nav-links">
        <a
          v-for="link in navLinks"
          :key="link.href"
          :href="link.href"
          :class="{ active: link.active }"
          class="nav-link"
        >
          {{ link.label }}
        </a>
      </nav>
    </div>

    <div class="nav-right">
      <div class="nav-search">
        <IconLucideSearch class="search-icon icon-md" />
        <input type="text" class="search-input" placeholder="搜索文章..." />
      </div>

      <template v-if="!auth.isAuthenticated">
        <AppButton variant="secondary" @click="navigateTo('/login')">登录</AppButton>
        <AppButton variant="primary" @click="navigateTo('/login')">注册</AppButton>
      </template>

      <div v-if="auth.isAuthenticated" ref="avatarContainer" class="avatar-wrapper">
        <div class="nav-avatar" :title="auth.user?.nickname" @click="showDropdown = !showDropdown">
          <img v-if="auth.user?.avatarUrl && !avatarError" :src="auth.user!.avatarUrl" class="avatar-img" @error="avatarError = true" />
          <span v-else>{{ auth.user?.nickname?.charAt(0) || '用' }}</span>
        </div>
        <Transition name="dropdown">
          <div v-if="showDropdown" class="avatar-dropdown">
            <div class="dropdown-user">
              <p class="dropdown-nickname">{{ auth.user?.nickname }}</p>
              <p class="dropdown-username">{{ auth.user?.username }}</p>
            </div>
            <div class="dropdown-divider" />
            <button class="dropdown-item dropdown-item-danger" @click="handleLogout">退出登录</button>
          </div>
        </Transition>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { onClickOutside } from '@vueuse/core'
import IconLucideSearch from '~icons/lucide/search'

const auth = useAuthStore()

const showDropdown = ref(false)
const avatarError = ref(false)
const avatarContainer = ref<HTMLElement | null>(null)

onClickOutside(avatarContainer, () => {
  showDropdown.value = false
})

async function handleLogout() {
  const { logout } = useAuth()
  await logout()
  showDropdown.value = false
  navigateTo('/login')
}

const navLinks = [
  { label: '首页', href: '/home', active: true },
  { label: '博客', href: '#', active: false },
  { label: '社区', href: '#', active: false },
  { label: 'AI助手', href: '#', active: false },
  { label: '关于', href: '#', active: false },
]
</script>

<style scoped>
.top-nav {
  height: var(--topbar-height);
  position: sticky;
  top: 0;
  z-index: var(--z-nav);
  background: var(--canvas);
  border-bottom: 1px solid var(--hairline);
  padding: 0 var(--spacing-xxl);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-md);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-xl);
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--ink);
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: var(--primary);
  color: var(--on-primary);
  border-radius: var(--rounded-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--body-md);
  font-weight: var(--weight-semibold);
}

.logo-text {
  font-size: 18px;
  font-weight: var(--weight-semibold);
}

.nav-links { display: flex; gap: var(--spacing-xs); list-style: none; }

.nav-link {
  font-size: var(--body-sm);
  font-weight: var(--weight-medium);
  line-height: 1.4;
  padding: 8px 14px;
  border-radius: var(--rounded-md);
  color: var(--steel);
  transition: background-color 0.15s var(--ease), color 0.15s var(--ease);
}
.nav-link:hover,
.nav-link.active {
  color: var(--ink);
  background: var(--hairline-soft);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin-left: auto;
}

.nav-search { position: relative; width: 200px; }
.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--muted);
  pointer-events: none;
}
.search-input {
  width: 100%;
  height: 36px;
  border-radius: var(--rounded-md);
  border: 1px solid var(--hairline);
  background: var(--surface);
  color: var(--ink);
  padding: 0 14px 0 38px;
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  transition: border-color 0.15s var(--ease);
}
.search-input::placeholder { color: var(--muted); }
.search-input:focus { outline: none; border-color: var(--primary); }

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
  flex-shrink: 0;
  cursor: pointer;
  transition: background 0.15s var(--ease);
}
.nav-avatar:hover { background: var(--hairline-soft); }
.avatar-img { width: 100%; height: 100%; object-fit: cover; border-radius: var(--rounded-full); }

.avatar-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 160px;
  padding: var(--spacing-xs);
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-xl);
}

.dropdown-user { padding: 8px 12px; }
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
  margin: 4px 0;
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 8px 12px;
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

@media (max-width: 599px) {
  .nav-links { display: none; }
  .nav-search { display: none; }
}
</style>
