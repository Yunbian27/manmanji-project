<template>
  <article class="article-body">
    <div ref="bodyRef" class="article-content" v-html="renderedHtml" />
  </article>
</template>

<script setup lang="ts">
import type { TocItem } from '~/types'

const props = defineProps<{ content: string }>()

const emit = defineEmits<{ tocUpdated: [items: TocItem[]] }>()

const { renderedHtml, tocItems } = useMarkdownRenderer(toRef(() => props.content))

watchEffect(() => {
  emit('tocUpdated', tocItems.value)
})
</script>

<style scoped>
.article-content {
  font-size: var(--text-body);
  line-height: var(--leading-relaxed);
  color: var(--body);
}
</style>

<style>
.article-content h2 {
  font-size: var(--text-title-lg);
  font-weight: var(--weight-bold);
  line-height: 1.3;
  letter-spacing: var(--tracking-semi-tight);
  color: var(--ink);
  margin-top: var(--space-xxl);
  border-top: 1px solid var(--hairline);
  padding-top: var(--space-lg);
  margin-bottom: var(--space-md);
}
.article-content h3 {
  font-size: var(--text-title-md);
  font-weight: var(--weight-semibold);
  color: var(--ink);
  margin-top: var(--space-xl);
  margin-bottom: var(--space-sm);
}
.article-content h4 {
  font-size: var(--text-title-sm);
  font-weight: var(--weight-semibold);
  color: var(--ink);
  margin-top: var(--space-lg);
  margin-bottom: var(--space-sm);
}
.article-content p { margin-bottom: var(--space-md); }
.article-content strong { color: var(--body-strong); font-weight: var(--weight-semibold); }
.article-content a { color: var(--primary); text-decoration: underline; }
.article-content a:hover { color: var(--primary-active); }
[data-theme="light"] .article-content a { color: var(--ink); text-decoration-color: var(--primary); }
.article-content blockquote {
  border-left: 3px solid var(--primary);
  padding: var(--space-sm) var(--space-md);
  margin: var(--space-lg) 0;
  background: var(--surface-soft);
  color: var(--muted);
  font-style: italic;
  font-size: 15px;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
}
.article-content ul, .article-content ol {
  padding-left: var(--space-lg);
  margin-bottom: var(--space-md);
}
.article-content ul { list-style: disc; }
.article-content ol { list-style: decimal; }
.article-content li { margin-bottom: var(--space-xxs); }
.article-content img {
  max-width: 100%;
  border-radius: var(--radius-md);
  margin: var(--space-lg) 0;
}
.article-content table {
  width: 100%;
  border-collapse: collapse;
  margin: var(--space-lg) 0;
  font-size: var(--text-body-sm);
}
.article-content th {
  text-align: left;
  padding: 10px 14px;
  background: var(--surface-card);
  color: var(--body-strong);
  font-size: var(--text-caption);
  font-weight: var(--weight-semibold);
  border-bottom: 1px solid var(--hairline-strong);
}
.article-content td {
  padding: 10px 14px;
  border-bottom: 1px solid var(--hairline);
}
.article-content tr:hover td { background: var(--surface-soft); }
.article-content :not(pre) > code {
  font-family: var(--font-mono);
  font-size: var(--text-code-inline);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  background: var(--surface-card);
  color: var(--primary);
  border: 1px solid var(--hairline);
}
[data-theme="light"] .article-content :not(pre) > code {
  background: var(--surface-elevated);
  color: #92400e;
}
.article-content .code-block {
  border-radius: var(--radius-lg);
  border: 1px solid var(--hairline);
  background: var(--surface-card);
  overflow: hidden;
  margin: var(--space-lg) 0;
}
.article-content .code-block-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-sm) var(--space-md);
  border-bottom: 1px solid var(--hairline);
}
.article-content .code-lang {
  font-size: var(--text-uppercase);
  font-weight: var(--weight-semibold);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;
  color: var(--muted);
}
.article-content .code-copy {
  font-family: var(--font-sans);
  font-size: var(--text-uppercase);
  color: var(--muted);
  background: transparent;
  border: none;
  cursor: pointer;
  padding: var(--space-xxs) var(--space-xs);
  border-radius: var(--radius-sm);
}
.article-content .code-copy:hover { color: var(--primary); }
.article-content .code-block pre {
  padding: 20px;
  color: var(--body);
  font-size: var(--text-code);
  line-height: var(--leading-relaxed);
  tab-size: 2;
  overflow-x: auto;
  margin: 0;
}
.article-content .code-block code { font-family: var(--font-mono); }
.article-content hr {
  border: none;
  border-top: 1px solid var(--hairline);
  margin: var(--space-xxl) 0;
}
</style>
