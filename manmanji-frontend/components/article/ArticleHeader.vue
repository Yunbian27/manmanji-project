<!--
  ArticleHeader.vue — 文章头部信息区
  显示：面包屑导航 + 文章标题 + 元信息行 + 标签列表

  Props 全是展示数据，无事件（纯展示组件）
-->
<template>
  <header class="article-header">
    <!-- 面包屑导航：分类 / 文章标题，13px/500 -->
    <nav class="breadcrumb">
      <span>{{ category || '未分类' }}</span>
      <span class="separator">/</span>
      <span>{{ title }}</span>
    </nav>

    <!-- 文章标题：32px/700，letter-spacing -1px -->
    <h1 class="article-title">{{ title }}</h1>

    <!-- 元信息行：头像 + 作者 + 日期 + 阅读量 + 阅读时长 -->
    <div class="article-meta">
      <div class="author-info">
        <div class="author-avatar">
          <img v-if="author.avatarUrl" :src="author.avatarUrl" :alt="author.nickname" />
          <span v-else>{{ author.nickname?.charAt(0) || '匿' }}</span>
        </div>
        <span class="author-name">{{ author.nickname }}</span>
      </div>
      <span class="meta-sep">·</span>
      <time class="meta-date">{{ formattedDate }}</time>
      <span class="meta-sep">·</span>
      <span class="meta-views">{{ formatCount(viewCount) }} 阅读</span>
      <span v-if="readTime" class="meta-sep">·</span>
      <span v-if="readTime" class="meta-readtime">{{ readTime }} 分钟</span>
    </div>

    <!-- 标签列表：第一个用黄色标签（突出），其余用默认标签 -->
    <div v-if="tags.length > 0" class="article-tags">
      <AppTag
        v-for="(tag, index) in tags"
        :key="tag.id"
        :variant="index === 0 ? 'yellow' : 'default'"
      >
        {{ tag.name }}
      </AppTag>
    </div>
  </header>
</template>

<script setup lang="ts">
import type { AuthorInfo, TagInfo } from '~/types'

const props = defineProps<{
  title: string
  author: AuthorInfo
  category?: string | null
  tags: TagInfo[]
  publishedAt?: string | null
  viewCount: number
  readTime?: number         // 预估阅读时长（分钟）
}>()

// computed: 根据 publishedAt 计算格式化日期
// 依赖变化时自动重新计算（有缓存）
const formattedDate = computed(() => {
  if (!props.publishedAt) return ''
  const d = new Date(props.publishedAt)
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
})

/** 数字格式化：2300 → 2.3k，1000 → 1.0k */
function formatCount(n: number): string {
  if (n >= 10000) return (n / 1000).toFixed(1) + 'k'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}
</script>

<style scoped>
.article-header { margin-bottom: var(--space-xl); }

/* 面包屑：13px/500，灰色 */
.breadcrumb {
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  color: var(--muted);
  margin-bottom: var(--space-md);
}
.separator { margin: 0 var(--space-xxs); }

/* 标题：32px/700，-1px 字间距 */
.article-title {
  font-size: var(--text-display-sm);
  font-weight: var(--weight-bold);
  line-height: var(--leading-tight);
  letter-spacing: var(--tracking-tight);
  color: var(--ink);
  margin-bottom: var(--space-md);
}

/* 元信息行：flex 横向排列，flex-wrap 自动换行 */
.article-meta {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  flex-wrap: wrap;
  margin-bottom: var(--space-md);
  color: var(--muted);
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
}

.author-info { display: flex; align-items: center; gap: var(--space-xs); }

/* 头像：32×32 圆形 */
.author-avatar {
  width: 32px; height: 32px;
  border-radius: var(--radius-full);
  background: var(--surface-elevated);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  font-size: var(--text-body-sm);
  color: var(--ink);
}
.author-avatar img { width: 100%; height: 100%; object-fit: cover; }
.author-name { color: var(--body-strong); }
.meta-sep { color: var(--hairline-strong); }

/* 标签容器：flex wrap，gap 8px */
.article-tags { display: flex; flex-wrap: wrap; gap: var(--space-xs); }
</style>
