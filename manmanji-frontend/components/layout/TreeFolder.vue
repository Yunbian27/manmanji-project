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
    >
      <!-- 箭头图标：展开后旋转 90deg -->
      <IconChevronRight :size="12" :class="['chevron', { open: isOpen }]" />
      <span class="folder-name">{{ folder.name }}</span>
      <!-- 数量标签（子文件夹数 + 文章数） -->
      <span class="folder-count">{{ folder.children.length + folder.articles.length }}</span>
    </button>

    <!-- Transition: Vue 内置动画组件，name="collapse" 对应 transitions.css 中的类名 -->
    <Transition name="collapse">
      <ul v-show="isOpen" class="folder-children">
        <!-- 递归！TreeFolder 嵌套自己渲染子文件夹 -->
        <TreeFolder
          v-for="child in folder.children"
          :key="child.id"
          :folder="child"
          :current-article-id="currentArticleId"
          @select-article="(id: number) => $emit('selectArticle', id)"
        />

        <!-- 当前文件夹下的文章列表 -->
        <li v-for="article in folder.articles" :key="article.id">
          <a
            class="tree-article"
            :class="{ active: article.id === currentArticleId }"
            href="#"
            @click.prevent="$emit('selectArticle', article.id)"
          >
            <!-- 小圆点指示器，选中时变黄 -->
            <span class="dot" />
            <span class="article-title">{{ article.title }}</span>
            <!-- 草稿文章显示橙色标签 -->
            <span v-if="article.status === 'DRAFT'" class="draft-tag">草稿</span>
          </a>
        </li>
      </ul>
    </Transition>
  </li>
</template>

<script setup lang="ts">
import type { FolderTreeVO } from '~/types'

// defineProps 声明组件接收的属性（带类型安全）
defineProps<{
  folder: FolderTreeVO            // 文件夹数据
  currentArticleId?: number       // ? 表示可选
}>()

// defineEmits 声明组件对外发送的事件
defineEmits<{
  selectArticle: [id: number]     // 事件名 + 参数类型
}>()

// 每个文件夹独立管理自己的展开/折叠状态
const isOpen = ref(true)          // 默认展开
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
  background: var(--surface-card);
  color: var(--ink);
}

.folder-name { flex: 1; text-align: left; }

.folder-count {
  font-size: 11px;
  color: var(--muted);
  margin-left: auto;                    /* 推到右侧 */
}

.folder-children { padding-left: var(--space-md); }

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
  background: var(--surface-card);
  color: var(--ink);
}

/* 选中态：背景加深 + 文字变白 */
.tree-article.active {
  background: var(--surface-card);
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
