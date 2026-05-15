<!--
  LeftSidebar.vue — 左侧文件夹分类栏
  布局：flex:1（填充左侧剩余空间），内部 sidebar-inner 右对齐
  包含：分类标题 + 新建按钮 + 文件夹树 + 底部操作按钮

  四种状态：
  - loading: 骨架屏占位
  - error: 错误信息 + 重试按钮
  - empty: "暂无分类" 提示
  - data: 渲染 TreeFolder 递归树
-->
<template>
  <aside class="left-sidebar" :class="{ 'mobile-open': mobileOpen }">
    <!-- sidebar-inner: 内容右对齐容器，max-width 280px -->
    <div class="sidebar-inner">
      <!-- 顶部：分类标题 + 新建按钮 -->
      <div class="sidebar-header">
        <h3 class="sidebar-title">文章分类</h3>
        <button class="sidebar-new-btn" @click="$emit('newFolder')" aria-label="新建文件夹">
          <IconPlus :size="16" />
        </button>
      </div>

      <!-- v-if / v-else-if / v-else: Vue 的条件渲染链 -->
      <!-- 状态1：加载中 — 骨架屏 -->
      <div v-if="loading" class="sidebar-loading">
        <div v-for="i in 4" :key="i" class="skeleton-folder">
          <div class="skeleton-line w-60" />
        </div>
      </div>

      <!-- 状态2：加载出错 — 提示 + 重试 -->
      <div v-else-if="error" class="sidebar-error">
        <p>{{ error }}</p>
        <button class="retry-btn" @click="$emit('retry')">重试</button>
      </div>

      <!-- 状态3：空数据 — 引导提示 -->
      <div v-else-if="folders.length === 0" class="sidebar-empty">
        <p>暂无分类</p>
        <p class="hint">创建你的第一个文件夹</p>
      </div>

      <!-- 状态4：正常 — 渲染文件夹树 -->
      <ul v-else class="tree-list">
        <TreeFolder
          v-for="folder in folders"
          :key="folder.id"
          :folder="folder"
          :current-article-id="currentArticleId"
          @select-article="(id: number) => $emit('selectArticle', id)"
        />
      </ul>

      <!-- 底部操作按钮 -->
      <div class="sidebar-footer">
        <AppButton variant="primary" @click="$emit('newFolder')">新建文件夹</AppButton>
        <AppButton variant="secondary" @click="$emit('newArticle')">新文章</AppButton>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import type { FolderTreeVO } from '~/types'

defineProps<{
  folders: FolderTreeVO[]
  loading?: boolean
  error?: string | null
  currentArticleId?: number
  mobileOpen?: boolean            // 移动端侧边栏是否打开
}>()

defineEmits<{
  selectArticle: [id: number]
  newFolder: []
  newArticle: []
  retry: []
}>()
</script>

<style scoped>
/* 左侧栏：flex:1 填充空间，min/max 约束宽度范围 */
.left-sidebar {
  flex: 1;
  min-width: 240px;
  max-width: 400px;
  flex-shrink: 0;                     /* 不会被压缩 */
  background: var(--surface-soft);
  border-right: 1px solid var(--hairline-strong);
  display: flex;
  flex-direction: column;
  position: sticky;                   /* 跟随滚动吸顶 */
  top: var(--nav-height);             /* 吸在导航栏下方 */
  height: calc(100vh - var(--nav-height));  /* 全屏减去导航高度 */
  overflow: hidden;
}

/* 内容右对齐：max-width 280px + margin-left: auto */
.sidebar-inner {
  max-width: var(--sidebar-max);
  margin-left: auto;
  width: 100%;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

/* 标题区：12px/600/全大写，底边分割线 */
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-sm) var(--space-md);
  border-bottom: 1px solid var(--hairline);
  flex-shrink: 0;
}
.sidebar-title {
  font-size: var(--text-uppercase);
  font-weight: var(--weight-semibold);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;
  color: var(--muted);
}
.sidebar-new-btn {
  width: 28px; height: 28px;
  border-radius: var(--radius-sm);
  border: none;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition-hover);
}
.sidebar-new-btn:hover { background: var(--surface-card); color: var(--ink); }

.tree-list {
  flex: 1;
  overflow-y: auto;                   /* 文件夹多时滚动 */
  padding: var(--space-sm);
}

.sidebar-footer {
  display: flex;
  gap: var(--space-xs);
  padding: var(--space-sm) var(--space-md);
  border-top: 1px solid var(--hairline);
  flex-shrink: 0;
}

/* 骨架屏（loading 状态） */
.sidebar-loading { padding: var(--space-md); }
.skeleton-folder { padding: 6px 8px; }
.skeleton-line { height: 14px; background: var(--surface-elevated); border-radius: 4px; }
.w-60 { width: 60%; }

/* 错误/空状态 */
.sidebar-error { padding: var(--space-md); text-align: center; color: var(--muted); }
.retry-btn {
  margin-top: var(--space-xs);
  font-size: var(--text-caption);
  color: var(--primary);
  background: transparent;
  border: none;
  cursor: pointer;
}
.sidebar-empty { padding: var(--space-md); text-align: center; color: var(--muted); }
.sidebar-empty .hint { font-size: var(--text-caption); color: var(--muted-soft); margin-top: var(--space-xxs); }

/* 响应式 */
@media (max-width: 1200px) {
  .left-sidebar { min-width: 200px; max-width: 280px; }
}
@media (max-width: 768px) {
  /* 手机端：隐藏为全屏抽屉，通过 .mobile-open 显示 */
  .left-sidebar {
    display: none;
    position: fixed;
    top: var(--nav-height);
    left: 0;
    bottom: 0;
    width: 280px;
    z-index: var(--z-nav);
    min-width: unset; max-width: unset;
  }
  .left-sidebar.mobile-open { display: flex; }
}
</style>
