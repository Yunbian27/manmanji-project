<!--
  ConfirmModal.vue — 确认弹窗
  用途：删除文件夹、删除文章

  设计规范（DESIGN.md）：
  - 卡片：surface-card(#1a1a1a) + rounded-lg(12px) + hairline 1px 边框 + padding xl(32px)，无阴影
  - 标题：title-md(18px/600/1.4) + ink(#ffffff)
  - 消息文字：body-sm(14px/400/1.55) + muted(#888888)
  - 删除按钮：error(#ef4444) 底 + #ffffff 白色文字 + button typography + rounded-md 8px + 40px 高
  - 取消按钮：button-secondary（surface-card 底 + on-dark 白色文字）
  - 错误提示：error(#ef4444) + surface-soft 底 + body-sm typography
  - 关闭按钮：button-icon-circular（36×36 + rounded-full）
-->
<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click.self="onCancel">
        <div class="modal-card">
          <!-- 头部 -->
          <div class="modal-header">
            <h3 class="modal-title">{{ title }}</h3>
            <button class="modal-close" @click="onCancel" aria-label="关闭" :disabled="loading">
              <IconX :size="18" />
            </button>
          </div>

          <!-- 消息文字 -->
          <p class="modal-message">{{ message }}</p>

          <!-- 错误提示 -->
          <p v-if="error" class="modal-error">{{ error }}</p>

          <!-- 按钮区 -->
          <div class="modal-actions">
            <button class="btn-cancel" @click="onCancel" :disabled="loading">{{ cancelText }}</button>
            <button class="btn-danger" @click="$emit('confirm')" :disabled="loading">
              {{ loading ? '处理中...' : confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  visible: boolean
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  loading?: boolean
  error?: string | null
}>(), {
  confirmText: '删除',
  cancelText: '取消',
  loading: false,
  error: null,
})

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()

function onCancel() {
  if (props.loading) return
  emit('cancel')
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && props.visible) onCancel()
}
onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: var(--z-modal, 200);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-md);
}

.modal-card {
  width: 100%;
  max-width: 400px;
  background: var(--surface-card);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-md);
}

.modal-title {
  font-size: var(--text-title-md);
  font-weight: var(--weight-semibold);
  line-height: var(--leading-normal);
  color: var(--ink);
}

.modal-close {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  border: none;
  background: var(--surface-card);
  color: var(--muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition-hover);
  flex-shrink: 0;
}
.modal-close:hover:not(:disabled) { background: var(--surface-elevated); color: var(--ink); }
.modal-close:disabled { opacity: 0.5; cursor: not-allowed; }

/* body-sm 规范 (DESIGN.md) */
.modal-message {
  font-size: var(--text-body-sm);
  font-weight: var(--weight-regular);
  line-height: var(--leading-relaxed);
  color: var(--muted);
  margin: 0 0 var(--space-md) 0;
}

/* error-state 规范 (DESIGN.md) */
.modal-error {
  font-size: var(--text-caption);
  color: var(--error);
  padding: var(--space-xs) var(--space-sm);
  background: var(--surface-soft);
  border-radius: var(--radius-sm);
  margin: 0 0 var(--space-md) 0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
}

/* button-secondary 规范 (DESIGN.md) */
.btn-cancel {
  height: 40px;
  padding: 12px 20px;
  border-radius: var(--radius-md);
  border: 1px solid var(--hairline);
  background: var(--surface-card);
  color: var(--on-dark);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
  line-height: 1;
  cursor: pointer;
  transition: var(--transition-hover);
}
.btn-cancel:hover:not(:disabled) { background: var(--surface-elevated); }
.btn-cancel:disabled { opacity: 0.5; cursor: not-allowed; }

/* danger 按钮 — error(#ef4444) 底 + #ffffff 文字 + button 规范 (DESIGN.md) */
.btn-danger {
  height: 40px;
  padding: 12px 20px;
  border-radius: var(--radius-md);
  border: none;
  background: var(--error);
  color: #ffffff;
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
  line-height: 1;
  cursor: pointer;
  transition: var(--transition-hover);
}
.btn-danger:hover:not(:disabled) { opacity: 0.85; }
.btn-danger:disabled { opacity: 0.5; cursor: not-allowed; }

/* ========== Vue Transition: modal ========== */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}
.modal-enter-active .modal-card,
.modal-leave-active .modal-card {
  transition: transform 0.2s ease, opacity 0.2s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-from .modal-card {
  transform: scale(0.95) translateY(-8px);
  opacity: 0;
}
.modal-leave-to .modal-card {
  transform: scale(0.95) translateY(-8px);
  opacity: 0;
}
</style>
