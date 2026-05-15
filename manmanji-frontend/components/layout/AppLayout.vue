<!--
  AppLayout.vue — 三栏布局容器
  使用 flex 布局实现经典的"左栏-主内容-右栏"结构

  Slot（插槽）：
  - #left-sidebar:  左侧文件夹栏
  - default:        中间主内容区（文章）
  - #right-sidebar: 右侧目录栏

  使用示例（在 pages/index.vue 中）：
  <AppLayout>
    <template #left-sidebar><LeftSidebar ... /></template>
    <ArticleContent ... />
    <template #right-sidebar><RightSidebar ... /></template>
  </AppLayout>
-->
<template>
  <div class="app-layout">
    <!-- 命名 slot：左侧栏 -->
    <slot name="left-sidebar" />

    <!-- 默认 slot：中间内容区，.content-area 供跳转函数引用 -->
    <main class="main-content content-area">
      <slot />
    </main>

    <!-- 命名 slot：右侧目录 -->
    <slot name="right-sidebar" />
  </div>
</template>

<style scoped>
/* 三栏 flex 布局：总高度 = 视口 - 导航栏 */
.app-layout {
  display: flex;
  min-height: calc(100vh - var(--nav-height));
  background: var(--canvas);
}

/* 主内容区：不参与 flex 拉伸（flex: 0 0 auto），固定最大宽度并居中 */
.main-content {
  flex: 0 0 auto;
  max-width: var(--content-max);        /* 1000px */
  width: 100%;
  padding: var(--space-xxl) var(--space-xxl);       /* 48px 上下，48px 左右 */
  overflow-y: auto;                      /* 内容区独立滚动 */
  height: calc(100vh - var(--nav-height));
  scrollbar-width: auto;
  scrollbar-color: auto;
}
.main-content::-webkit-scrollbar { width: auto; }
.main-content::-webkit-scrollbar-track { background: initial; }
.main-content::-webkit-scrollbar-thumb { background: initial; border-radius: initial; }
.main-content::-webkit-scrollbar-thumb:hover { background: initial; }

/* 响应式断点 */
@media (max-width: 1200px) {
  /* 中屏：主内容缩窄 */
  .main-content { max-width: 860px; padding: var(--space-xl) var(--space-xl); }
}
@media (max-width: 1024px) {
  /* 平板：主内容 100% 宽度 */
  .main-content { max-width: 100%; padding: var(--space-xl); }
}
@media (max-width: 768px) {
  /* 手机：最小 padding */
  .main-content { padding: var(--space-lg) 20px; }
}
</style>
