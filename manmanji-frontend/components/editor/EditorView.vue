<template>
  <div class="editor-view">
    <EditorTopNav />
    <div class="editor-body">
      <EditorNav @close="handleClose" @insert-markdown="onInsertMarkdown" />

      <div :class="['editor-main', `editor-main--${viewMode}`]">
      <EditorTextarea
        v-if="viewMode !== 'preview'"
        ref="textareaRef"
        @scroll="onTextareaScroll"
      />
      <div v-if="viewMode === 'split'" class="editor-divider" />
      <EditorPreview
        v-if="viewMode !== 'edit'"
        ref="previewRef"
        @scroll="onPreviewScroll"
        @toc-updated="() => {}"
      />
    </div>
    </div>

    <div v-if="publishError" class="editor-toast error">
      {{ publishError }}
      <button @click="publishError = null">
        <IconX :size="14" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{ articleId: number }>()
const emit = defineEmits<{ close: [] }>()

const editor = createEditorState(props.articleId)
provideEditor(editor)

const { content, title } = editor

onBeforeRouteLeave((_to, _from, next) => {
  if (content.value || title.value) {
    if (!confirm('有未保存的内容，确定离开吗？')) {
      next(false)
      return
    }
  }
  next()
})

const {
  viewMode, publishError,
  startAutoSave, stopAutoSave, loadDraft,
} = editor

interface TextareaExposed {
  syncScroll: (ratio: number) => void
  insertAtCursor: (before: string, after: string, placeholder: string) => void
}

const textareaRef = ref<TextareaExposed | null>(null)
const previewRef = ref<{ syncScroll: (ratio: number) => void } | null>(null)

let isTextareaDriven = false
let isPreviewDriven = false

function onTextareaScroll(ratio: number) {
  if (isPreviewDriven) return
  isTextareaDriven = true
  previewRef.value?.syncScroll(ratio)
  requestAnimationFrame(() => { isTextareaDriven = false })
}

function onPreviewScroll(ratio: number) {
  if (isTextareaDriven) return
  isPreviewDriven = true
  textareaRef.value?.syncScroll(ratio)
  requestAnimationFrame(() => { isPreviewDriven = false })
}

function onInsertMarkdown(before: string, after: string, placeholder: string) {
  textareaRef.value?.insertAtCursor(before, after, placeholder)
}

function handleClose() {
  stopAutoSave()
  emit('close')
}

onMounted(() => {
  loadDraft()
  startAutoSave()
})

onUnmounted(() => {
  stopAutoSave()
})
</script>

<style scoped>
.editor-view {
  height: 100%;
  background: var(--canvas);
  display: flex;
  flex-direction: column;
}

.editor-body {
  flex: 1;
  display: flex;
  flex-direction: row;
  overflow: hidden;
}

.editor-main {
  flex: 1;
  display: grid;
  overflow: hidden;
}

.editor-main--edit {
  grid-template-columns: 1fr;
}

.editor-main--split {
  grid-template-columns: 1fr 4px 1fr;
}

.editor-main--preview {
  grid-template-columns: min(var(--content-max), 100%) 1fr;
}

.editor-divider {
  width: 4px;
  background: var(--hairline);
  cursor: col-resize;
  transition: background-color 0.15s ease;
  flex-shrink: 0;
}
.editor-divider:hover {
  background: var(--primary);
}

.editor-toast {
  position: fixed;
  bottom: var(--space-lg);
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-lg);
  border-radius: var(--radius-md);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  z-index: 200;
  animation: toast-in 0.2s ease;
}
.editor-toast.error {
  background: #c0392b;
  color: #fff;
}
.editor-toast button {
  display: flex;
  align-items: center;
  background: transparent;
  border: none;
  color: inherit;
  cursor: pointer;
  opacity: 0.8;
}
.editor-toast button:hover { opacity: 1; }

@keyframes toast-in {
  from { opacity: 0; transform: translateX(-50%) translateY(8px); }
  to { opacity: 1; transform: translateX(-50%) translateY(0); }
}
</style>
