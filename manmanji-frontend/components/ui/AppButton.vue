<!--
  AppButton.vue — 统一按钮组件（Uber 风格）
  三种变体：
  - primary: 黑色胶囊（主要 CTA）
  - secondary: 白色胶囊 + 描边（次要操作）
  - subtle: 浅灰胶囊（三级操作）
-->
<template>
  <button
    :class="['app-btn', `app-btn--${variant}`]"
    :disabled="disabled"
    type="button"
    @click="$emit('click')"
  >
    <slot />
  </button>
</template>

<script setup lang="ts">
defineProps<{
  variant?: 'primary' | 'secondary' | 'subtle'
  disabled?: boolean
}>()

defineEmits<{ click: [] }>()
</script>

<style scoped>
.app-btn {
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  line-height: 1;
  border-radius: var(--radius-pill);        /* 999px — 胶囊形签名形状 */
  cursor: pointer;
  border: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-xs);
  padding: var(--space-md) var(--space-md); /* 12px — 标准胶囊内边距 */
  height: 40px;
  transition: var(--transition-hover);
}
.app-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* 主按钮：黑色背景 + 白色文字 */
.app-btn--primary {
  background: var(--primary);
  color: var(--on-primary);
}
.app-btn--primary:hover:not(:disabled) { background: var(--primary-active); }

/* 次按钮：白色背景 + 黑色文字 + 细边框 */
.app-btn--secondary {
  background: transparent;
  color: var(--ink);
  border: 1px solid var(--hairline);
}
.app-btn--secondary:hover:not(:disabled) { background: var(--canvas-soft); }

/* 弱按钮：浅灰背景 + 黑色文字 */
.app-btn--subtle {
  background: var(--canvas-soft);
  color: var(--ink);
}
.app-btn--subtle:hover:not(:disabled) { background: var(--surface-elevated); }
</style>
