<!--
  EditorTopNav.vue — 编辑器顶部导航栏
  常驻顶部不随滚动消失
  左侧: logo + 返回 + 标题输入 | 右侧: 用户头像
-->
<template>
  <header class="editor-topnav">
    <div class="topnav-left">
      <div class="nav-logo">
        <span class="logo-icon">慢</span>
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
          {{ auth.user?.nickname?.charAt(0) || '用' }}
        </div>
        <Transition name="dropdown">
          <div v-if="showDropdown" class="avatar-dropdown">
            <div class="dropdown-user">
              <p class="dropdown-nickname">{{ auth.user?.nickname }}</p>
              <p class="dropdown-username">{{ auth.user?.username }}</p>
            </div>
            <div class="dropdown-divider" />
            <button class="dropdown-item dropdown-item-danger" @click="handleLogout">
              退出登录
            </button>
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
const { title, titleError, content } = editor

const showDropdown = ref(false)
const avatarContainer = ref<HTMLElement | null>(null)

onClickOutside(avatarContainer, () => {
  showDropdown.value = false
})

function onTitleInput(e: Event) {
  title.value = (e.target as HTMLInputElement).value
}

function handleBack() {
  if (content.value || title.value) {
    if (!confirm('有未保存的内容，确定返回吗？')) return
  }
  editor.stopAutoSave()
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
  height: var(--nav-height);
  position: sticky;
  top: 0;
  z-index: var(--z-nav);
  background: var(--canvas);
  border-bottom: 1px solid var(--hairline);
  padding: 0 var(--space-3xl);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.topnav-left {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
  flex-shrink: 0;
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
  color: var(--on-primary);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-body-md-strong);
  font-weight: var(--weight-bold);
}

.logo-text {
  font-size: 18px;
  font-weight: var(--weight-bold);
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-xxs);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  color: var(--muted);
  background: transparent;
  border: none;
  cursor: pointer;
  padding: var(--space-xxs) var(--space-xs);
  border-radius: var(--radius-md);
  transition: color 0.15s ease;
}
.back-btn:hover { color: var(--ink); }
.back-icon { transform: rotate(180deg); flex-shrink: 0; }

.title-input {
  width: 320px;
  height: 40px;
  padding: 0 var(--space-md);
  font-family: var(--font-sans);
  font-size: var(--text-body-md-strong);
  font-weight: var(--weight-medium);
  color: var(--ink);
  background: var(--canvas-soft);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-pill);
  outline: none;
  transition: border-color 0.15s ease;
}
.title-input::placeholder { color: var(--muted-soft); }
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
  border-radius: var(--radius-full);
  background: var(--canvas-soft);
  color: var(--ink);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  flex-shrink: 0;
  cursor: pointer;
  transition: var(--transition-hover);
}
.nav-avatar:hover { background: var(--surface-elevated); }

.avatar-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 160px;
  padding: var(--space-xs);
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-2);
}

.dropdown-user { padding: 8px 12px; }
.dropdown-nickname {
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  color: var(--ink);
  margin: 0;
  line-height: var(--leading-normal);
}
.dropdown-username {
  font-size: var(--text-caption);
  color: var(--muted);
  margin: 2px 0 0;
  line-height: var(--leading-normal);
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
  border-radius: var(--radius-pill);
  background: transparent;
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-regular);
  line-height: var(--leading-normal);
  text-align: left;
  cursor: pointer;
  transition: var(--transition-hover);
}
.dropdown-item:hover { background: var(--canvas-soft); }
.dropdown-item-danger:hover { color: #c0392b; }

.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

@media (max-width: 599px) {
  .editor-topnav { padding: 0 var(--space-lg); }
  .logo-text { display: none; }
  .title-input { width: 200px; }
}
</style>
