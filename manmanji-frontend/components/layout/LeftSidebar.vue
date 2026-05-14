<template>
  <aside class="left-sidebar" :class="{ 'mobile-open': mobileOpen }">
    <div class="sidebar-inner">
      <div class="sidebar-header">
        <h3 class="sidebar-title">文章分类</h3>
        <button class="sidebar-new-btn" @click="$emit('newFolder')" aria-label="新建文件夹">
          <IconPlus :size="16" />
        </button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="sidebar-loading">
        <div v-for="i in 4" :key="i" class="skeleton-folder">
          <div class="skeleton-line w-60" />
        </div>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="sidebar-error">
        <p>{{ error }}</p>
        <button class="retry-btn" @click="retry">重试</button>
      </div>

      <!-- Empty -->
      <div v-else-if="folders.length === 0" class="sidebar-empty">
        <p>暂无分类</p>
        <p class="hint">创建你的第一个文件夹</p>
      </div>

      <!-- Tree -->
      <ul v-else class="tree-list">
        <TreeFolder
          v-for="folder in folders"
          :key="folder.id"
          :folder="folder"
          :current-article-id="currentArticleId"
          @select-article="(id: number) => $emit('selectArticle', id)"
        />
      </ul>

      <div class="sidebar-footer">
        <AppButton variant="primary" @click="$emit('newFolder')">
          新建文件夹
        </AppButton>
        <AppButton variant="secondary" @click="$emit('newArticle')">
          新文章
        </AppButton>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import type { FolderTreeVO } from '~/types'

const props = defineProps<{
  folders: FolderTreeVO[]
  loading?: boolean
  error?: string | null
  currentArticleId?: number
  mobileOpen?: boolean
}>()

defineEmits<{
  selectArticle: [id: number]
  newFolder: []
  newArticle: []
  retry: []
}>()

function retry() {
  // handled by parent
}
</script>

<style scoped>
.left-sidebar {
  flex: 1;
  min-width: 240px;
  max-width: 400px;
  flex-shrink: 0;
  background: var(--canvas);
  border-right: 1px solid var(--hairline);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: var(--nav-height);
  height: calc(100vh - var(--nav-height));
  overflow: hidden;
}

.sidebar-inner {
  max-width: var(--sidebar-max);
  margin-left: auto;
  width: 100%;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-sm);
  border-bottom: 1px solid var(--hairline);
  flex-shrink: 0;
}

.sidebar-title {
  font-size: var(--text-uppercase);
  font-weight: var(--weight-semibold);
  line-height: var(--leading-normal);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;
  color: var(--muted);
}

.sidebar-new-btn {
  width: 28px;
  height: 28px;
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

.sidebar-new-btn:hover {
  background: var(--surface-card);
  color: var(--ink);
}

.tree-list {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-xs);
}

.sidebar-footer {
  display: flex;
  gap: var(--space-xs);
  padding: var(--space-sm);
  border-top: 1px solid var(--hairline);
  flex-shrink: 0;
}

.sidebar-footer :deep(.app-btn) {
  flex: 1;
  height: 36px;
  font-size: 13px;
}

/* States */
.sidebar-loading { padding: var(--space-md); }
.skeleton-folder { padding: 6px 8px; }
.skeleton-line { height: 14px; background: var(--surface-elevated); border-radius: 4px; }
.skeleton-line.w-60 { width: 60%; }

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

@media (max-width: 1200px) {
  .left-sidebar { min-width: 200px; max-width: 280px; }
}
@media (max-width: 768px) {
  .left-sidebar {
    display: none;
    position: fixed;
    top: var(--nav-height);
    left: 0;
    bottom: 0;
    width: 280px;
    z-index: var(--z-nav);
    min-width: unset;
    max-width: unset;
  }
  .left-sidebar.mobile-open { display: flex; }
}
</style>
