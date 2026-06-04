<!--
  EditorTopNav.vue — 编辑器顶部导航栏（Notion DESIGN.md 规范）
  51px 高，sticky 定位
  左侧: logo + 返回 + 标题输入 | 右侧: 用户头像
-->
<template>
  <header class="editor-topnav">
    <div class="topnav-left">
      <div class="nav-logo">
        <img src="/favicon.svg" alt="慢慢记" class="logo-icon" />
        <span class="logo-text">慢慢记</span>
      </div>
      <button class="back-btn" @click="handleBack">
        <IconChevronRight :size="14" class="back-icon" />
        <span>返回</span>
      </button>
      <input
        :value="title"
        class="title-input"
        :class="{ 'has-error': titleError }"
        placeholder="输入文章标题..."
        type="text"
        maxlength="200"
        @input="onTitleInput"
        @keydown.enter.prevent
      />
    </div>

    <div class="topnav-right">
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

const router = useRouter()
const auth = useAuthStore()
const editor = injectEditor()
const { title, titleError } = editor

const showDropdown = ref(false)
const avatarError = ref(false)
const avatarContainer = ref<HTMLElement | null>(null)

onClickOutside(avatarContainer, () => {
  showDropdown.value = false
})

function onTitleInput(e: Event) {
  title.value = (e.target as HTMLInputElement).value
}

function handleBack() {
  router.push('/home')
}

async function handleLogout() {
  const { logout } = useAuth()
  await logout()
  showDropdown.value = false
  navigateTo('/login')
}
</script>

<style scoped>
.editor-topnav {
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
}

.topnav-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  flex-shrink: 0;
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

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  font-weight: var(--weight-medium);
  color: var(--steel);
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 4px var(--spacing-xs);
  border-radius: var(--rounded-md);
  transition: color 0.15s var(--ease);
}
.back-btn:hover { color: var(--ink); }
.back-icon { transform: rotate(180deg); flex-shrink: 0; }

.title-input {
  width: 320px;
  height: 40px;
  padding: 0 var(--spacing-md);
  font-family: var(--font-sans);
  font-size: var(--body-md);
  font-weight: var(--weight-medium);
  color: var(--ink);
  background: var(--surface);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-md);
  outline: none;
  transition: border-color 0.15s var(--ease);
}
.title-input::placeholder { color: var(--muted); }
.title-input:focus { border-color: var(--primary); }
.title-input.has-error { border-color: #c0392b; }

.topnav-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

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
  box-shadow: rgba(15, 15, 15, 0.08) 0px 4px 12px 0px;
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
.dropdown-item-danger:hover { color: #c0392b; }

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
  .editor-topnav { padding: 0 var(--spacing-md); }
  .logo-text { display: none; }
  .title-input { width: 200px; }
}
</style>
