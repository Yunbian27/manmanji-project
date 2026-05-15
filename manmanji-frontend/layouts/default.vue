<!--
  layouts/default.vue — 默认布局
  所有页面共用：TopNav + 页面内容
  Nuxt 自动将页面组件渲染到 <slot /> 中
-->
<template>
  <div class="layout">
    <TopNav />
    <slot />
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  background: var(--canvas);
}
</style>
