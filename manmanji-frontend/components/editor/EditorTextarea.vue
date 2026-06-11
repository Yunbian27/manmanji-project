<template>
  <div ref="cmContainer" class="editor-textarea-wrap" />
</template>

<script setup lang="ts">
import { EditorView, keymap } from '@codemirror/view'
import { EditorState } from '@codemirror/state'
import { markdown } from '@codemirror/lang-markdown'
import { defaultKeymap, history, historyKeymap } from '@codemirror/commands'

const editor = injectEditor()
const { content } = editor

const cmContainer = ref<HTMLElement | null>(null)
let view: EditorView | null = null

const emit = defineEmits<{
  scroll: [ratio: number]
  selectionChange: [state: { start: number; end: number; text: string; isCollapsed: boolean }]
}>()

// ── Selection state (exposed, used by EditorView.vue) ──────
function getSelection() {
  if (!view) return { start: 0, end: 0, text: '', isCollapsed: true }
  const { from, to } = view.state.selection.main
  return {
    start: from,
    end: to,
    text: view.state.sliceDoc(from, to),
    isCollapsed: from === to,
  }
}

const selectionState = computed(() => getSelection())

function onSelectionChange() {
  emit('selectionChange', getSelection())
}

// ── Scroll ─────────────────────────────────────────────────
function onScroll() {
  if (!view) return
  const el = view.scrollDOM
  const maxScroll = el.scrollHeight - el.clientHeight
  if (maxScroll <= 0) return
  emit('scroll', el.scrollTop / maxScroll)
}

function syncScroll(ratio: number) {
  if (!view) return
  const el = view.scrollDOM
  const maxScroll = el.scrollHeight - el.clientHeight
  if (maxScroll <= 0) return
  el.scrollTop = ratio * maxScroll
}

// ── Cursor ─────────────────────────────────────────────────
function getCursorLineCol(): { row: number; col: number } {
  if (!view) return { row: 1, col: 1 }
  const pos = view.state.selection.main.head
  const line = view.state.doc.lineAt(pos)
  return { row: line.number, col: pos - line.from + 1 }
}

// ── Insert at cursor ───────────────────────────────────────
function insertAtCursor(before: string, after = '', placeholder = '') {
  if (!view) return
  const { from, to } = view.state.selection.main
  const selected = view.state.sliceDoc(from, to)
  const insertText = selected
    ? before + selected + after
    : before + placeholder + after

  let selection: { anchor: number; head: number }
  if (selected) {
    selection = { anchor: from, head: from + insertText.length }
  } else if (placeholder) {
    selection = {
      anchor: from + before.length,
      head: from + before.length + placeholder.length,
    }
  } else {
    selection = { anchor: from + before.length, head: from + before.length }
  }

  view.dispatch({
    changes: { from, to, insert: insertText },
    selection,
  })
  view.focus()
}

// ── CM6 theme (matches existing design tokens) ─────────────
const cmTheme = EditorView.theme({
  '&': {
    height: '100%',
    background: 'transparent',
  },
  '.cm-scroller': {
    overflow: 'auto',
    fontFamily: 'var(--font-mono)',
  },
  '.cm-content': {
    fontFamily: 'var(--font-mono)',
    fontSize: 'var(--body-md)',
    lineHeight: '1.75',
    padding: 'var(--spacing-xl)',
    caretColor: 'var(--ink)',
    overflowWrap: 'break-word',
    wordBreak: 'break-word',
  },
  '.cm-cursor': {
    borderLeftColor: 'var(--ink)',
  },
  '.cm-line': {
    padding: '0',
  },
  '.cm-activeLine': {
    background: 'transparent !important',
  },
  '.cm-selectionBackground, ::selection': {
    background: 'rgba(37, 99, 235, 0.2) !important',
  },
  '.cm-gutters': {
    display: 'none',
  },
  '&.cm-focused': {
    outline: 'none',
  },
  '&.cm-focused .cm-selectionBackground, .cm-selectionBackground': {
    background: 'rgba(37, 99, 235, 0.2) !important',
  },
})

// ── Lifecycle ──────────────────────────────────────────────
onMounted(() => {
  if (!cmContainer.value) return

  const updateListener = EditorView.updateListener.of((update) => {
    if (update.docChanged) {
      content.value = update.state.doc.toString()
    }
    if (update.selectionSet) {
      onSelectionChange()
    }
  })

  view = new EditorView({
    state: EditorState.create({
      doc: content.value,
      extensions: [
        markdown(),
        history(),
        keymap.of([...defaultKeymap, ...historyKeymap]),
        EditorView.lineWrapping,
        updateListener,
        cmTheme,
        EditorView.domEventHandlers({
          scroll: () => onScroll(),
        }),
      ],
    }),
    parent: cmContainer.value,
  })
})

onUnmounted(() => {
  view?.destroy()
  view = null
})

// ── External content change → CM6 ──────────────────────────
watch(
  () => content.value,
  (newVal) => {
    if (!view) return
    const currentDoc = view.state.doc.toString()
    if (newVal !== currentDoc) {
      view.dispatch({
        changes: {
          from: 0,
          to: currentDoc.length,
          insert: newVal,
        },
      })
    }
  },
)

function scrollToPos(pos: number) {
  if (!view) return
  view.dispatch({
    effects: EditorView.scrollIntoView(pos, { y: 'start' }),
  })
}

defineExpose({
  syncScroll,
  insertAtCursor,
  getCursorLineCol,
  scrollToPos,
  selectionState,
  scrollDOM: computed(() => view?.scrollDOM ?? null),
})
</script>

<style scoped>
.editor-textarea-wrap {
  height: 100%;
  overflow: hidden;
}
</style>
