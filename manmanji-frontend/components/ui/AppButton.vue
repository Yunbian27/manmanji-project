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
  variant?: 'primary' | 'secondary' | 'text' | 'icon'
  disabled?: boolean
}>()

defineEmits<{ click: [] }>()
</script>

<style scoped>
.app-btn {
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
  line-height: 1;
  border-radius: var(--radius-md);
  cursor: pointer;
  border: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-xs);
  padding: 12px 20px;
  height: 40px;
  transition: var(--transition-hover);
}
.app-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.app-btn--primary {
  background: var(--primary);
  color: #0a0a0a;
}
.app-btn--primary:hover:not(:disabled) { background: var(--primary-active); }

.app-btn--secondary {
  background: var(--surface-card);
  color: var(--on-dark);
  border: 1px solid var(--hairline);
}
.app-btn--secondary:hover:not(:disabled) { background: var(--surface-elevated); }

.app-btn--text {
  background: transparent;
  color: var(--on-dark);
  padding: 12px 8px;
  height: auto;
}
.app-btn--text:hover:not(:disabled) { color: var(--primary); }

.app-btn--icon {
  background: var(--surface-card);
  color: var(--on-dark);
  width: 36px;
  height: 36px;
  padding: 0;
  border-radius: var(--radius-full);
}
.app-btn--icon:hover:not(:disabled) { background: var(--surface-elevated); }
</style>
