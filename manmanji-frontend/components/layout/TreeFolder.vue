<template>
  <li class="tree-folder">
    <button
      class="folder-toggle"
      :class="{ open: isOpen }"
      @click="isOpen = !isOpen"
    >
      <IconChevronRight :size="12" :class="['chevron', { open: isOpen }]" />
      <span class="folder-name">{{ folder.name }}</span>
      <span class="folder-count">{{ folder.children.length + folder.articles.length }}</span>
    </button>
    <Transition name="collapse">
      <ul v-show="isOpen" class="folder-children">
        <!-- nested folders -->
        <TreeFolder
          v-for="child in folder.children"
          :key="child.id"
          :folder="child"
          :current-article-id="currentArticleId"
          @select-article="(id: number) => $emit('selectArticle', id)"
        />
        <!-- articles -->
        <li
          v-for="article in folder.articles"
          :key="article.id"
        >
          <a
            class="tree-article"
            :class="{ active: article.id === currentArticleId }"
            href="#"
            @click.prevent="$emit('selectArticle', article.id)"
          >
            <span class="dot" />
            <span class="article-title">{{ article.title }}</span>
            <span
              v-if="article.status === 'DRAFT'"
              class="draft-tag"
            >草稿</span>
          </a>
        </li>
      </ul>
    </Transition>
  </li>
</template>

<script setup lang="ts">
import type { FolderTreeVO } from '~/types'

defineProps<{
  folder: FolderTreeVO
  currentArticleId?: number
}>()

defineEmits<{
  selectArticle: [id: number]
}>()

const isOpen = ref(true)
</script>

<style scoped>
.tree-folder {
  list-style: none;
}

.folder-toggle {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  width: 100%;
  padding: 6px 8px;
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
  margin-left: auto;
}

.folder-children {
  padding-left: var(--space-md);
}

.tree-article {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  padding: 6px 8px 6px 20px;
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

.tree-article.active {
  background: var(--surface-card);
  color: var(--ink);
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--hairline);
  flex-shrink: 0;
}

.tree-article.active .dot {
  background: var(--primary);
}

.article-title { flex: 1; }

.draft-tag {
  font-size: 11px;
  color: #E65100;
  background: #FFF3E0;
  padding: 1px 6px;
  border-radius: var(--radius-xs);
  flex-shrink: 0;
}

/* Collapse transition */
.folder-children { overflow: hidden; }
</style>
