<template>
  <textarea
    ref="textareaRef"
    v-model="content"
    class="editor-textarea"
    placeholder="开始写文章..."
    spellcheck="false"
    @scroll="onScroll"
  />
</template>

<script setup lang="ts">
const editor = injectEditor()
const { content } = editor

const textareaRef = ref<HTMLTextAreaElement | null>(null)

const emit = defineEmits<{ scroll: [ratio: number] }>()

function onScroll() {
  const el = textareaRef.value
  if (!el) return
  const maxScroll = el.scrollHeight - el.clientHeight
  if (maxScroll <= 0) return
  emit('scroll', el.scrollTop / maxScroll)
}

function syncScroll(ratio: number) {
  const el = textareaRef.value
  if (!el) return
  const maxScroll = el.scrollHeight - el.clientHeight
  if (maxScroll <= 0) return
  el.scrollTop = ratio * maxScroll
}

defineExpose({ syncScroll })
</script>

<style scoped>
.editor-textarea {
  width: 100%;
  height: 100%;
  resize: none;
  border: none;
  outline: none;
  background: var(--canvas);
  color: var(--body);
  font-family: var(--font-mono);
  font-size: var(--text-code);
  line-height: var(--leading-relaxed);
  padding: var(--space-xl);
  tab-size: 2;
}
.editor-textarea::placeholder { color: var(--muted-soft); }
</style>
