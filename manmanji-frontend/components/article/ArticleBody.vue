<template>
  <article class="article-body">
    <!-- Content rendered from markdown -->
    <div ref="bodyRef" class="article-content" v-html="renderedHtml" />
  </article>
</template>

<script setup lang="ts">
import MarkdownIt from 'markdown-it'
import type { TocItem } from '~/types'

const props = defineProps<{
  content: string
}>()

const emit = defineEmits<{
  tocUpdated: [items: TocItem[]]
}>()

const bodyRef = ref<HTMLElement | null>(null)

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  breaks: false,
})

// Override fence renderer to add code-block structure
const defaultFence = md.renderer.rules.fence!
md.renderer.rules.fence = (tokens, idx, options, env, self) => {
  const token = tokens[idx]
  const lang = token.info.trim() || 'text'
  const code = token.content
  const escaped = escapeHtml(code)
  return `<div class="code-block"><div class="code-block-header"><span class="code-lang">${lang}</span><button class="code-copy" onclick="navigator.clipboard.writeText(this.closest('.code-block').querySelector('code').textContent);var b=this;b.textContent='已复制';setTimeout(function(){b.textContent='复制代码'},2000)">复制代码</button></div><pre><code>${escaped}</code></pre></div>`
}

function escapeHtml(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

const renderedHtml = computed(() => {
  let html = md.render(props.content)

  // Add IDs to headings for TOC linking
  html = html.replace(/<(h[234])>/g, (_, tag: string) => {
    const level = parseInt(tag[1])
    const id = `heading-${tocIndex[level - 2]++}`
    headingIds.push({ id, level: level as 2 | 3 | 4, text: '' })
    return `<${tag} id="${id}">`
  })

  return html
})

let headingIds: TocItem[] = []
const tocIndex = [0, 0, 0] // h2, h3, h4

// Extract TOC items from rendered HTML
const tocItems = computed<TocItem[]>(() => {
  const items: TocItem[] = []
  const regex = /<(h[234])[^>]*id="([^"]*)"[^>]*>(.*?)<\/h[234]>/gi
  let match: RegExpExecArray | null
  const html = renderedHtml.value
  while ((match = regex.exec(html)) !== null) {
    const level = parseInt(match[1][1]) as 2 | 3 | 4
    const id = match[2]
    const text = match[3].replace(/<[^>]*>/g, '')
    items.push({ id, text, level })
  }
  return items
})

// Emit TOC when it changes
watchEffect(() => {
  emit('tocUpdated', tocItems.value)
})

// Activate code copy buttons in rendered HTML
onMounted(() => {
  if (bodyRef.value) {
    bodyRef.value.querySelectorAll('.code-copy').forEach((btn) => {
      btn.addEventListener('click', () => {
        const code = btn.parentElement?.nextElementSibling?.querySelector('code')
        if (code) {
          navigator.clipboard.writeText(code.textContent || '')
          btn.textContent = '已复制'
          setTimeout(() => { btn.textContent = '复制代码' }, 2000)
        }
      })
    })
  }
})
</script>

<style scoped>
.article-content {
  /* Typography cascade */
  font-size: var(--text-body);
  line-height: var(--leading-relaxed);
  color: var(--body);
}

/* Headings are unscoped so they work with v-html */
</style>

<style>
/* Article body global styles (must not be scoped for v-html) */
.article-body :deep(h2),
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

.article-body :deep(h3),
.article-content h3 {
  font-size: var(--text-title-md);
  font-weight: var(--weight-semibold);
  line-height: var(--leading-normal);
  color: var(--ink);
  margin-top: var(--space-xl);
  margin-bottom: var(--space-sm);
}

.article-body :deep(h4),
.article-content h4 {
  font-size: var(--text-title-sm);
  font-weight: var(--weight-semibold);
  line-height: var(--leading-normal);
  color: var(--ink);
  margin-top: var(--space-lg);
  margin-bottom: var(--space-sm);
}

.article-body :deep(p),
.article-content p {
  margin-bottom: var(--space-md);
}

.article-body :deep(strong),
.article-content strong {
  color: var(--body-strong);
  font-weight: var(--weight-semibold);
}

.article-body :deep(a),
.article-content a {
  color: var(--primary);
  text-decoration: underline;
}
.article-body :deep(a:hover),
.article-content a:hover {
  color: var(--primary-active);
}

[data-theme="light"] .article-body :deep(a),
[data-theme="light"] .article-content a {
  color: var(--ink);
  text-decoration-color: var(--primary);
}

.article-body :deep(blockquote),
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

.article-body :deep(ul),
.article-content ul,
.article-body :deep(ol),
.article-content ol {
  padding-left: var(--space-lg);
  margin-bottom: var(--space-md);
}
.article-body :deep(ul),
.article-content ul { list-style: disc; }
.article-body :deep(ol),
.article-content ol { list-style: decimal; }

.article-body :deep(li),
.article-content li {
  margin-bottom: var(--space-xxs);
}

.article-body :deep(img),
.article-content img {
  max-width: 100%;
  border-radius: var(--radius-md);
  margin: var(--space-lg) 0;
}

.article-body :deep(table),
.article-content table {
  width: 100%;
  border-collapse: collapse;
  margin: var(--space-lg) 0;
  font-size: var(--text-body-sm);
}

.article-body :deep(th),
.article-content th {
  text-align: left;
  padding: 10px 14px;
  background: var(--surface-card);
  color: var(--body-strong);
  font-size: var(--text-caption);
  font-weight: var(--weight-semibold);
  border-bottom: 1px solid var(--hairline-strong);
}

.article-body :deep(td),
.article-content td {
  padding: 10px 14px;
  border-bottom: 1px solid var(--hairline);
}

.article-body :deep(tr:hover td),
.article-content tr:hover td {
  background: var(--surface-soft);
}

/* Inline code */
.article-body :deep(:not(pre) > code),
.article-content :not(pre) > code {
  font-family: var(--font-mono);
  font-size: var(--text-code-inline);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  background: var(--surface-card);
  color: var(--primary);
  border: 1px solid var(--hairline);
}

[data-theme="light"] .article-body :deep(:not(pre) > code),
[data-theme="light"] .article-content :not(pre) > code {
  background: var(--surface-elevated);
  color: #92400e;
}

/* Code blocks rendered from markdown-it */
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
  transition: var(--transition-hover);
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
.article-content .code-block code {
  font-family: var(--font-mono);
}

/* Horizontal rule */
.article-body :deep(hr),
.article-content hr {
  border: none;
  border-top: 1px solid var(--hairline);
  margin: var(--space-xxl) 0;
}
</style>
