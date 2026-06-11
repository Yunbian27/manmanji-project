declare module 'markdown-it-katex' {
  const plugin: (md: any, options?: { throwOnError?: boolean; errorColor?: string }) => void
  export default plugin
}

declare module 'markdown-it-task-lists' {
  const plugin: (md: any, options?: { enabled?: boolean }) => void
  export default plugin
}

declare module 'markdown-it-emoji' {
  export const bare: (md: any, options?: any) => void
  export const full: (md: any, options?: any) => void
  export const light: (md: any, options?: any) => void
}

declare module 'markdown-it-footnote' {
  const plugin: (md: any) => void
  export default plugin
}
