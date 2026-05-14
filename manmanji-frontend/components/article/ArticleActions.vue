<template>
  <div class="article-actions">
    <button
      class="interaction-btn"
      :class="{ active: liked }"
      @click="$emit('like')"
    >
      <IconHeart :size="16" />
      <span>{{ likeCount }}</span>
    </button>
    <button class="interaction-btn" @click="$emit('commentClick')">
      <IconMessageCircle :size="16" />
      <span>{{ commentCount }}</span>
    </button>
    <button
      class="interaction-btn"
      :class="{ active: bookmarked }"
      @click="$emit('bookmark')"
    >
      <IconBookmark :size="16" />
      <span>{{ bookmarkCount }}</span>
    </button>
    <button class="interaction-btn" @click="$emit('share')">
      <IconShare :size="16" />
      <span>分享</span>
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  likeCount: number
  commentCount: number
  bookmarkCount: number
  liked: boolean
  bookmarked: boolean
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
  gap: var(--space-xs);
  padding: var(--space-xl) 0;
  margin-top: var(--space-xxl);
  border-top: 1px solid var(--hairline);
  border-bottom: 1px solid var(--hairline);
}

.interaction-btn {
  height: 36px;
  padding: 0 14px;
  border-radius: var(--radius-md);
  border: none;
  background: var(--surface-card);
  color: var(--body);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: var(--space-xxs);
  transition: var(--transition-hover);
}

.interaction-btn:hover { background: var(--surface-elevated); }

.interaction-btn.active {
  background: #2a2a1a;
  color: var(--primary);
}
</style>
