<!--
  TopNav.vue — 顶部导航栏
  position: sticky 固定在顶部，64px 高，z-index 100 始终可见

  三区域布局（flex + space-between）：
  左侧：Logo 图标（黄底黑字"慢"）+ "慢慢记" 品牌名 + 导航链接组
  右侧：搜索框 + 登录/注册按钮（未登录时）+ 主题切换 + 用户头像（已登录时）

  Vue 3 知识点：
  - v-for: 列表渲染
  - v-if / v-else: 条件渲染
  - :class: 动态类名绑定
  - @click: 事件监听
-->
<template>
  <header class="top-nav">
    <!-- 左侧区域 -->
    <div class="nav-left">
      <!-- Logo：32×32 黄色方块 + 品牌名 -->
      <div class="nav-logo">
        <span class="logo-icon">慢</span>
        <span class="logo-text">慢慢记</span>
      </div>

      <!-- 导航链接组 -->
      <nav class="nav-links">
        <!-- v-for 遍历数组渲染多个元素，:key 是 Vue 的列表优化标识 -->
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

    <!-- 右侧区域 -->
    <div class="nav-right">
      <!-- 搜索框 -->
      <div class="nav-search">
        <IconSearch :size="16" class="search-icon" />
        <input type="text" class="search-input" placeholder="搜索文章..." />
      </div>

      <!-- v-if: 根据登录状态切换显示内容 -->
      <template v-if="!auth.isAuthenticated">
        <AppButton variant="secondary" @click="showLogin = true">登录</AppButton>
        <AppButton variant="primary" @click="showRegister = true">注册</AppButton>
      </template>
      <template v-else>
        <AppButton variant="secondary" @click="goWrite">写文章</AppButton>
      </template>

      <ThemeToggle />

      <!-- 用户头像：36×36 圆形，取昵称首字 -->
      <div v-if="auth.isAuthenticated" class="nav-avatar" :title="auth.user?.nickname">
        {{ auth.user?.nickname?.charAt(0) || '用' }}
      </div>
    </div>

    <!-- 登录弹窗 -->
    <LoginModal v-model:visible="showLogin" />
  </header>
</template>

<script setup lang="ts">
const auth = useAuthStore()

const emit = defineEmits<{ writeArticle: [] }>()

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
  emit('writeArticle')
}
</script>

<style scoped>
.top-nav {
  height: var(--nav-height);            /* 64px */
  position: sticky;                     /* 吸顶定位 */
  top: 0;
  z-index: var(--z-nav);               /* 100 — 始终在页面之上 */
  background: var(--canvas);
  border-bottom: 1px solid var(--hairline);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.18);
  padding: 0 var(--space-xxl);          /* 左右 48px */
  display: flex;
  align-items: center;
  justify-content: space-between;       /* 两端对齐 */
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

/* 小黄方块标志 */
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

/* 品牌名 18px/700 */
.logo-text {
  font-size: 18px;
  font-weight: var(--weight-bold);
  letter-spacing: -0.3px;
}

.nav-links { display: flex; gap: var(--space-sm); list-style: none; }

/* 导航链接：14px/500，padding 8px 14px，圆角 8px */
.nav-link {
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  line-height: var(--leading-normal);
  padding: 8px 14px;
  border-radius: var(--radius-md);
  color: var(--muted);                  /* 默认灰 */
  transition: var(--transition-hover);
}
.nav-link:hover,
.nav-link.active {
  color: var(--ink);                    /* hover/active 变白 */
  background: var(--surface-card);      /* 加背景 */
}

.nav-right {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-left: auto;                    /* 推到最右 */
}

/* 搜索框：宽 200px，高 40px */
.nav-search { position: relative; width: 200px; }
.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);          /* 垂直居中 */
  color: var(--muted-soft);
  pointer-events: none;                 /* 不拦截点击，点击穿透到 input */
}
.search-input {
  width: 100%;
  height: 40px;
  border-radius: var(--radius-md);
  border: 1px solid var(--hairline-strong);
  background: var(--surface-card);
  color: var(--ink);
  padding: 0 14px 0 38px;               /* 左侧留 38px 给图标 */
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  transition: var(--transition-hover);
}
.search-input::placeholder { color: var(--muted-soft); }
.search-input:focus { outline: none; border-color: var(--primary); }

/* 头像圆形 */
.nav-avatar {
  width: 36px; height: 36px;
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

/* 手机端隐藏导航链接和搜索框（可以用汉堡菜单替代） */
@media (max-width: 768px) {
  .nav-links { display: none; }
  .nav-search { display: none; }
}
</style>
