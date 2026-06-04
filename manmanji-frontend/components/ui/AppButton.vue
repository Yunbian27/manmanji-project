<!--
  AppButton.vue — 统一按钮组件（Notion DESIGN.md 规范）
  - primary: #2563eb 蓝底白字，8px 圆角
  - secondary: 透明 + 描边，8px 圆角
  - subtle: 透明幽灵按钮，6px 圆角
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
  font-size: var(--button-md);
  font-weight: var(--weight-medium);
  line-height: var(--leading-button);
  cursor: pointer;
  border: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  transition: background-color 0.15s var(--ease), color 0.15s var(--ease);
}
.app-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* primary: 蓝底白字，8px 圆角 */
.app-btn--primary {
  background: var(--primary);
  color: var(--on-primary);
  border-radius: var(--rounded-md);
  padding: 10px 18px;
}
.app-btn--primary:hover:not(:disabled) { background: var(--primary-pressed); }

/* secondary: 透明 + 描边，8px 圆角 */
.app-btn--secondary {
  background: transparent;
  color: var(--ink);
  border: 1px solid var(--hairline-strong);
  border-radius: var(--rounded-md);
  padding: 10px 18px;
}
.app-btn--secondary:hover:not(:disabled) { background: var(--hairline-soft); }

/* subtle: 透明幽灵按钮，6px 圆角 */
.app-btn--subtle {
  background: transparent;
  color: var(--ink);
  border-radius: var(--rounded-sm);
  padding: 8px 12px;
}
.app-btn--subtle:hover:not(:disabled) { background: var(--hairline-soft); }
</style>
