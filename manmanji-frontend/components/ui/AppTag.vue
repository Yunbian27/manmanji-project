<!--
  AppTag.vue — 标签组件（Uber 风格）
  浅灰胶囊标签，无第二强调色
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
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  line-height: var(--leading-normal);
  padding: 4px 12px;
  border-radius: var(--radius-pill);
  background: var(--canvas-soft);
  color: var(--muted);
}
</style>
