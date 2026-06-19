<!--
  layouts/editor.vue — 编辑器布局（Notion DESIGN.md 规范）
  全视口，无 Footer
-->
<template>
  <div class="editor-layout">
    <slot />
    <Toast />
  </div>
</template>

<script setup lang="ts">
provideToast()

onMounted(() => {
  document.documentElement.style.overflow = 'hidden'
})
onUnmounted(() => {
  document.documentElement.style.overflow = ''
})
</script>

<style scoped>
.editor-layout {
  height: 100%; overflow: hidden;
  background: var(--surface-soft);
}
</style>
