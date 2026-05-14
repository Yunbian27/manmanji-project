<template>
  <header class="article-header">
    <!-- Breadcrumb -->
    <nav class="breadcrumb">
      <span>{{ category || '未分类' }}</span>
      <span class="separator">/</span>
      <span>{{ title }}</span>
    </nav>

    <!-- Title -->
    <h1 class="article-title">{{ title }}</h1>

    <!-- Meta row -->
    <div class="article-meta">
      <div class="author-info">
        <div class="author-avatar">
          <img
            v-if="author.avatarUrl"
            :src="author.avatarUrl"
            :alt="author.nickname"
          />
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

    <!-- Tags -->
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
  readTime?: number
}>()

const formattedDate = computed(() => {
  if (!props.publishedAt) return ''
  const d = new Date(props.publishedAt)
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
})

function formatCount(n: number): string {
  if (n >= 10000) return (n / 1000).toFixed(1) + 'k'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}
</script>

<style scoped>
.article-header { margin-bottom: var(--space-xl); }

.breadcrumb {
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  line-height: var(--leading-normal);
  color: var(--muted);
  margin-bottom: var(--space-md);
}
.separator { margin: 0 var(--space-xxs); }

.article-title {
  font-size: var(--text-display-sm);
  font-weight: var(--weight-bold);
  line-height: var(--leading-tight);
  letter-spacing: var(--tracking-tight);
  color: var(--ink);
  margin-bottom: var(--space-md);
}

.article-meta {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  flex-wrap: wrap;
  margin-bottom: var(--space-md);
  color: var(--muted);
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  line-height: var(--leading-normal);
}

.author-info { display: flex; align-items: center; gap: var(--space-xs); }
.author-avatar {
  width: 32px;
  height: 32px;
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

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-xs);
}
</style>
