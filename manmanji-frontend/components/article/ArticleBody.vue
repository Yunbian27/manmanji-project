<!--
  ArticleBody.vue — 文章正文渲染组件
  将 Markdown 字符串渲染为 HTML，并提取标题生成目录

  技术栈：
  - markdown-it: Markdown → HTML 转换
  - 自定义 fence 规则：代码块包装成 .code-block 结构（带语言标签和复制按钮）
  - TOC 提取：从渲染后的 HTML 中提取 h2/h3 id 和文本

  Events:
  - toc-updated: 每次标题变化时通知父组件（供 RightSidebar 使用）

  样式穿透：
  由于 v-html 渲染的内容不受 scoped 样式影响，
  使用非 scoped 的 <style> 块，通过 .article-content 限定范围
-->
<template>
  <article class="article-body">
    <!-- v-html: 将 HTML 字符串渲染成真实 DOM（注意：确保内容安全，不含 XSS） -->
    <div ref="bodyRef" class="article-content" v-html="renderedHtml" />
  </article>
</template>

<script setup lang="ts">
import MarkdownIt from 'markdown-it'
import type { TocItem } from '~/types'

const props = defineProps<{ content: string }>()

const emit = defineEmits<{ tocUpdated: [items: TocItem[]] }>()

// 创建 markdown-it 实例
const md = new MarkdownIt({
  html: false,          // 禁止原始 HTML（安全考虑）
  linkify: true,        // 自动识别 URL 并转为链接
  typographer: true,    // 智能引号转换
  breaks: false,        // 不将换行转为 <br>
})

/**
 * 自定义 fence 渲染规则（``` 代码块）
 * 把 markdown-it 默认的 <pre><code> 替换成带语言标签和复制按钮的结构
 */
md.renderer.rules.fence = (tokens, idx, _options, _env, _self) => {
  const token = tokens[idx]!
  const lang = token.info.trim() || 'text'
  const code = token.content
  const escaped = escapeHtml(code)
  // 返回自定义 HTML 结构
  return `<div class="code-block"><div class="code-block-header"><span class="code-lang">${lang}</span><button class="code-copy" onclick="navigator.clipboard.writeText(this.closest('.code-block').querySelector('code').textContent);var b=this;b.textContent='已复制';setTimeout(function(){b.textContent='复制代码'},2000)">复制代码</button></div><pre><code>${escaped}</code></pre></div>`
}

/** HTML 实体转义（防止 XSS） */
function escapeHtml(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

/** 将 Markdown 转为 HTML，并为标题添加 id */
const renderedHtml = computed(() => {
  let html = md.render(props.content)
  // 给每个 h2/h3/h4 添加 id="heading-N" 用于目录锚点
  html = html.replace(/<(h[234])>/g, (_, tag) => {
    const level = parseInt(tag[1]!) as 2 | 3 | 4
    const id = `heading-${tocIndex[level - 2]!++}`
    return `<${tag} id="${id}">`
  })
  return html
})

let tocIndex = [0, 0, 0]  // h2, h3, h4 的计数器

/** 从渲染的 HTML 中提取目录项 */
const tocItems = computed<TocItem[]>(() => {
  const items: TocItem[] = []
  // 正则匹配 <h2 id="heading-0">标题文字</h2>
  const regex = /<(h[234])[^>]*id="([^"]*)"[^>]*>(.*?)<\/h[234]>/gi
  let match: RegExpExecArray | null
  const html = renderedHtml.value
  while ((match = regex.exec(html)) !== null) {
    const level = parseInt(match[1]![1]!) as 2 | 3 | 4
    const id = match[2]!
    const text = match[3]!.replace(/<[^>]*>/g, '')  // 去除嵌套标签
    items.push({ id, text, level })
  }
  return items
})

// watchEffect: 响应式副作用，tocItems 变化时自动 emit 给父组件
watchEffect(() => {
  emit('tocUpdated', tocItems.value)
})
</script>

<style scoped>
/* scoped 样式：.article-content 容器本身 */
.article-content {
  font-size: var(--text-body);
  line-height: var(--leading-relaxed);
  color: var(--body);
}
</style>

<!--
  非 scoped 样式块：用于 v-html 渲染的内容
  由于 v-html 插入的 DOM 不受 scoped 限制，这里用全局选择器
  但通过 .article-content 前缀限定范围，避免污染其他组件
-->
<style>
/* === 标题排版 === */
.article-content h2 {
  font-size: var(--text-title-lg);         /* 24px */
  font-weight: var(--weight-bold);         /* 700 */
  line-height: 1.3;
  letter-spacing: var(--tracking-semi-tight);
  color: var(--ink);
  margin-top: var(--space-xxl);            /* 上距 48px */
  border-top: 1px solid var(--hairline);   /* 上方分割线 */
  padding-top: var(--space-lg);
  margin-bottom: var(--space-md);
}
.article-content h3 {
  font-size: var(--text-title-md);         /* 18px */
  font-weight: var(--weight-semibold);     /* 600 */
  color: var(--ink);
  margin-top: var(--space-xl);
  margin-bottom: var(--space-sm);
}
.article-content h4 {
  font-size: var(--text-title-sm);         /* 16px */
  font-weight: var(--weight-semibold);
  color: var(--ink);
  margin-top: var(--space-lg);
  margin-bottom: var(--space-sm);
}

/* === 段落 === */
.article-content p { margin-bottom: var(--space-md); }
.article-content strong { color: var(--body-strong); font-weight: var(--weight-semibold); }

/* === 链接 === */
/* 深色：黄色链接 + 下划线，浅色：黑字 + 黄色下划线 */
.article-content a { color: var(--primary); text-decoration: underline; }
.article-content a:hover { color: var(--primary-active); }
[data-theme="light"] .article-content a { color: var(--ink); text-decoration-color: var(--primary); }

/* === 引用块 === */
/* 左侧 3px 黄色边框，浅色背景，斜体 */
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

/* === 列表 === */
.article-content ul, .article-content ol {
  padding-left: var(--space-lg);
  margin-bottom: var(--space-md);
}
.article-content ul { list-style: disc; }
.article-content ol { list-style: decimal; }
.article-content li { margin-bottom: var(--space-xxs); }

/* === 图片 === */
.article-content img {
  max-width: 100%;
  border-radius: var(--radius-md);
  margin: var(--space-lg) 0;
}

/* === 表格 === */
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

/* === 内联代码 === */
/* :not(pre) > code: 只选中非代码块内的 code 元素 */
.article-content :not(pre) > code {
  font-family: var(--font-mono);
  font-size: var(--text-code-inline);      /* 13px */
  padding: 2px 6px;
  border-radius: var(--radius-sm);         /* 6px */
  background: var(--surface-card);
  color: var(--primary);                   /* 深色：黄色文字 */
  border: 1px solid var(--hairline);
}
[data-theme="light"] .article-content :not(pre) > code {
  background: var(--surface-elevated);
  color: #92400e;                          /* 浅色：棕色文字 */
}

/* === 代码块（由 markdown-it fence 规则渲染） === */
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

/* === 分割线 === */
.article-content hr {
  border: none;
  border-top: 1px solid var(--hairline);
  margin: var(--space-xxl) 0;
}
</style>
