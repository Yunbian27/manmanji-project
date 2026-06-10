<template>
  <div class="toast-container">
    <TransitionGroup name="toast">
      <div
        v-for="item in toasts"
        :key="item.id"
        class="toast-item"
        :class="'toast--' + item.type"
      >
        <svg v-if="item.type === 'success'" class="toast-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
        <svg v-else class="toast-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
        <span class="toast-message">{{ item.message }}</span>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
const { toasts } = injectToast()
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  pointer-events: none;
}

.toast-item {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 200px;
  max-width: 360px;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--canvas);
  border-radius: var(--rounded-md);
  box-shadow: rgba(15, 15, 15, 0.16) 0px 4px 12px 0px;
  pointer-events: auto;
}

.toast--success {
  border-left: 3px solid var(--semantic-success);
}

.toast--error {
  border-left: 3px solid var(--semantic-error);
}

.toast-icon {
  flex-shrink: 0;
}

.toast--success .toast-icon { color: var(--semantic-success); }
.toast--error .toast-icon { color: var(--semantic-error); }

.toast-message {
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  font-weight: var(--weight-regular);
  color: var(--ink);
  line-height: 1.4;
}

/* ── TransitionGroup ── */
.toast-enter-active {
  transition: all 0.3s var(--ease);
}

.toast-leave-active {
  transition: all 0.25s var(--ease);
}

.toast-enter-from {
  opacity: 0;
  transform: translateY(-12px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.toast-move {
  transition: transform 0.25s var(--ease);
}
</style>
