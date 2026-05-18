<!--
  layouts/editor.vue — 编辑器专属布局（无 Footer）
  EditorTopNav 在 EditorView 内部渲染，确保 provide/inject 正确
-->
<template>
  <div class="editor-layout">
    <slot />
  </div>
</template>

<style scoped>
.editor-layout {
  min-height: 100vh;
  background: var(--canvas);
}
</style>
