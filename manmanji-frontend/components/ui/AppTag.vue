<!--
  AppTag.vue — 标签组件（Notion DESIGN.md 规范）
  浅灰背景标签芯片，4px 圆角
-->
<template>
  <span class="app-tag">
    <slot />
  </span>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ variant?: string }>(), { variant: 'default' })
</script>

<style scoped>
.app-tag {
  font-family: var(--font-sans);
  font-size: var(--caption-bold);
  font-weight: var(--weight-semibold);
  padding: 2px 8px;
  border-radius: var(--rounded-xs);
  background: var(--surface);
  color: var(--slate);
}
</style>
