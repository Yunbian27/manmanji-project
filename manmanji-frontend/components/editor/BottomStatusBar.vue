<template>
  <div class="bottom-status-bar">
    <div class="status-left">
      <span class="stat-item">{{ wordCount }} 字</span>
      <span class="stat-divider">·</span>
      <span class="stat-item">{{ lineCount }} 行</span>
      <span class="stat-divider">·</span>
      <span class="stat-item">第 {{ cursorRow }} 行，第 {{ cursorCol }} 列</span>
    </div>
    <div class="status-right">
      <span v-if="lastSavedAt" class="save-timestamp">已保存 {{ lastSavedAt }}</span>
      <button class="status-btn save-btn" :disabled="isSaving" @click="emit('save')">
        {{ isSaving ? '保存中...' : '保存草稿' }}
      </button>
      <button class="status-btn publish-btn" :disabled="isSaving" @click="emit('openPublishSettings')">
        发布文章
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  wordCount: number
  lineCount: number
  cursorRow: number
  cursorCol: number
  isSaving: boolean
  lastSavedAt: string | null
}>()

const emit = defineEmits<{
  save: []
  openPublishSettings: []
}>()
</script>

<style scoped>
.bottom-status-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 44px;
  padding: 0 var(--spacing-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-top: 1px solid var(--hairline);
  z-index: 99;
  font-size: var(--caption);
  color: var(--steel);
}

.status-left {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-divider {
  color: var(--hairline-strong);
}

.status-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.save-timestamp {
  color: var(--muted);
  font-size: var(--caption);
}

.status-btn {
  height: 30px;
  padding: 0 var(--spacing-sm);
  border: none;
  border-radius: var(--rounded-sm);
  font-family: var(--font-sans);
  font-size: var(--caption);
  font-weight: var(--weight-medium);
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.12s var(--ease), color 0.12s var(--ease);
}

.save-btn {
  background: var(--surface);
  color: var(--steel);
}

.save-btn:hover:not(:disabled) {
  background: var(--hairline-soft);
  color: var(--ink);
}

.publish-btn {
  background: var(--primary);
  color: var(--on-primary);
}

.publish-btn:hover:not(:disabled) {
  background: var(--primary-pressed);
}

.status-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
