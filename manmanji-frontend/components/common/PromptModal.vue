<!--
  PromptModal.vue — 文本输入弹窗
  用途：新建文件夹、重命名文件夹、重命名文章

  设计规范（DESIGN.md）：
  - 卡片：surface-card(#1a1a1a) + rounded-lg(12px) + hairline 1px 边框 + padding xl(32px)，无阴影
  - 标题：title-md(18px/600/1.4) + ink(#ffffff)
  - 输入框：text-input 规范（surface-card 底 + rounded-md 8px + 40px 高 + focus 时 primary 黄色边框）
  - 确认按钮：button-primary（primary 底 + on-primary 黑色文字 + button typography + rounded-md 8px + 40px 高）
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

          <!-- 输入框 -->
          <input
            ref="inputRef"
            v-model="inputValue"
            class="modal-input"
            type="text"
            :placeholder="placeholder"
            :disabled="loading"
            @keydown.esc="onCancel"
            @keydown.enter="onConfirm"
          />

          <!-- 错误提示 -->
          <p v-if="error" class="modal-error">{{ error }}</p>

          <!-- 按钮区 -->
          <div class="modal-actions">
            <button class="btn-cancel" @click="onCancel" :disabled="loading">{{ cancelText }}</button>
            <button class="btn-confirm" @click="onConfirm" :disabled="loading || !inputValue.trim()">
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
  placeholder: string
  confirmText?: string
  cancelText?: string
  loading?: boolean
  error?: string | null
  modelValue?: string
}>(), {
  confirmText: '确定',
  cancelText: '取消',
  loading: false,
  error: null,
  modelValue: '',
})

const emit = defineEmits<{
  confirm: [value: string]
  cancel: []
}>()

const inputRef = ref<HTMLInputElement | null>(null)
const inputValue = ref(props.modelValue)

function onConfirm() {
  if (props.loading || !inputValue.value.trim()) return
  emit('confirm', inputValue.value.trim())
}

function onCancel() {
  if (props.loading) return
  emit('cancel')
}

watch(() => props.visible, (v) => {
  if (v) {
    inputValue.value = props.modelValue
    nextTick(() => inputRef.value?.focus())
  }
})
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
  margin-bottom: var(--space-lg);
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

/* text-input 规范 (DESIGN.md) */
.modal-input {
  width: 100%;
  height: 40px;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  border: 1px solid var(--hairline);
  background: var(--surface-card);
  color: var(--on-dark);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-regular);
  line-height: var(--leading-normal);
  transition: border-color 0.15s ease;
  margin-bottom: var(--space-md);
}
.modal-input::placeholder { color: var(--muted-soft); }
.modal-input:focus { outline: none; border-color: var(--primary); }
.modal-input:disabled { opacity: 0.5; }

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

/* button-primary 规范 (DESIGN.md) */
.btn-confirm {
  height: 40px;
  padding: 12px 20px;
  border-radius: var(--radius-md);
  border: none;
  background: var(--primary);
  color: var(--on-primary);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
  line-height: 1;
  cursor: pointer;
  transition: var(--transition-hover);
}
.btn-confirm:hover:not(:disabled) { background: var(--primary-active); }
.btn-confirm:disabled { background: var(--primary-disabled); color: var(--muted); cursor: not-allowed; }

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
