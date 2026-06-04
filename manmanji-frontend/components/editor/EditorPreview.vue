<template>
  <div
    ref="previewRef"
    :class="['editor-preview', {
      'editor-preview--centered': centered
    }]"
    @scroll="onScroll"
  >
    <div class="article-content" v-html="renderedHtml" />
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
  const el = previewRef.value
  if (!el) return
  const headings = el.querySelectorAll('h1, h2, h3, h4, h5, h6')
  headings[index]?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

defineExpose({ syncScroll, scrollToHeading })
</script>

<style scoped>
.editor-preview {
  height: 100%;
  overflow-y: auto;
  padding: var(--spacing-xl);
  background: transparent;
}

.article-content {
  overflow-wrap: break-word;
  word-break: break-word;
}

.editor-preview--centered {
  display: flex;
  justify-content: center;
}

.editor-preview--centered .article-content {
  width: 100%;
  max-width: 720px;
}
</style>
