// Loads emoji (ESM) and CJS-only markdown-it plugins.

// markdown-it-emoji v3 exports named: bare / full / light
export { full as emojiPlugin } from 'markdown-it-emoji'

let katexPlugin: any = null
let taskListsPlugin: any = null
let footnotePlugin: any = null

export async function initMdPlugins(md: any) {
  if (footnotePlugin) {
    md.use(footnotePlugin)
    md.use(katexPlugin, { throwOnError: false, errorColor: '#e03131' })
    md.use(taskListsPlugin, { enabled: true })
    return
  }

  const [kMod, tMod, fMod, markMod] = await Promise.all([
    import('markdown-it-katex'),
    import('markdown-it-task-lists'),
    import('markdown-it-footnote'),
    import('markdown-it-mark'),
  ])

  katexPlugin = kMod.default || kMod
  taskListsPlugin = tMod.default || tMod
  footnotePlugin = fMod.default || fMod
  const markPlugin = markMod.default || markMod

  md.use(footnotePlugin)
  md.use(markPlugin)
  md.use(katexPlugin, { throwOnError: false, errorColor: '#e03131' })
  md.use(taskListsPlugin, { enabled: true })
}
