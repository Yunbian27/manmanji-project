<template>
  <div class="sidebar-header">
    <span class="sidebar-logo">
      <img src="/favicon.svg" alt="慢慢记" class="sidebar-logo-mark" />
      慢慢记
    </span>
    <div ref="avatarContainer" class="avatar-wrapper">
      <div class="sidebar-avatar" :title="auth.user?.nickname" @click="showDropdown = !showDropdown">
        <img v-if="auth.user?.avatarUrl && !avatarError" :src="auth.user!.avatarUrl" class="avatar-img" @error="onAvatarError" />
        <IconLucideUser v-else />
      </div>
      <Transition name="dropdown">
        <div v-if="showDropdown" class="avatar-dropdown">
          <div class="dropdown-user">
            <img v-if="auth.user?.avatarUrl && !avatarError" :src="auth.user!.avatarUrl" class="dropdown-avatar" @error="onAvatarError" />
            <span v-else class="dropdown-avatar dropdown-avatar--fallback">
              <IconLucideUser />
            </span>
            <div>
              <p class="dropdown-nickname">{{ auth.user?.nickname }}</p>
              <p class="dropdown-username">{{ auth.user?.username }}</p>
            </div>
          </div>
          <button class="dropdown-item" @click="router.push('/manage?tab=profile')">
            <IconLucideUser />
            个人资料
          </button>
          <button class="dropdown-item" @click="router.push('/manage?tab=articles')">
            <IconLucideFileText />
            内容管理
          </button>
          <div class="dropdown-divider" />
          <button class="dropdown-item dropdown-item-danger" @click="handleLogout">
            <IconLucideLogOut />
            退出登录
          </button>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onClickOutside } from '@vueuse/core'
import IconLucideFileText from '~icons/lucide/file-text'
import IconLucideLogOut from '~icons/lucide/log-out'
import IconLucideUser from '~icons/lucide/user'

const router = useRouter()
const auth = useAuthStore()

const showDropdown = ref(false)
const avatarContainer = ref<HTMLElement | null>(null)
const avatarError = ref(false)
function onAvatarError() { avatarError.value = true }

onClickOutside(avatarContainer, () => { showDropdown.value = false })

function handleLogout() {
  auth.clearSession()
  router.push('/login')
}
</script>

<style scoped>
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm) var(--spacing-md);
  flex-shrink: 0;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-family: var(--font-sans);
  font-size: var(--heading-5);
  font-weight: var(--weight-semibold);
  color: var(--ink);
}

.sidebar-logo-mark {
  width: 28px; height: 28px;
  border-radius: var(--rounded-sm);
  flex-shrink: 0;
}

.sidebar-avatar {
  width: 32px; height: 32px;
  border-radius: var(--rounded-full);
  background: var(--canvas);
  color: var(--ink);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  cursor: pointer;
  transition: background 0.15s var(--ease);
  border: none;
  overflow: hidden;
}
.sidebar-avatar:hover { background: var(--hairline-soft); }

.avatar-img {
  width: 100%; height: 100%;
  object-fit: cover;
}

.avatar-wrapper { position: relative; flex-shrink: 0; }

.avatar-dropdown {
  position: absolute;
  top: calc(100% + 16px);
  left: 0;
  min-width: 280px;
  padding: var(--spacing-md);
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-lg);
  box-shadow: rgba(15, 15, 15, 0.16) 0px 16px 48px -8px;
  z-index: var(--z-modal);
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
</style>
