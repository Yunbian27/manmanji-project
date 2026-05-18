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
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-xl);
  padding: var(--space-2xl);
  box-shadow: var(--shadow-2);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-md);
}

.modal-title {
  font-size: var(--text-body-lg);
  font-weight: var(--weight-medium);
  line-height: var(--leading-normal);
  color: var(--ink);
}

.modal-close {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  border: none;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition-hover);
  flex-shrink: 0;
}
.modal-close:hover:not(:disabled) { background: var(--canvas-soft); color: var(--ink); }
.modal-close:disabled { opacity: 0.5; cursor: not-allowed; }

/* body-sm 规范 (DESIGN.md) */
.modal-message {
  font-size: var(--text-body-sm);
  font-weight: var(--weight-regular);
  line-height: var(--leading-relaxed);
  color: var(--muted);
  margin: 0 0 var(--space-md) 0;
}

/* error-state */
.modal-error {
  font-size: var(--text-caption);
  color: #c0392b;
  padding: var(--space-xs) var(--space-sm);
  background: #fde8e8;
  border-radius: var(--radius-md);
  margin: 0 0 var(--space-md) 0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
}

/* button-secondary */
.btn-cancel {
  height: 40px;
  padding: 12px 20px;
  border-radius: var(--radius-pill);
  border: 1px solid var(--hairline);
  background: transparent;
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  line-height: 1;
  cursor: pointer;
  transition: var(--transition-hover);
}
.btn-cancel:hover:not(:disabled) { background: var(--canvas-soft); }
.btn-cancel:disabled { opacity: 0.5; cursor: not-allowed; }

/* danger 按钮 */
.btn-danger {
  height: 40px;
  padding: 12px 20px;
  border-radius: var(--radius-pill);
  border: none;
  background: #c0392b;
  color: var(--on-primary);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  line-height: 1;
  cursor: pointer;
  transition: var(--transition-hover);
}
.btn-danger:hover:not(:disabled) { background: #a93226; }
.btn-danger:disabled { opacity: 0.5; cursor: not-allowed; }

</style>
