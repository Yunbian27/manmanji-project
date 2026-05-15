// ============================================================
// composables/useDevice.ts — 响应式设备检测
// 使用 VueUse 的 useMediaQuery 判断当前屏幕宽度
// 用于响应式布局的组件条件渲染
//
// VueUse: Vue 生态最流行的工具库，提供 200+ 组合式函数
// useMediaQuery: 响应式地监听 CSS 媒体查询
// ============================================================

export function useDevice() {
  // 移动端：< 768px（隐藏侧边栏，汉堡菜单）
  const isMobile = useMediaQuery('(max-width: 767px)')
  // 平板：< 1024px（隐藏右侧目录栏）
  const isTablet = useMediaQuery('(max-width: 1023px)')
  // 桌面端：≥ 1024px（三栏全显）
  const isDesktop = useMediaQuery('(min-width: 1024px)')

  return { isMobile, isTablet, isDesktop }
}
