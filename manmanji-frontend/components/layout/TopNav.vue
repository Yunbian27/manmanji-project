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
        <IconSearch :size="16" class="search-icon" />
        <input
          type="text"
          class="search-input"
          placeholder="搜索文章..."
        />
      </div>
      <template v-if="!auth.isAuthenticated">
        <AppButton variant="secondary" @click="showLogin = true">
          登录
        </AppButton>
        <AppButton variant="primary" @click="showRegister = true">
          注册
        </AppButton>
      </template>
      <template v-else>
        <AppButton variant="secondary" @click="goWrite">
          写文章
        </AppButton>
      </template>
      <ThemeToggle />
      <div v-if="auth.isAuthenticated" class="nav-avatar" :title="auth.user?.nickname">
        {{ auth.user?.nickname?.charAt(0) || '用' }}
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
const auth = useAuthStore()

const showLogin = ref(false)
const showRegister = ref(false)

const navLinks = [
  { label: '首页', href: '/', active: true },
  { label: '博客', href: '#', active: false },
  { label: '社区', href: '#', active: false },
  { label: 'AI助手', href: '#', active: false },
  { label: '关于', href: '#', active: false },
]

function goWrite() {
  // TODO: navigate to write page
}
</script>

<style scoped>
.top-nav {
  height: var(--nav-height);
  position: sticky;
  top: 0;
  z-index: var(--z-nav);
  background: var(--canvas);
  border-bottom: 1px solid var(--hairline);
  padding: 0 var(--space-xl);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-lg);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  color: var(--ink);
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: var(--primary);
  color: #0a0a0a;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-title-sm);
  font-weight: var(--weight-bold);
}

.logo-text {
  font-size: 18px;
  font-weight: var(--weight-bold);
  letter-spacing: -0.3px;
}

.nav-links {
  display: flex;
  gap: var(--space-xxs);
  list-style: none;
}

.nav-link {
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  line-height: var(--leading-normal);
  padding: 8px 14px;
  border-radius: var(--radius-md);
  color: var(--muted);
  transition: var(--transition-hover);
}

.nav-link:hover,
.nav-link.active {
  color: var(--ink);
  background: var(--surface-card);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-left: auto;
}

.nav-search {
  position: relative;
  width: 200px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--muted-soft);
  pointer-events: none;
}

.search-input {
  width: 100%;
  height: 40px;
  border-radius: var(--radius-md);
  border: 1px solid var(--hairline);
  background: var(--surface-card);
  color: var(--ink);
  padding: 0 14px 0 38px;
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  transition: var(--transition-hover);
}

.search-input::placeholder { color: var(--muted-soft); }
.search-input:focus { outline: none; border-color: var(--primary); }

.nav-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  background: var(--surface-card);
  color: var(--ink);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .nav-links { display: none; }
  .nav-search { display: none; }
}
</style>
