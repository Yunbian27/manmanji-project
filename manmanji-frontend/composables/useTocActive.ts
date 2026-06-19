import type { TocItem } from '~/types'

/**
 * TOC 高亮追踪 — 用 IntersectionObserver 监听标题可见性
 * @param tocItems 目录条目（变化时重新绑定观察器）
 * @param root 滚动容器（不传则观察页面级滚动）
 */
export function useTocActive(tocItems: Ref<TocItem[]>, root?: Ref<HTMLElement | null>) {
  const activeTocId = ref('')
  let observer: IntersectionObserver | null = null

  function setup() {
    observer?.disconnect()
    nextTick(() => {
      const scope = root?.value ?? document
      const headings = scope.querySelectorAll<HTMLElement>('[id^="heading-"]')
      if (headings.length === 0) return
      observer = new IntersectionObserver(
        (entries) => {
          for (const entry of entries) {
            if (entry.isIntersecting) {
              activeTocId.value = entry.target.id
              return
            }
          }
        },
        {
          root: root?.value ?? null,
          rootMargin: root?.value ? '0px 0px -70% 0px' : '-60px 0px -70% 0px',
          threshold: 0,
        }
      )
      headings.forEach(h => observer!.observe(h))
    })
  }

  watch(tocItems, setup)
  onUnmounted(() => observer?.disconnect())

  return { activeTocId }
}
