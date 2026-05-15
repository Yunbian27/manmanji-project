<!--
  ArticleActions.vue — 文章互动按钮栏
  四个操作：点赞 / 评论 / 收藏 / 分享
  上下有分割线，位于文章正文和评论区之间
  active 态（已点赞/已收藏）时背景变为浅黄色 + 黄色文字
-->
<template>
  <div class="article-actions">
    <!-- 点赞按钮 -->
    <button class="interaction-btn" :class="{ active: liked }" @click="$emit('like')">
      <IconHeart :size="16" />
      <span>{{ likeCount }}</span>
    </button>

    <!-- 评论按钮（点击滚动到评论区） -->
    <button class="interaction-btn" @click="$emit('commentClick')">
      <IconMessageCircle :size="16" />
      <span>{{ commentCount }}</span>
    </button>

    <!-- 收藏按钮 -->
    <button class="interaction-btn" :class="{ active: bookmarked }" @click="$emit('bookmark')">
      <IconBookmark :size="16" />
      <span>{{ bookmarkCount }}</span>
    </button>

    <!-- 分享按钮 -->
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
/* 互动栏：flex 布局，上下分割线 */
.article-actions {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  padding: var(--space-xl) 0;             /* 上下 32px */
  margin-top: var(--space-xxl);           /* 上距 48px */
  border-top: 1px solid var(--hairline);
  border-bottom: 1px solid var(--hairline);
}

/* 互动按钮：36px 高，padding 0 14px，圆角 8px */
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

/* 激活态：浅黄色背景 + 黄色文字 */
.interaction-btn.active {
  background: #2a2a1a;                    /* 深色：暗黄绿 */
  color: var(--primary);
}
</style>
