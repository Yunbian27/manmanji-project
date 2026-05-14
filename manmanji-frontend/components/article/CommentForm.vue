<template>
  <div class="comment-form">
    <textarea
      v-model="text"
      class="comment-input"
      placeholder="写下你的评论..."
      rows="3"
      :minlength="1"
    />
    <div class="comment-form-footer">
      <span class="char-count" />
      <AppButton variant="primary" :disabled="!text.trim()" @click="submit">
        发表评论
      </AppButton>
    </div>
  </div>
</template>

<script setup lang="ts">
const emit = defineEmits<{ submit: [content: string] }>()
const text = ref('')

function submit() {
  if (!text.value.trim()) return
  emit('submit', text.value.trim())
  text.value = ''
}
</script>

<style scoped>
.comment-form { margin-bottom: var(--space-lg); }

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
  resize: vertical;
  transition: var(--transition-hover);
}

.comment-input:focus { outline: none; border-color: var(--primary); }
.comment-input::placeholder { color: var(--muted-soft); }

.comment-form-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: var(--space-xs);
}
</style>
