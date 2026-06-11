<template>
  <div class="editor-textarea-wrap">
    <textarea
      ref="textareaEl"
      v-model="content"
      class="editor-textarea"
      placeholder="开始写文章..."
      spellcheck="false"
      @scroll="onScroll"
      @mouseup="onSelectionChange"
      @keyup="onSelectionChange"
    />
  </div>
</template>

<script setup lang="ts">
const editor = injectEditor()
const { content } = editor

const textareaEl = ref<HTMLTextAreaElement | null>(null)

const emit = defineEmits<{
  scroll: [ratio: number]
  selectionChange: [state: { start: number; end: number; text: string; isCollapsed: boolean }]
}>()

const selectionState = computed(() => {
  const el = textareaEl.value
  if (!el) return { start: 0, end: 0, text: '', isCollapsed: true }
  return {
    start: el.selectionStart,
    end: el.selectionEnd,
    text: content.value.slice(el.selectionStart, el.selectionEnd),
    isCollapsed: el.selectionStart === el.selectionEnd,
  }
})

function onSelectionChange() { emit('selectionChange', selectionState.value) }

function getCursorLineCol(): { row: number; col: number } {
  const el = textareaEl.value
  if (!el) return { row: 1, col: 1 }
  const pos = el.selectionStart
  const before = content.value.substring(0, pos)
  const row = before.split('\n').length
  const lastNewline = before.lastIndexOf('\n')
  const col = lastNewline === -1 ? pos + 1 : pos - lastNewline
  return { row, col }
}

function onScroll() {
  const el = textareaEl.value
  if (!el) return
  const maxScroll = el.scrollHeight - el.clientHeight
  if (maxScroll <= 0) return
  emit('scroll', el.scrollTop / maxScroll)
}

function syncScroll(ratio: number) {
  const el = textareaEl.value
  if (!el) return
  const maxScroll = el.scrollHeight - el.clientHeight
  if (maxScroll <= 0) return
  el.scrollTop = ratio * maxScroll
}

function insertAtCursor(before: string, after = '', placeholder = '') {
  const el = textareaEl.value
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

defineExpose({ syncScroll, insertAtCursor, textareaEl, selectionState, getCursorLineCol })
</script>

<style scoped>
.editor-textarea-wrap {
  height: 100%;
}

.editor-textarea {
  width: 100%;
  height: 100%;
  resize: none;
  border: none;
  outline: none;
  color: var(--ink);
  font-family: var(--font-mono);
  font-size: var(--body-md);
  line-height: 1.75;
  padding: var(--spacing-xl);
  tab-size: 2;
}

.editor-textarea::placeholder { color: var(--muted); }
</style>
