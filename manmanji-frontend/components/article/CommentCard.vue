<!--
  CommentCard.vue — 单条评论展示卡片
  左侧头像 + 右侧内容（作者名、时间、正文、回复按钮）
  卡片样式：圆角 12px + 1px 边框 + 背景色
-->
<template>
  <div class="comment-card">
    <!-- 头像：32×32 圆形 -->
    <div class="comment-avatar">
      <img v-if="comment.author.avatarUrl && !avatarError" :src="comment.author.avatarUrl" :alt="comment.author.nickname" @error="avatarError = true" />
      <span v-else>{{ comment.author.nickname?.charAt(0) || '匿' }}</span>
    </div>

    <div class="comment-body">
      <!-- 顶行：作者名 + 时间 -->
      <div class="comment-header">
        <span class="comment-author">{{ comment.author.nickname }}</span>
        <time class="comment-time">{{ formattedTime }}</time>
      </div>

      <!-- 评论正文 -->
      <p class="comment-text">{{ comment.content }}</p>

      <!-- 底行：回复按钮 -->
      <div class="comment-footer">
        <button class="reply-btn" @click="$emit('reply', comment.id)">回复</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { CommentVO } from '~/types'

const props = defineProps<{ comment: CommentVO }>()

const avatarError = ref(false)
defineEmits<{ reply: [id: number] }>()

// computed: 根据 createdAt 格式化显示时间
const formattedTime = computed(() => {
  const d = new Date(props.comment.createdAt)
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
})
</script>

<style scoped>
.comment-card {
  display: flex;
  gap: var(--space-sm);
  padding: 20px;
  border-radius: var(--radius-xl);        /* 16px */
  background: var(--canvas);
  border: 1px solid var(--hairline);
  margin-bottom: var(--space-sm);
}
.comment-avatar {
  width: 32px; height: 32px;
  border-radius: var(--radius-full);
  background: var(--canvas-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
  font-size: var(--text-body-sm);
  color: var(--ink);
}
.comment-avatar img { width: 100%; height: 100%; object-fit: cover; }
.comment-body { flex: 1; min-width: 0; }  /* min-width:0 防止 flex 溢出 */

.comment-header {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  margin-bottom: var(--space-xxs);
}
.comment-author { font-size: var(--text-body-sm); font-weight: var(--weight-medium); color: var(--body-strong); }
.comment-time { font-size: var(--text-caption); font-weight: var(--weight-medium); color: var(--muted-soft); }
.comment-text {
  font-size: var(--text-body-sm);
  line-height: var(--leading-relaxed);
  color: var(--ink);
  margin-bottom: var(--space-xs);
}

.comment-footer { display: flex; gap: var(--space-md); }
.reply-btn {
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  color: var(--muted);
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0;
  transition: var(--transition-hover);
}
.reply-btn:hover { color: var(--ink); }
</style>
