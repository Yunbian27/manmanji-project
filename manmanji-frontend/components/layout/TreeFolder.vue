<!--
  TreeFolder.vue — 递归文件夹树组件
  Vue 中组件可以递归调用自己：TreeFolder 内部可以嵌套 TreeFolder
  通过 name 属性或文件名自动识别

  Props（父→子）:
  - folder: 文件夹数据（含子文件夹和文章）
  - currentArticleId: 当前选中的文章 id

  Events（子→父）:
  - selectArticle: 用户点击某篇文章时触发，传递文章 id

  交互：
  - 点击文件夹标题 → 折叠/展开（isOpen 状态切换）
  - 点击文章标题 → 高亮选中（active 类 + dot 变黄）
  - 草稿文章 → 显示橙色"草稿"标签
-->
<template>
  <li class="tree-folder">
    <!-- 文件夹标题按钮 -->
    <button
      class="folder-toggle"
      :class="{ open: isOpen }"
      @click="isOpen = !isOpen"
      @contextmenu.prevent.stop="openMenu?.($event, 'folder', folder.id)"
    >
      <!-- 箭头图标：展开后旋转 90deg -->
      <IconChevronRight :size="12" :class="['chevron', { open: isOpen }]" />
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
          @change="onArticleChildrenChange"
        >
          <template #item="{ element: article }">
            <li>
              <a
                class="tree-article"
                :class="{ active: article.id === currentArticleId }"
                href="#"
                @click.prevent="$emit('selectArticle', article.id)"
                @contextmenu.prevent.stop="openMenu?.($event, 'article', article.id)"
              >
                <!-- 小圆点指示器，选中时变黄 -->
                <span class="dot" />
                <span class="article-title">{{ article.title }}</span>
                <!-- 草稿文章显示橙色标签 -->
                <span v-if="article.status === 'DRAFT'" class="draft-tag">草稿</span>
              </a>
            </li>
          </template>
        </draggable>
      </div>
    </Transition>
  </li>
</template>

<script setup lang="ts">
import draggable from 'vuedraggable'
import type { FolderTreeVO, ArticleItem } from '~/types'

const props = defineProps<{
  folder: FolderTreeVO            // 文件夹数据
  currentArticleId?: number       // ? 表示可选
}>()

defineEmits<{
  selectArticle: [id: number]     // 事件名 + 参数类型
}>()

type OpenMenuFn = (event: MouseEvent, type: 'blank' | 'folder' | 'article', targetId?: number) => void
const openMenu = inject<OpenMenuFn>('openContextMenu')

const folderStore = useFolderStore()
const isOpen = ref(true)

// vuedraggable 需要直接修改数组，但 props 是只读的。
// 因此维护本地可写副本，通过 sync 函数与 store 同步。
const localChildren = ref<FolderTreeVO[]>([...props.folder.children])
const localArticles = ref<ArticleItem[]>([...props.folder.articles])
const isSyncing = ref(false)

// 外部变化（props from store）→ 同步到本地 ref
watch(
  () => [props.folder.children, props.folder.articles],
  () => {
    if (isSyncing.value) return
    localChildren.value = [...props.folder.children]
    localArticles.value = [...props.folder.articles]
  },
  { deep: true },
)

// 本地 ref 变化（拖拽操作）→ 同步到 store 树
function syncToStore() {
  isSyncing.value = true
  const folder = folderStore.folderMap.get(props.folder.id)
  if (folder) {
    folder.children.splice(0, folder.children.length, ...localChildren.value)
    folder.articles.splice(0, folder.articles.length, ...localArticles.value)
  }
  nextTick(() => { isSyncing.value = false })
}

// ---- 拖拽排序/移动处理 ----

function onFolderChildrenChange(evt: any) {
  syncToStore()

  if (evt.moved) {
    const { element, newIndex } = evt.moved
    folderStore.moveFolder(element.id, props.folder.id, newIndex).catch(() => {
      folderStore.fetchFolders()
    })
  } else if (evt.added) {
    const { element, newIndex } = evt.added
    folderStore.moveFolder(element.id, props.folder.id, newIndex).catch(() => {
      folderStore.fetchFolders()
    })
  }
}

function onArticleChildrenChange(evt: any) {
  syncToStore()

  if (evt.moved) {
    const { element, newIndex } = evt.moved
    folderStore.moveArticle(element.id, props.folder.id, newIndex).catch(() => {
      folderStore.fetchFolders()
    })
  } else if (evt.added) {
    const { element, newIndex } = evt.added
    folderStore.moveArticle(element.id, props.folder.id, newIndex).catch(() => {
      folderStore.fetchFolders()
    })
  }
}
</script>

<style scoped>
/* 文件夹标题：14px/500，padding 6px 8px，圆角 8px */
.folder-toggle {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  width: 100%;
  padding: var(--space-xs) var(--space-sm);
  margin-bottom: 2px;
  border-radius: var(--radius-md);
  border: none;
  background: transparent;
  color: var(--body);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  line-height: var(--leading-normal);
  cursor: pointer;
  transition: var(--transition-hover);
}
.folder-toggle:hover {
  background: rgba(250, 255, 105, 0.07);
  color: var(--ink);
  border-left: 3px solid var(--primary);
  padding-left: 9px; /* 原 12px 减 3px 抵消 border */
}

.folder-name { flex: 1; text-align: left; }

.folder-count {
  font-size: 11px;
  color: var(--muted);
  margin-left: auto;                    /* 推到右侧 */
}

.folder-children { padding-left: var(--space-md); }

/* 拖拽容器最小高度，确保空容器也能作为拖放目标 */
.subfolder-list,
.article-list {
  min-height: 4px;
}

/* 文件夹标题作为拖放悬停目标时的高亮 */
.folder-toggle.drop-hover {
  outline: 2px dashed var(--primary);
  outline-offset: -2px;
  border-radius: var(--radius-md);
}

/* 文章项：13px/400，padding 6px 8px 6px 20px，圆角 6px */
.tree-article {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  padding: var(--space-xs) var(--space-sm) var(--space-xs) 24px;           /* 左缩进 24px */
  margin-bottom: 2px;
  border-radius: var(--radius-sm);
  color: var(--muted);
  font-size: var(--text-caption);
  font-weight: var(--weight-regular);
  line-height: var(--leading-normal);
  transition: var(--transition-hover);
  text-decoration: none;
}
.tree-article:hover {
  background: rgba(250, 255, 105, 0.07);
  color: var(--ink);
  border-left: 3px solid var(--primary);
  padding-left: 21px; /* 原 24px 减 3px 抵消 border */
}
.tree-article:hover .dot {
  background: var(--primary);
}

/* 选中态：背景加深 + 文字变白 + 黄色圆点 */
.tree-article.active {
  background: var(--surface-elevated);
  color: var(--ink);
}

/* 小圆点：6px 直径，默认灰色，选中变黄 */
.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--hairline);
  flex-shrink: 0;
}
.tree-article.active .dot { background: var(--primary); }

.article-title { flex: 1; }

/* 草稿标签：橙色文字 + 浅橙底色 */
.draft-tag {
  font-size: 11px;
  color: #E65100;
  background: #FFF3E0;
  padding: 1px 6px;
  border-radius: var(--radius-xs);
  flex-shrink: 0;
}
</style>
