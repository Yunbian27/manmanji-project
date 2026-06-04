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

function insertAtCursor(before: string, after = '', placeholder = '') {
  const el = textareaRef.value
  if (!el) return
  const start = el.selectionStart
  const end = el.selectionEnd
  const selected = content.value.slice(start, end)
  const insertText = selected ? before + selected + after : before + placeholder + after
  content.value = content.value.slice(0, start) + insertText + content.value.slice(end)
  nextTick(() => {
    el.focus()
    if (selected) {
      el.selectionStart = start
      el.selectionEnd = start + insertText.length
    } else if (placeholder) {
      const cursor = start + before.length
      el.selectionStart = cursor
      el.selectionEnd = cursor + placeholder.length
    } else {
      const cursor = start + before.length
      el.selectionStart = cursor
      el.selectionEnd = cursor
    }
  })
}

defineExpose({ syncScroll, insertAtCursor })
</script>

<style scoped>
.editor-textarea {
  width: 100%;
  height: 100%;
  resize: none;
  border: none;
  outline: none;
  background: var(--canvas);
  color: var(--ink);
  font-family: var(--font-mono);
  font-size: var(--body-sm);
  line-height: var(--leading-body);
  padding: var(--spacing-xl);
  tab-size: 2;
}
.editor-textarea::placeholder { color: var(--muted); }
</style>
