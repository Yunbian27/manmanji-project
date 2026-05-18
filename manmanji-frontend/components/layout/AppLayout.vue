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

/* 主内容区：自然流式滚动，不再独立 fixed-height 滚动 */
.main-content {
  flex: 0 0 auto;
  max-width: var(--content-max);        /* 1000px */
  width: 100%;
  padding: var(--space-xxl) var(--space-xxl);       /* 24px */
}

/* 响应式断点 (BLUEPRINT: Tablet 1119, Desktop 1120, Mobile 599) */
@media (max-width: 1119px) {
  .main-content { max-width: 860px; padding: var(--space-xl) var(--space-xl); }
}
@media (max-width: 767px) {
  .main-content { max-width: 100%; padding: var(--space-xl); }
}
@media (max-width: 599px) {
  .main-content { padding: var(--space-lg); }
}
</style>
