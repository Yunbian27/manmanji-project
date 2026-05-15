<!--
  RightSidebar.vue — 右侧文章目录导航栏
  固定宽度 220px，sticky 定位，滚动时跟随

  功能：
  - 自动显示文章 h2/h3 标题生成目录链接
  - active 态高亮当前阅读位置（左边框变黄）
  - 点击跳转到对应标题（scrollIntoView）
  - "回到顶部"按钮
  - 平板以下隐藏
-->
<template>
  <aside class="right-sidebar">
    <div class="toc-sticky">
      <!-- 目录标题：12px/600/全大写 -->
      <h4 class="toc-title">目录</h4>

      <!-- 有目录时渲染链接列表 -->
      <nav v-if="tocItems.length > 0" class="toc-list">
        <a
          v-for="item in tocItems"
          :key="item.id"
          :class="['toc-link', { active: item.id === activeSectionId }, item.level === 3 ? 'indent' : '']"
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
  tocItems: TocItem[]                 // 从 ArticleBody 提取的标题列表
  activeSectionId?: string            // 当前阅读到的标题 id
}>()

/** 平滑滚动到指定 id 的元素 */
function scrollTo(id: string) {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

/** 滚动到内容区顶部 */
function scrollToTop() {
  // .content-area 是 AppLayout 中主内容区的类名
  const content = document.querySelector('.content-area')
  if (content) {
    content.scrollTo({ top: 0, behavior: 'smooth' })
  }
}
</script>

<style scoped>
.right-sidebar {
  width: var(--right-sidebar-width);    /* 220px */
  flex-shrink: 0;
  background: var(--canvas);
  border-left: 1px solid var(--hairline);
  position: sticky;
  top: var(--nav-height);
  height: calc(100vh - var(--nav-height));
  overflow-y: auto;
  padding: var(--space-lg) var(--space-md);
}

.toc-sticky { position: sticky; top: var(--space-lg); }

/* 目录标题：12px/600/全大写 */
.toc-title {
  font-size: var(--text-uppercase);
  font-weight: var(--weight-semibold);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;
  color: var(--muted);
  margin-bottom: var(--space-md);
}

/* 目录链接列表 */
.toc-list { display: flex; flex-direction: column; gap: 2px; }

/* 目录链接：13px/400，左侧 2px 透明边框 */
.toc-link {
  display: block;
  padding: 6px 0;
  border-left: 2px solid transparent;
  padding-left: var(--space-sm);
  font-size: var(--text-caption);
  font-weight: var(--weight-regular);
  color: var(--muted);
  transition: var(--transition-hover);
  text-decoration: none;
}
.toc-link:hover { color: var(--ink); }

/* 激活态：左边框变黄 + 字体加粗 */
.toc-link.active {
  border-left-color: var(--primary);
  color: var(--ink);
  font-weight: var(--weight-medium);
}

/* h3 缩进：左边距 22px + 字号缩小 */
.toc-link.indent {
  padding-left: 22px;
  font-size: var(--text-uppercase);
}

.toc-empty { font-size: var(--text-caption); color: var(--muted-soft); }

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

/* 平板以下隐藏 */
@media (max-width: 1024px) {
  .right-sidebar { display: none; }
}
</style>
