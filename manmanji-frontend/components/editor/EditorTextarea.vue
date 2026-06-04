<template>
  <div class="editor-textarea-wrap">
    <div ref="lineNumbersRef" class="line-numbers">
      <span v-for="(num, i) in visualLineNumbers" :key="i">{{ num }}</span>
    </div>
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
import { useResizeObserver } from '@vueuse/core'

const editor = injectEditor()
const { content } = editor

const textareaEl = ref<HTMLTextAreaElement | null>(null)
const lineNumbersRef = ref<HTMLElement | null>(null)

function charWidth(ch: string): number {
  const code = ch.charCodeAt(0)
  if (code > 0x2000) return 2
  if (code > 0xff) return 1
  return code > 0x7f ? 2 : 1
}

function visualLength(str: string): number {
  let w = 0
  for (const ch of str) { w += charWidth(ch) }
  return w
}

const textareaWidth = ref(600)
useResizeObserver(textareaEl, (entries) => {
  const entry = entries[0]
  if (entry) { textareaWidth.value = entry.contentRect.width }
})

const charsPerLine = computed(() => {
  // textarea padding: left 12px + right 24px = 36px
  const availWidth = textareaWidth.value - 36
  return Math.max(10, Math.floor(availWidth / 8.4))
})

const visualLineNumbers = computed(() => {
  const cpl = charsPerLine.value
  const lines = content.value ? content.value.split('\n') : ['']
  const result: number[] = []
  let logicalNum = 1
  for (const line of lines) {
    const vLen = visualLength(line)
    const wraps = vLen === 0 ? 1 : Math.max(1, Math.ceil(vLen / cpl))
    for (let i = 0; i < wraps; i++) { result.push(logicalNum) }
    logicalNum++
  }
  return result
})

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
  if (lineNumbersRef.value) { lineNumbersRef.value.scrollTop = el.scrollTop }
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
  display: flex;
  flex-direction: row;
  height: 100%;
}

.line-numbers {
  width: 44px;
  flex-shrink: 0;
  overflow: hidden;
  padding: var(--spacing-xl) 8px var(--spacing-xl) 12px;
  font-family: var(--font-mono);
  font-size: var(--body-sm);
  color: var(--muted);
  line-height: var(--leading-body);
  text-align: right;
  user-select: none;
  pointer-events: none;
  white-space: pre;
}

.line-numbers span {
  display: block;
}

.editor-textarea {
  flex: 1;
  height: 100%;
  resize: none;
  border: none;
  outline: none;
  color: var(--ink);
  font-family: var(--font-mono);
  font-size: var(--body-sm);
  line-height: var(--leading-body);
  padding: var(--spacing-xl);
  padding-left: var(--spacing-sm);
  tab-size: 2;
}

.editor-textarea::placeholder { color: var(--muted); }
</style>
