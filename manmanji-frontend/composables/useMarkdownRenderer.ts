import MarkdownIt from 'markdown-it'
import type { TocItem } from '~/types'
import type { ComputedRef } from 'vue'

/**
 * Markdown 渲染器 — 从 ArticleBody.vue 提取的独立 composable
 * 供文章阅读页和编辑器预览区复用
 */
export function useMarkdownRenderer(content: ComputedRef<string> | Ref<string>) {
  const md = new MarkdownIt({
    html: false,
    linkify: true,
    typographer: true,
    breaks: false,
  })

  md.renderer.rules.fence = (tokens, idx) => {
    const token = tokens[idx]!
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
    let html = md.render(unref(content))
    html = html.replace(/<(h[23456])>/g, (_, tag) => {
      const level = parseInt(tag[1]!) as 2 | 3 | 4 | 5 | 6
      const id = `heading-${tocIndex[level - 2]!++}`
      return `<${tag} id="${id}">`
    })
    return html
  })

  const tocIndex = [0, 0, 0, 0, 0]

  const tocItems = computed<TocItem[]>(() => {
    const items: TocItem[] = []
    const regex = /<(h[23456])[^>]*id="([^"]*)"[^>]*>(.*?)<\/h[23456]>/gi
    let match: RegExpExecArray | null
    const html = renderedHtml.value
    while ((match = regex.exec(html)) !== null) {
      const level = parseInt(match[1]![1]!) as 2 | 3 | 4 | 5 | 6
      const id = match[2]!
      const text = match[3]!.replace(/<[^>]*>/g, '')
      items.push({ id, text, level })
    }
    return items
  })

  return { renderedHtml, tocItems }
}
