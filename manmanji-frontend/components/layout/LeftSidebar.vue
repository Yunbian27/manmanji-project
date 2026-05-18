<!--
  LeftSidebar.vue — 左侧文件夹树 (SIDEBAR.md §4)
  sticky 定位，可折叠文件夹树，激活项带 3px 黑色左边条
-->
<template>
  <aside class="left-sidebar" :class="{ 'mobile-open': mobileOpen }">
    <div class="sidebar-inner">
      <div class="sidebar-header">
        <h3 class="sidebar-title">文章列表</h3>
        <div class="plus-btn-wrap">
          <button class="sidebar-plus-btn" @click.stop="showDropdown = !showDropdown" aria-label="新建">+</button>
          <Transition name="dropdown">
            <div v-if="showDropdown" class="plus-dropdown">
              <button class="dropdown-item" @click.stop="onCreateFolder">新建文件夹</button>
              <button class="dropdown-item" @click.stop="onNewArticle">新建文章</button>
            </div>
          </Transition>
        </div>
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
      <div v-else class="tree-list">
        <draggable
          tag="ul"
          :list="folderStore.folders"
          group="folders"
          item-key="id"
          class="root-folder-list"
          ghost-class="sortable-ghost"
          drag-class="sortable-drag"
          chosen-class="sortable-chosen"
          :force-fallback="true"
          :scroll="true"
          :scroll-sensitivity="50"
          :scroll-speed="15"
          :bubble-scroll="true"
          @start="onDragStart"
          @end="onDragEnd"
          @change="onRootFolderChange"
        >
          <template #item="{ element: folder }">
            <TreeFolder
              :folder="folder"
              :current-article-id="currentArticleId"
              @select-article="(id: number) => $emit('selectArticle', id)"
            />
          </template>
        </draggable>
      </div>

    </div>
  </aside>
</template>

<script setup lang="ts">
import draggable from 'vuedraggable'
import type { FolderTreeVO } from '~/types'

defineProps<{
  folders: FolderTreeVO[]
  loading?: boolean
  error?: string | null
  currentArticleId?: number
  mobileOpen?: boolean
}>()

const emit = defineEmits<{
  selectArticle: [id: number]
  newArticle: []
  createFolder: []
  retry: []
}>()

const showDropdown = ref(false)

function onCreateFolder() {
  showDropdown.value = false
  emit('createFolder')
}

function onNewArticle() {
  showDropdown.value = false
  emit('newArticle')
}

// 点击其他区域关闭下拉
onMounted(() => {
  document.addEventListener('click', () => { showDropdown.value = false })
})

// ---- 根级文件夹拖拽移动 ----
const folderStore = useFolderStore()
const collator = new Intl.Collator('zh-CN', { numeric: true })

let cursorCleanup: (() => void) | null = null

function onDragStart(evt: any) {
  const icon = document.createElement('div')
  icon.className = 'drag-cursor-icon'
  icon.setAttribute('data-drag-type', 'folder')
  icon.style.cssText = 'position:fixed;width:32px;height:32px;pointer-events:none;z-index:99999'
  document.body.appendChild(icon)

  const onMove = (e: MouseEvent) => {
    icon.style.left = (e.clientX - 16) + 'px'
    icon.style.top = (e.clientY - 16) + 'px'
  }
  const onEnd = () => {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onEnd)
    icon.remove()
    cursorCleanup = null
  }
  cursorCleanup = onEnd
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onEnd)

  const native = evt.originalEvent as MouseEvent
  onMove(native || evt)
}

function onDragEnd() {
  cursorCleanup?.()
}

function onRootFolderChange(evt: any) {
  if (evt.moved) {
    // 同级重排 → 恢复名称排序，不调 API
    folderStore.folders.sort((a, b) => collator.compare(a.name, b.name))
  } else if (evt.added) {
    const { element } = evt.added
    folderStore.moveFolder(element.id, null).catch(() => {
      folderStore.fetchFolders()
    })
  }
}
</script>

<style scoped>
/* 左侧栏：flex:1 拉伸，内容右对齐 (推内容至中间) */
.left-sidebar {
  flex: 1;
  min-width: 250px;
  flex-shrink: 0;
  background: var(--canvas);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: var(--nav-height);
  max-height: calc(100vh - var(--nav-height));
  overflow-y: auto;
  align-self: flex-start;
  padding-top: var(--space-xxl);
}

.sidebar-inner {
  width: 100%;
  max-width: var(--sidebar-max);        /* 280px — 内容不超宽 */
  margin-left: auto;                    /* 右对齐贴紧中间栏 */
}

/* 头部：标题 + 按钮 (SIDEBAR §5.2) */
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-lg);       /* 16px */
  padding: 0 var(--space-md);
  flex-shrink: 0;
}
.sidebar-title {
  font-size: var(--display-sm);         /* 20px */
  font-weight: var(--weight-bold);      /* 700 */
  line-height: 28px;
  color: var(--ink);
}

/* 圆形新建按钮 (SIDEBAR: 32×32) */
.sidebar-plus-btn {
  width: 32px; height: 32px;
  border-radius: var(--radius-full);
  border: none;
  background: var(--canvas-soft);
  color: var(--ink);
  font-size: 18px;
  font-family: var(--font-sans);
  font-weight: var(--weight-regular);
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition-hover);
}
.sidebar-plus-btn:hover { background: var(--surface-elevated); }

/* "+"下拉菜单 */
.plus-btn-wrap {
  position: relative;
}
.plus-dropdown {
  position: absolute;
  right: 0;
  top: 100%;
  margin-top: 4px;
  min-width: 120px;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-2);
  overflow: hidden;
  z-index: var(--z-nav);
}
.dropdown-item {
  display: block;
  width: 100%;
  padding: var(--space-sm) var(--space-md);
  border: none;
  background: transparent;
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  color: var(--ink);
  cursor: pointer;
  text-align: left;
  transition: var(--transition-hover);
}
.dropdown-item:hover { background: var(--canvas-soft); }

.dropdown-enter-active,
.dropdown-leave-active { transition: opacity 0.12s ease, transform 0.12s ease; }
.dropdown-enter-from,
.dropdown-leave-to { opacity: 0; transform: translateY(-4px); }

.tree-list {
  padding: var(--space-sm);
}

.root-folder-list {
  min-height: 40px;                   /* 确保空列表也能接收拖入 */
}

/* 拖拽占位符：原位置的半透明占位 */
.sortable-ghost {
  opacity: 0.3;
  background: var(--surface-elevated);
  border-radius: var(--radius-md);
}

/* mirror 隐身 — 只做 SortableJS 的拖拽载体，实际图标由 JS 追踪光标渲染 */
.sortable-drag {
  opacity: 0 !important;
}

/* chosenClass 加在原始元素上 — 仅半透明 */
.sortable-chosen {
  opacity: 0.4;
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

/* 响应式 (BLUEPRINT: Tablet ≤1119px, Mobile <600px) */
@media (max-width: 1119px) {
  .left-sidebar { min-width: 200px; max-width: 280px; }
}
@media (max-width: 599px) {
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
