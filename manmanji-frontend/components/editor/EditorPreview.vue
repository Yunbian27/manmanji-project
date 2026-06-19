<template>
  <div
    ref="previewRef"
    :class="['editor-preview', {
      'editor-preview--centered': centered
    }]"
    @scroll="onScroll"
  >
    <div class="markdown-body" v-html="renderedHtml" />
  </div>
</template>

<script setup lang="ts">
import type { TocItem } from '~/types'

const props = withDefaults(defineProps<{ centered?: boolean }>(), { centered: false })

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

function scrollToHeading(index: number) {
  const container = previewRef.value
  if (!container) return
  const target = container.querySelector<HTMLElement>(`#heading-${index}`)
  if (!target) return
  container.scrollTop = target.getBoundingClientRect().top - container.getBoundingClientRect().top + container.scrollTop
}

defineExpose({ syncScroll, scrollToHeading, containerEl: previewRef })
</script>

<style scoped>
.editor-preview {
  height: 100%;
  overflow-y: auto;
  padding: var(--spacing-xl);
  background: transparent;
}

.editor-preview--centered {
  display: flex;
  justify-content: center;
}

.editor-preview--centered .markdown-body {
  width: 100%;
  max-width: max(calc(50% - 24.5px), calc(50vw - 92.5px));
}
</style>
