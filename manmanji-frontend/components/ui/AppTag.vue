<template>
  <span :class="['app-tag', variant === 'yellow' ? 'app-tag--yellow' : 'app-tag--default']">
    <slot />
  </span>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ variant?: 'default' | 'yellow' }>(), { variant: 'default' })
</script>

<style scoped>
.app-tag {
  font-family: var(--font-sans);
  padding: 4px 12px;
  border-radius: var(--radius-pill);
  transition: var(--transition-theme);
}

.app-tag--default {
  background: var(--surface-card);
  color: var(--body);
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  line-height: var(--leading-normal);
}

.app-tag--yellow {
  background: var(--primary);
  color: #0a0a0a;
  font-size: var(--text-uppercase);
  font-weight: var(--weight-semibold);
  line-height: var(--leading-normal);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;
}
</style>
