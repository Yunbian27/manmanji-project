<!--
  CommentForm.vue — 评论输入表单
  textarea + 提交按钮，输入为空时按钮禁用
-->
<template>
  <div class="comment-form">
    <!-- v-model: 双向绑定，输入内容自动同步到 text 变量 -->
    <textarea
      v-model="text"
      class="comment-input"
      placeholder="写下你的评论..."
      rows="3"
    />

    <div class="comment-form-footer">
      <AppButton variant="primary" :disabled="!text.trim()" @click="submit">
        发表评论
      </AppButton>
    </div>
  </div>
</template>

<script setup lang="ts">
const emit = defineEmits<{ submit: [content: string] }>()

// ref: 表单输入值的响应式状态
const text = ref('')

function submit() {
  if (!text.value.trim()) return          // 空内容不提交
  emit('submit', text.value.trim())       // 把内容传给父组件
  text.value = ''                          // 清空输入框
}
</script>

<style scoped>
.comment-form { margin-bottom: var(--space-lg); }

/* textarea 输入框：最小高度 80px，圆角 8px */
.comment-input {
  width: 100%;
  min-height: 80px;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  border: 1px solid var(--hairline);
  background: var(--surface-card);
  color: var(--body);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  line-height: var(--leading-relaxed);
  resize: vertical;                       /* 只允许竖向调整大小 */
  transition: var(--transition-hover);
}
.comment-input:focus { outline: none; border-color: var(--primary); }
.comment-input::placeholder { color: var(--muted-soft); }

.comment-form-footer {
  display: flex;
  justify-content: flex-end;              /* 按钮靠右 */
  margin-top: var(--space-xs);
}
</style>
