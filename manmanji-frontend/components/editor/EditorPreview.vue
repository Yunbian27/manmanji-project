<template>
  <div ref="previewRef" class="editor-preview" @scroll="onScroll">
    <div class="article-content" v-html="renderedHtml" />
  </div>
</template>

<script setup lang="ts">
import type { TocItem } from '~/types'

const editor = injectEditor()
const { content } = editor

const emit = defineEmits<{
  scroll: [ratio: number]
  tocUpdated: [items: TocItem[]]
}>()

const { renderedHtml, tocItems } = useMarkdownRenderer(content)

watchEffect(() => {
  emit('tocUpdated', tocItems.value)
})

const previewRef = ref<HTMLElement | null>(null)

function onScroll() {
  const el = previewRef.value
  if (!el) return
  const maxScroll = el.scrollHeight - el.clientHeight
  if (maxScroll <= 0) return
  emit('scroll', el.scrollTop / maxScroll)
}

function syncScroll(ratio: number) {
  const el = previewRef.value
  if (!el) return
  const maxScroll = el.scrollHeight - el.clientHeight
  if (maxScroll <= 0) return
  el.scrollTop = ratio * maxScroll
}

defineExpose({ syncScroll })
</script>

<style scoped>
.editor-preview {
  height: 100%;
  overflow-y: auto;
  padding: var(--spacing-xl);
  background: var(--canvas);
}
</style>
