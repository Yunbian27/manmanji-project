<!--
  LoginModal.vue — 登录弹窗组件
  半透明遮罩 + 居中卡片，用户输入用户名密码后请求后端登录

  三种状态：
  - idle: 表单正常可输入
  - loading: 登录按钮禁用 + 显示"登录中..."
  - error: 红色错误提示文字

  v-model: boolean 控制弹窗显隐
-->
<template>
  <!-- Teleport: 将弹窗渲染到 body 下，避免被父元素的 overflow/z-index 影响 -->
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click.self="close">
        <div class="modal-card">
          <!-- 头部：标题 + 关闭按钮 -->
          <div class="modal-header">
            <h3 class="modal-title">登录</h3>
            <button class="modal-close" @click="close" aria-label="关闭">
              <IconX :size="18" />
            </button>
          </div>

          <!-- 表单 -->
          <form class="modal-form" @submit.prevent="handleLogin">
            <!-- 错误提示 -->
            <p v-if="errorMsg" class="modal-error">{{ errorMsg }}</p>

            <label class="modal-label">
              <span>用户名</span>
              <input
                v-model="username"
                class="modal-input"
                type="text"
                placeholder="请输入用户名"
                autocomplete="username"
                :disabled="loading"
              />
            </label>

            <label class="modal-label">
              <span>密码</span>
              <input
                v-model="password"
                class="modal-input"
                type="password"
                placeholder="请输入密码"
                autocomplete="current-password"
                :disabled="loading"
              />
            </label>

            <button
              type="submit"
              class="modal-submit"
              :disabled="loading || !username || !password"
            >
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </form>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{ 'update:visible': [value: boolean] }>()

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref<string | null>(null)

function close() {
  emit('update:visible', false)
}

/** 提交登录：调用后端 API，成功则关闭弹窗并清空表单 */
async function handleLogin() {
  if (loading.value || !username.value || !password.value) return

  loading.value = true
  errorMsg.value = null

  try {
    const { login } = useAuth()
    await login({
      username: username.value,
      password: password.value,
    })
    // 登录成功，关闭弹窗并重置表单
    username.value = ''
    password.value = ''
    emit('update:visible', false)
  } catch (e: any) {
    errorMsg.value = e?.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}

// Esc 键关闭
function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && props.visible) close()
}
onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))
</script>

<style scoped>
/* 遮罩层：半透明黑色背景，居中卡片 */
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

/* 卡片：12px 圆角，深色背景 */
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
  color: var(--ink);
}

/* 关闭按钮：36×36，圆形，hover 变亮 */
.modal-close {
  width: 36px; height: 36px;
  border-radius: var(--radius-full);
  border: none;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition-hover);
}
.modal-close:hover { background: var(--surface-elevated); color: var(--ink); }

/* 表单 */
.modal-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

/* 错误消息：红色 13px */
.modal-error {
  font-size: var(--text-caption);
  color: var(--error, #ef4444);
  padding: var(--space-xs) var(--space-sm);
  background: var(--surface-soft, rgba(239, 68, 68, 0.1));
  border-radius: var(--radius-sm);
  margin: 0;
}

/* 标签 + 输入框 */
.modal-label {
  display: flex;
  flex-direction: column;
  gap: var(--space-xxs);
}

.modal-label span {
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  color: var(--body);
}

.modal-input {
  height: 40px;
  padding: 0 var(--space-sm);
  border-radius: var(--radius-md);
  border: 1px solid var(--hairline);
  background: var(--canvas);
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  transition: var(--transition-hover);
}
.modal-input::placeholder { color: var(--muted-soft); }
.modal-input:focus { outline: none; border-color: var(--primary); }
.modal-input:disabled { opacity: 0.5; }

.modal-submit {
  margin-top: var(--space-xs);
  width: 100%;
  height: 40px;
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
  line-height: 1;
  border-radius: var(--radius-md);
  border: none;
  cursor: pointer;
  background: var(--primary);
  color: #0a0a0a;
  transition: var(--transition-hover);
}
.modal-submit:hover:not(:disabled) { background: var(--primary-active); }
.modal-submit:disabled { opacity: 0.5; cursor: not-allowed; }

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
