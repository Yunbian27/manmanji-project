<template>
  <section class="comment-section" id="comments">
    <h3 class="comment-title">评论 ({{ comments.length }})</h3>

    <CommentForm @submit="addComment" />

    <!-- Loading -->
    <div v-if="loading" class="comment-loading">
      <div v-for="i in 3" :key="i" class="skeleton-comment">
        <div class="skeleton-avatar" />
        <div class="skeleton-lines">
          <div class="skeleton-line w-40" />
          <div class="skeleton-line w-90" />
        </div>
      </div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="comment-error">
      <p>{{ error }}</p>
    </div>

    <!-- Empty -->
    <div v-else-if="comments.length === 0" class="comment-empty">
      <p>暂无评论，来抢沙发吧</p>
    </div>

    <!-- List -->
    <CommentCard
      v-for="comment in comments"
      :key="comment.id"
      :comment="comment"
      @reply="onReply"
    />
  </section>
</template>

<script setup lang="ts">
import type { CommentVO } from '~/types'

defineProps<{
  comments: CommentVO[]
  loading?: boolean
  error?: string | null
}>()

const emit = defineEmits<{
  addComment: [content: string]
  reply: [commentId: number]
}>()

function addComment(content: string) {
  emit('addComment', content)
}

function onReply(id: number) {
  emit('reply', id)
}
</script>

<style scoped>
.comment-section {
  margin-top: var(--space-xxl);
}

.comment-title {
  font-size: var(--text-title-md);
  font-weight: var(--weight-semibold);
  color: var(--ink);
  margin-bottom: var(--space-lg);
}

/* Loading skeleton */
.comment-loading { display: flex; flex-direction: column; gap: var(--space-sm); }
.skeleton-comment {
  display: flex;
  gap: var(--space-sm);
  padding: 20px;
  border-radius: var(--radius-lg);
  background: var(--surface-card);
  border: 1px solid var(--hairline);
}
.skeleton-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  background: var(--surface-elevated);
  flex-shrink: 0;
}
.skeleton-lines { flex: 1; display: flex; flex-direction: column; gap: var(--space-xs); }
.skeleton-line { height: 12px; background: var(--surface-elevated); border-radius: 4px; }
.skeleton-line.w-40 { width: 40%; }
.skeleton-line.w-90 { width: 90%; }

/* States */
.comment-error { text-align: center; padding: var(--space-xl); color: var(--error); }
.comment-empty {
  text-align: center;
  padding: var(--space-xxl);
  color: var(--muted);
  font-size: var(--text-body-sm);
}
</style>
