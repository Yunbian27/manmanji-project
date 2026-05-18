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
    <div class="right-sidebar-inner">
      <h4 class="toc-title">本页目录</h4>

      <nav v-if="tocItems.length > 0" class="toc-list">
        <a
          v-for="item in tocItems"
          :key="item.id"
          :class="['toc-link', `level-${item.level}`, { active: item.id === activeSectionId }]"
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
  flex: 1;
  min-width: 160px;
  flex-shrink: 0;
  background: var(--canvas);
  position: sticky;
  top: var(--nav-height);
  align-self: flex-start;
}
.right-sidebar-inner {
  max-width: var(--right-sidebar-width);  /* 220px — 内容约束 (BLUEPRINT) */
  padding: var(--space-xxl) var(--space-lg) var(--space-lg) var(--space-lg);
}

/* 目录标题：16px/500 (BLUEPRINT 215) */
.toc-title {
  font-size: var(--text-body-md-strong);
  font-weight: var(--weight-medium);
  color: var(--ink);
  margin-bottom: var(--space-md);
}

/* 目录链接列表 + 垂直引导线 */
.toc-list {
  position: relative;
  display: flex;
  flex-direction: column;
}
.toc-list::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 1px;
  background: var(--hairline);
}

/* 目录链接：14px/400/hairline-mid (BLUEPRINT 218) */
.toc-link {
  position: relative;
  z-index: 1;
  display: block;
  padding: var(--space-xs) var(--space-sm);
  margin-bottom: 2px;
  border-left: 2px solid transparent;
  font-size: var(--text-body-sm);       /* 14px */
  font-weight: var(--weight-regular);   /* 400 */
  color: var(--muted-soft);
  transition: var(--transition-hover);
  text-decoration: none;
}
.toc-link:hover {
  background: var(--canvas-soft);
  color: var(--ink);
}

/* 激活态：左边框变黑 + 浅灰背景 */
.toc-link.active {
  border-left-color: var(--primary);
  background: var(--canvas-softer);
  color: var(--ink);
  font-weight: var(--weight-medium);
}

/* H3：左缩进 32px (BLUEPRINT 221) */
.toc-link.level-3 {
  padding-left: 32px;
  font-size: var(--text-caption);
  font-weight: var(--weight-regular);
  color: var(--muted);
}

/* H4：左缩进 44px */
.toc-link.level-4 {
  padding-left: 44px;
  font-size: var(--text-caption);
  font-weight: var(--weight-regular);
  color: var(--muted-soft);
}

/* H5/H6：左缩进 56px/68px */
.toc-link.level-5 {
  padding-left: 56px;
  font-size: var(--text-caption);
  font-weight: var(--weight-regular);
  color: var(--muted-soft);
}
.toc-link.level-6 {
  padding-left: 68px;
  font-size: var(--text-caption);
  font-weight: var(--weight-regular);
  color: var(--muted-soft);
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
.back-top:hover { color: var(--ink); }

/* 平板以下隐藏 (BLUEPRINT: ≤1119px) */
@media (max-width: 1119px) {
  .right-sidebar { display: none; }
}
</style>
