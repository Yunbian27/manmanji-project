<!--
  ArticleActions.vue — 文章互动按钮栏 (BLUEPRINT 7.3 bottom)
  space-between: 左侧互动按钮 / 右侧标签 chip
-->
<template>
  <div class="article-actions">
    <div class="actions-left">
      <button class="interaction-btn" :class="{ active: liked }" @click="$emit('like')">
        <IconHeart :size="16" />
        <span>{{ likeCount }}</span>
      </button>
      <button class="interaction-btn" @click="$emit('commentClick')">
        <IconMessageCircle :size="16" />
        <span>{{ commentCount }}</span>
      </button>
      <button class="interaction-btn" :class="{ active: bookmarked }" @click="$emit('bookmark')">
        <IconBookmark :size="16" />
        <span>{{ bookmarkCount }}</span>
      </button>
      <button class="interaction-btn" @click="$emit('share')">
        <IconShare :size="16" />
        <span>分享</span>
      </button>
    </div>
    <div v-if="tags && tags.length > 0" class="actions-right">
      <AppTag v-for="tag in tags" :key="tag.id">{{ tag.name }}</AppTag>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  likeCount: number
  commentCount: number
  bookmarkCount: number
  liked: boolean
  bookmarked: boolean
  tags?: { id: number; name: string }[]
}>()

defineEmits<{
  like: []
  commentClick: []
  bookmark: []
  share: []
}>()
</script>

<style scoped>
.article-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-xs);
  padding: var(--space-xl) 0;             /* 上下 32px */
  margin-top: var(--space-xxl);           /* 上距 48px */
  border-top: 1px solid var(--hairline);
  border-bottom: 1px solid var(--hairline);
}

/* 互动按钮：16px/ink，胶囊形 (BLUEPRINT subtle button) */
.interaction-btn {
  height: 36px;
  padding: 0 14px;
  border-radius: var(--radius-pill);
  border: none;
  background: var(--canvas-soft);
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: var(--text-body-md);       /* 16px */
  font-weight: var(--weight-medium);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: var(--space-xxs);
  transition: var(--transition-hover);
}
.interaction-btn:hover { background: var(--surface-elevated); }

/* 激活态 */
.interaction-btn.active {
  background: #e8e8e8;
}

.actions-left {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

.actions-right {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}
</style>
