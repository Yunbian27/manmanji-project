<!--
  TreeFolder.vue — 递归文件夹树组件 (SIDEBAR.md §5)
  ex-app-shell-row 规范：激活态 3px 黑色左边条 + 浅灰背景
-->
<template>
  <li class="tree-folder" data-drag-type="folder">
    <!-- 文件夹标题按钮 -->
    <button
      class="folder-toggle"
      :class="{ open: isOpen }"
      @click="isOpen = !isOpen"
      @contextmenu.prevent.stop="openMenu?.($event, 'folder', folder.id)"
    >
      <span class="chevron">{{ isOpen ? '▼' : '▶' }}</span>
      <span class="folder-name">{{ folder.name }}</span>
      <!-- 数量标签（子文件夹数 + 文章数） -->
      <span class="folder-count">{{ localChildren.length + localArticles.length }}</span>
    </button>

    <!-- Transition: Vue 内置动画组件，name="collapse" 对应 transitions.css 中的类名 -->
    <Transition name="collapse">
      <div v-show="isOpen" class="folder-children">
        <!-- 子文件夹列表：group="folders" 允许跨文件夹移动子文件夹 -->
        <draggable
          tag="ul"
          :list="localChildren"
          group="folders"
          item-key="id"
          class="subfolder-list"
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
          @change="onFolderChildrenChange"
        >
          <template #item="{ element: child }">
            <TreeFolder
              :folder="child"
              :current-article-id="currentArticleId"
              @select-article="(id: number) => $emit('selectArticle', id)"
            />
          </template>
        </draggable>

        <!-- 文章列表：group="articles" 允许跨文件夹移动文章 -->
        <draggable
          tag="ul"
          :list="localArticles"
          group="articles"
          item-key="id"
          class="article-list"
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
          @change="onArticleChildrenChange"
        >
          <template #item="{ element: article }">
            <li data-drag-type="article">
              <a
                class="tree-article"
                :class="{ active: article.id === currentArticleId }"
                href="#"
                @click.prevent="$emit('selectArticle', article.id)"
                @contextmenu.prevent.stop="openMenu?.($event, 'article', article.id)"
              >
                <span class="article-title">{{ article.title }}</span>
                <!-- 未发布文章显示标签 -->
                <span v-if="article.status === 'UNPUBLISHED'" class="unpublished-tag">未发布</span>
              </a>
            </li>
          </template>
        </draggable>

        <!-- 空文件夹提示 (BLUEPRINT 7.2) -->
        <p v-if="localChildren.length === 0 && localArticles.length === 0" class="empty-folder">暂无文章</p>
      </div>
    </Transition>
  </li>
</template>

<script setup lang="ts">
import draggable from 'vuedraggable'
import type { FolderTree, ArticleItem } from '~/types'

const props = defineProps<{
  folder: FolderTree              // 文件夹数据
  currentArticleId?: number       // ? 表示可选
}>()

defineEmits<{
  selectArticle: [id: number]     // 事件名 + 参数类型
}>()

type OpenMenuFn = (event: MouseEvent, type: 'blank' | 'folder' | 'article', targetId?: number) => void
const openMenu = inject<OpenMenuFn>('openContextMenu')

const folderStore = useFolderStore()
const isOpen = computed({
  get: () => folderStore.expandedIds.includes(props.folder.id),
  set: (val: boolean) => {
    if (val) {
      if (!folderStore.expandedIds.includes(props.folder.id)) {
        folderStore.expandedIds = [...folderStore.expandedIds, props.folder.id]
      }
    } else {
      folderStore.expandedIds = folderStore.expandedIds.filter(id => id !== props.folder.id)
    }
  }
})

const collator = new Intl.Collator('zh-CN', { numeric: true })

// vuedraggable 需要可写数组，props 是只读的，维护本地副本
const localChildren = ref<FolderTree[]>(sorted([...props.folder.children]))
const localArticles = ref<ArticleItem[]>(sorted([...props.folder.articles]))

function sorted<T extends FolderTree[] | ArticleItem[]>(list: T): T {
  return list.sort((a, b) => collator.compare('name' in a ? a.name : a.title, 'name' in b ? b.name : b.title)) as T
}

// 文件夹切换时重新同步（浅监听 id 足矣，fetchFolders 会替换整个对象）
watch(() => props.folder.id, () => {
  localChildren.value = sorted([...props.folder.children])
  localArticles.value = sorted([...props.folder.articles])
})

// ---- 拖拽移动处理 ----

let cursorCleanup: (() => void) | null = null

function onDragStart(evt: any) {
  const el = evt.item as HTMLElement
  const isFolder = el.dataset.dragType === 'folder'

  const icon = document.createElement('div')
  icon.className = 'drag-cursor-icon'
  icon.setAttribute('data-drag-type', isFolder ? 'folder' : 'article')
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

function onFolderChildrenChange(evt: any) {
  if (evt.moved) {
    // 同级重排 → 恢复名称排序，不调 API
    localChildren.value = sorted([...localChildren.value])
  } else if (evt.added) {
    const { element } = evt.added
    folderStore.moveFolder(element.id, props.folder.id).catch(() => {
      folderStore.fetchFolders()
    })
  }
}

function onArticleChildrenChange(evt: any) {
  if (evt.moved) {
    localArticles.value = sorted([...localArticles.value])
  } else if (evt.added) {
    const { element } = evt.added
    folderStore.moveArticle(element.id, props.folder.id).catch(() => {
      folderStore.fetchFolders()
    })
  }
}
</script>

<style scoped>
/* 文件夹标题：16px/500, 8px 圆角 (SIDEBAR §5.4) */
.folder-toggle {
  display: flex;
  align-items: center;
  gap: var(--space-sm);                 /* 8px — 箭头与文字间距 */
  width: 100%;
  padding: var(--space-md) var(--space-lg); /* 12px 16px */
  margin-bottom: var(--space-xxs);      /* 4px */
  border-radius: var(--radius-md);      /* 8px */
  border: none;
  background: transparent;
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: 16px;
  font-weight: var(--weight-medium);    /* 500 */
  line-height: 20px;
  cursor: pointer;
  user-select: none;
  transition: var(--transition-hover);
}
.folder-toggle:hover {
  background: var(--canvas-soft);
}

/* 实心三角箭头：12px/ink, 展开 ▼ 折叠 ▶ */
.chevron {
  font-size: 12px;
  color: var(--ink);
  flex-shrink: 0;
  width: 16px;
  text-align: center;
  line-height: 1;
}

.folder-name { flex: 1; text-align: left; }

.folder-count {
  font-size: 11px;
  color: var(--muted);
  margin-left: auto;
}

/* 子项容器：左边 1px 竖线 + 16px 缩进 (SIDEBAR §5.4c) */
.folder-children {
  margin-left: var(--space-lg);         /* 16px */
  border-left: 1px solid var(--surface-elevated); /* #e2e2e2 */
  padding-left: 0;
}

/* 拖拽容器最小高度 */
.subfolder-list,
.article-list {
  min-height: 4px;
}

.sortable-drag { opacity: 0 !important; }
.sortable-chosen { opacity: 0.4; }

/* 文章项：14px/400/body, 8px 圆角 (SIDEBAR §5.5) */
.tree-article {
  display: block;
  padding: var(--space-md) var(--space-lg); /* 12px 16px */
  border-radius: var(--radius-md);      /* 8px */
  color: var(--body);                   /* #5e5e5e */
  font-size: var(--text-body-sm);       /* 14px */
  font-weight: var(--weight-regular);   /* 400 */
  line-height: 20px;
  transition: var(--transition-hover);
  text-decoration: none;
  position: relative;
}
.tree-article:hover {
  background: var(--canvas-soft);
  color: var(--ink);
}

/* 激活态：3px 黑色左边条 + 浅灰底 + 500 字重 (SIDEBAR §5.5) */
.tree-article.active {
  font-weight: var(--weight-medium);    /* 500 */
  color: var(--ink);
  background: var(--canvas-soft);
  border-left: 3px solid var(--primary);
  padding-left: calc(var(--space-lg) - 3px); /* 16px - 3px = 13px */
}

.article-title { /* text content, no flex needed */ }

/* 草稿标签 */
.unpublished-tag {
  font-size: 11px;
  color: var(--muted);
  background: var(--canvas-softer);
  padding: 1px 6px;
  border-radius: var(--radius-pill);
  flex-shrink: 0;
  margin-left: var(--space-xs);
}

/* 空文件夹提示：12px, mute 色 (SIDEBAR §5.6) */
.empty-folder {
  font-size: var(--text-caption);       /* 12px */
  font-weight: var(--weight-regular);
  line-height: 20px;
  color: var(--muted);                  /* #afafaf */
  padding: var(--space-md) var(--space-lg); /* 12px 16px */
  margin: 0;
}
</style>
