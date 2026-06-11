import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import mermaid from 'mermaid'
import { emojiPlugin, initMdPlugins } from './md-plugin-loader'
import type { TocItem } from '~/types'
import type { ComputedRef } from 'vue'

mermaid.initialize({ startOnLoad: false, theme: 'default' })

/**
 * Markdown 渲染器
 */
export function useMarkdownRenderer(content: ComputedRef<string> | Ref<string>) {
  const md = new MarkdownIt({
    html: false,
    linkify: true,
    typographer: true,
    breaks: true,
  })

  md.use(emojiPlugin)

  // 链接新窗口打开
  const defaultLinkRender = md.renderer.rules.link_open || ((tokens, idx, options, _env, self) => self.renderToken(tokens, idx, options))
  md.renderer.rules.link_open = (tokens, idx, options, env, self) => {
    const token = tokens[idx]!
    token.attrSet('target', '_blank')
    token.attrSet('rel', 'noopener noreferrer')
    return defaultLinkRender(tokens, idx, options, env, self)
  }

  // CJS plugins loaded async — available on next tick
  initMdPlugins(md)

  md.renderer.rules.fence = (tokens, idx) => {
    const token = tokens[idx]!
    const lang = token.info.trim() || 'text'
    const code = token.content

    if (lang === 'mermaid') {
      return `<div class="mermaid">${escapeHtml(code)}</div>`
    }

    const highlighted = highlightCode(code, lang)
    return `<div class="code-block"><div class="code-block-header"><span class="code-lang">${lang}</span><button class="code-copy" onclick="navigator.clipboard.writeText(this.closest('.code-block').querySelector('code').textContent);var b=this;b.textContent='已复制';setTimeout(function(){b.textContent='复制代码'},2000)">复制代码</button></div><pre><code class="hljs">${highlighted}</code></pre></div>`
  }

  function highlightCode(code: string, lang: string): string {
    if (!lang || lang === 'text' || lang === 'plain' || lang === 'plaintext') {
      return escapeHtml(code)
    }
    if (hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  }

  function escapeHtml(str: string): string {
    return str
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
  }

  const tocIndex = [0, 0, 0, 0, 0, 0]

  const renderedHtml = ref('')

  function doRender(text: string) {
    for (let i = 0; i < tocIndex.length; i++) tocIndex[i] = 0
    let html = md.render(text || '')
    html = html.replace(/<(h[123456])>/g, (_, tag) => {
      const level = parseInt(tag[1]!) as 1 | 2 | 3 | 4 | 5 | 6
      const id = `heading-${tocIndex[level - 1]!++}`
      return `<${tag} id="${id}">`
    })
    renderedHtml.value = html
  }

  doRender(unref(content) || '')

  watch(() => unref(content), (text) => {
    doRender(text || '')
  })

  // 异步插件加载完成后重新渲染，确保首次渲染就包含插件效果
  initMdPlugins(md).then(() => {
    doRender(unref(content) || '')
  })

  const tocItems = computed<TocItem[]>(() => {
    const items: TocItem[] = []
    const regex = /<(h[123456])[^>]*id="([^"]*)"[^>]*>(.*?)<\/h[123456]>/gi
    let match: RegExpExecArray | null
    const html = renderedHtml.value
    while ((match = regex.exec(html)) !== null) {
      const level = parseInt(match[1]![1]!) as 1 | 2 | 3 | 4 | 5 | 6
      const id = match[2]!
      const text = match[3]!.replace(/<[^>]*>/g, '')
      items.push({ id, text, level })
    }
    return items
  })

  watch(renderedHtml, async () => {
    await nextTick()
    try {
      await mermaid.run({ querySelector: '.mermaid' })
    } catch {
      // mermaid 渲染失败时忽略
    }
  })

  return { renderedHtml, tocItems }
}
