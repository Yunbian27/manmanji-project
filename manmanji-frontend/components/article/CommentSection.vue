<!--
  CommentSection.vue — 评论区组件
  组合 CommentForm + CommentCard，管理评论列表

  四种状态（统一模式）：
  - loading: 骨架屏占位（3 条评论占位）
  - error: 错误信息
  - empty: "暂无评论" 引导（但表单仍可输入）
  - data: 评论卡片列表
-->
<template>
  <section class="comment-section" id="comments">
    <!-- 状态1：加载中 -->
    <div v-if="loading" class="comment-loading">
      <div v-for="i in 3" :key="i" class="skeleton-comment">
        <div class="skeleton-avatar" />
        <div class="skeleton-lines">
          <div class="skeleton-line w-40" />
          <div class="skeleton-line w-90" />
        </div>
      </div>
    </div>

    <!-- 状态2：加载出错 -->
    <div v-else-if="error" class="comment-error">
      <p>{{ error }}</p>
    </div>

    <!-- 状态3&4：正常 — 标题和表单始终显示 -->
    <template v-else>
      <h3 class="comment-title">评论 ({{ comments.length }})</h3>
      <CommentForm @submit="(content: string) => $emit('addComment', content)" />
      <!-- 无评论时显示引导文案 -->
      <div v-if="comments.length === 0" class="comment-empty">
        <p>暂无评论，来抢沙发吧</p>
      </div>
      <!-- 评论列表 -->
      <CommentCard
        v-for="comment in comments"
        :key="comment.id"
        :comment="comment"
        @reply="(id: number) => $emit('reply', id)"
      />
    </template>
  </section>
</template>

<script setup lang="ts">
import type { CommentVO } from '~/types'

defineProps<{
  comments: CommentVO[]
  loading?: boolean
  error?: string | null
}>()

defineEmits<{
  addComment: [content: string]
  reply: [commentId: number]
}>()
</script>

<style scoped>
.comment-section { margin-top: var(--space-xxl); }

.comment-title {
  font-size: var(--text-body-lg);         /* 18px */
  font-weight: var(--weight-medium);
  color: var(--ink);
  margin-bottom: var(--space-lg);
}

/* 骨架屏 */
.comment-loading { display: flex; flex-direction: column; gap: var(--space-sm); }
.skeleton-comment {
  display: flex;
  gap: var(--space-sm);
  padding: 20px;
  border-radius: var(--radius-xl);
  background: var(--canvas);
  border: 1px solid var(--hairline);
}
.skeleton-avatar {
  width: 32px; height: 32px;
  border-radius: var(--radius-full);
  background: var(--canvas-soft);
  flex-shrink: 0;
}
.skeleton-lines { flex: 1; display: flex; flex-direction: column; gap: var(--space-xs); }
.skeleton-line { height: 12px; background: var(--canvas-soft); border-radius: 4px; }
.w-40 { width: 40%; }
.w-90 { width: 90%; }

.comment-error { text-align: center; padding: var(--space-xl); color: #c0392b; }
.comment-empty {
  text-align: center;
  padding: var(--space-lg);
  color: var(--muted);
  font-size: var(--text-body-sm);
}
</style>
