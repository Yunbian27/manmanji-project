<template>
  <aside class="right-sidebar">
    <div class="toc-sticky">
      <h4 class="toc-title">目录</h4>
      <nav v-if="tocItems.length > 0" class="toc-list">
        <a
          v-for="item in tocItems"
          :key="item.id"
          :class="[
            'toc-link',
            { active: item.id === activeSectionId },
            item.level === 3 ? 'indent' : '',
          ]"
          @click.prevent="scrollTo(item.id)"
        >
          {{ item.text }}
        </a>
      </nav>
      <p v-else class="toc-empty">暂无标题</p>
      <button class="back-top" @click="scrollToTop">回到顶部</button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import type { TocItem } from '~/types'

defineProps<{
  tocItems: TocItem[]
  activeSectionId?: string
}>()

function scrollTo(id: string) {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function scrollToTop() {
  const content = document.querySelector('.content-area')
  if (content) {
    content.scrollTo({ top: 0, behavior: 'smooth' })
  }
}
</script>

<style scoped>
.right-sidebar {
  width: var(--right-sidebar-width);
  flex-shrink: 0;
  background: var(--canvas);
  border-left: 1px solid var(--hairline);
  position: sticky;
  top: var(--nav-height);
  height: calc(100vh - var(--nav-height));
  overflow-y: auto;
  padding: var(--space-lg) var(--space-md);
}

.toc-sticky {
  position: sticky;
  top: var(--space-lg);
}

.toc-title {
  font-size: var(--text-uppercase);
  font-weight: var(--weight-semibold);
  line-height: var(--leading-normal);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;
  color: var(--muted);
  margin-bottom: var(--space-md);
}

.toc-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.toc-link {
  display: block;
  padding: 6px 0;
  border-left: 2px solid transparent;
  padding-left: var(--space-sm);
  font-size: var(--text-caption);
  font-weight: var(--weight-regular);
  line-height: var(--leading-normal);
  color: var(--muted);
  transition: var(--transition-hover);
  text-decoration: none;
}

.toc-link:hover { color: var(--ink); }

.toc-link.active {
  border-left-color: var(--primary);
  color: var(--ink);
  font-weight: var(--weight-medium);
}

.toc-link.indent {
  padding-left: 22px;
  font-size: var(--text-uppercase);
}

.toc-empty {
  font-size: var(--text-caption);
  color: var(--muted-soft);
}

.back-top {
  margin-top: var(--space-lg);
  font-size: var(--text-caption);
  color: var(--muted);
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0;
  transition: var(--transition-hover);
}

.back-top:hover { color: var(--primary); }

@media (max-width: 1024px) {
  .right-sidebar { display: none; }
}
</style>
