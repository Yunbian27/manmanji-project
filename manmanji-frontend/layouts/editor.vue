<!--
  layouts/editor.vue — 编辑器布局（Notion DESIGN.md 规范）
  全视口，无 Footer
-->
<template>
  <div class="editor-layout">
    <slot />
  </div>
</template>

<style scoped>
.editor-layout {
  height: 100vh;
  background: var(--surface-soft);
  overflow: hidden;
}
</style>
