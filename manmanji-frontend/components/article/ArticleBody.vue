<template>
  <article class="article-body">
    <div v-if="renderedHtml" ref="bodyRef" class="article-content" v-html="renderedHtml" />
    <div v-else class="article-empty">
      <p>暂无内容</p>
    </div>
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
  color: var(--ink);                    /* body paragraphs = 纯黑 (DESIGN.md) */
}
.article-empty {
  padding: var(--space-3xl) 0;
  text-align: center;
  color: var(--muted);
  font-size: var(--text-body);
}
</style>

<style>
.article-content h2 {
  font-size: var(--text-display-md);    /* 24px */
  font-weight: var(--weight-bold);      /* 700 */
  line-height: 32px;                    /* BLUEPRINT 207 */
  color: var(--ink);
  margin-top: var(--space-3xl);         /* 32px */
  margin-bottom: var(--space-sm);
}
.article-content h3 {
  font-size: var(--display-sm);         /* 20px (BLUEPRINT 208) */
  font-weight: var(--weight-bold);      /* 700 */
  line-height: 28px;
  color: var(--ink);
  margin-top: var(--space-2xl);         /* 24px */
  margin-bottom: var(--space-sm);
}
.article-content h4 {
  font-size: var(--text-body-md-strong);
  font-weight: var(--weight-medium);
  color: var(--ink);
  margin-top: var(--space-lg);
  margin-bottom: var(--space-sm);
}
.article-content p { margin-bottom: var(--space-lg); }
.article-content strong { color: var(--body-strong); font-weight: var(--weight-medium); }
.article-content a { color: var(--link); text-decoration: underline; }
.article-content a:hover { color: var(--ink); }
.article-content blockquote {
  border-left: 3px solid var(--ink);
  padding: 0 var(--space-lg);          /* 16px left (BLUEPRINT 211) */
  margin: var(--space-lg) 0;
  color: var(--body);
}
.article-content ul, .article-content ol {
  padding-left: var(--space-2xl);       /* 24px (BLUEPRINT 212) */
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
  background: var(--canvas-soft);
  color: var(--body-strong);
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  border-bottom: 1px solid var(--hairline-strong);
}
.article-content td {
  padding: 10px 14px;
  border-bottom: 1px solid var(--hairline);
}
.article-content tr:hover td { background: var(--surface-soft); }
.article-content :not(pre) > code {
  font-family: var(--font-mono);
  font-size: var(--text-body-sm);       /* 14px (BLUEPRINT 209) */
  padding: 2px 6px;
  border-radius: var(--radius-md);
  background: var(--canvas-softer);
  color: var(--ink);
}
.article-content .code-block {
  border-radius: var(--radius-xl);
  border: 1px solid var(--hairline);
  background: var(--canvas-soft);
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
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  color: var(--muted);
}
.article-content .code-copy {
  font-family: var(--font-sans);
  font-size: var(--text-caption);
  color: var(--muted);
  background: transparent;
  border: none;
  cursor: pointer;
  padding: var(--space-xxs) var(--space-xs);
  border-radius: var(--radius-pill);
}
.article-content .code-copy:hover { color: var(--ink); }
.article-content .code-block pre {
  padding: var(--space-lg);             /* 16px (BLUEPRINT 210) */
  font-size: var(--text-code);
  line-height: var(--leading-relaxed);
  tab-size: 2;
  overflow-x: auto;
  margin: 0;
}
.article-content .code-block code { font-family: var(--font-mono); }

/* 代码块语法高亮（只设颜色/字重，不改容器样式） */
.article-content .hljs-keyword,
.article-content .hljs-selector-tag,
.article-content .hljs-type { color: #d73a49; }                      /* 红 — 关键字、类型 */
.article-content .hljs-string,
.article-content .hljs-addition { color: #032f62; }                  /* 深蓝 — 字符串 */
.article-content .hljs-comment,
.article-content .hljs-quote { color: #6a737d; font-style: italic; } /* 灰 — 注释 */
.article-content .hljs-number,
.article-content .hljs-literal,
.article-content .hljs-template-variable { color: #005cc5; }         /* 蓝 — 数字、字面量 */
.article-content .hljs-title,
.article-content .hljs-section,
.article-content .hljs-selector-id,
.article-content .hljs-selector-class { color: #6f42c1; }            /* 紫 — 函数名、标题 */
.article-content .hljs-built_in,
.article-content .hljs-name,
.article-content .hljs-attr { color: #22863a; }                      /* 绿 — 内置、属性 */
.article-content .hljs-regexp,
.article-content .hljs-link,
.article-content .hljs-symbol { color: #032f62; }                    /* 深蓝 — 正则、符号 */
.article-content .hljs-params,
.article-content .hljs-variable { color: #e36209; }                  /* 橙 — 参数、变量 */
.article-content .hljs-meta { color: #6a737d; }                      /* 灰 — 元信息 */
.article-content .hljs-deletion { color: #b31d28; }                  /* 深红 — 删除线 */
.article-content .hljs-template-tag { color: #22863a; }              /* 绿 — 模板标签 */
.article-content .hljs-emphasis { font-style: italic; }

.article-content hr {
  border: none;
  border-top: 1px solid var(--hairline);
  margin: var(--space-xxl) 0;
}
</style>
