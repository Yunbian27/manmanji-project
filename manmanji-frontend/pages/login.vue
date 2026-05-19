<template>
  <div class="login-page">
    <div class="login-card">
      <!-- Tab 切换 -->
      <div class="login-tabs">
        <button
          :class="{ active: mode === 'login' }"
          class="tab-btn"
          @click="mode = 'login'"
        >
          登录
        </button>
        <button
          :class="{ active: mode === 'register' }"
          class="tab-btn"
          @click="mode = 'register'"
        >
          注册
        </button>
      </div>

      <!-- 错误提示 -->
      <p v-if="errorMsg" class="login-error">{{ errorMsg }}</p>

      <!-- 表单 -->
      <form class="login-form" @submit.prevent="handleSubmit">
        <label class="login-label">
          <span>{{ mode === 'login' ? '用户名或邮箱' : '用户名' }}</span>
          <input
            v-model="account"
            class="login-input"
            type="text"
            :placeholder="mode === 'login' ? '请输入用户名或邮箱' : '请输入用户名'"
            autocomplete="username"
            :disabled="loading"
          />
        </label>

        <template v-if="mode === 'register'">
          <label class="login-label">
            <span>邮箱</span>
            <div class="code-row">
              <input
                v-model="email"
                class="login-input code-input"
                type="email"
                placeholder="请输入邮箱"
                autocomplete="email"
                :disabled="loading"
              />
              <button
                type="button"
                class="send-code-btn"
                :disabled="sendingCode || countdown > 0 || !email"
                @click="sendCode"
              >
                {{ countdown > 0 ? `${countdown}s` : sendingCode ? '发送中...' : '发送验证码' }}
              </button>
            </div>
          </label>

          <label class="login-label">
            <span>验证码</span>
            <input
              v-model="code"
              class="login-input"
              type="text"
              placeholder="请输入6位验证码"
              maxlength="6"
              autocomplete="off"
              :disabled="loading"
            />
          </label>
        </template>

        <label class="login-label">
          <span>密码</span>
          <input
            v-model="password"
            class="login-input"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            :disabled="loading"
          />
        </label>

        <template v-if="mode === 'register'">
          <label class="login-label">
            <span>确认密码</span>
            <input
              v-model="confirmPassword"
              class="login-input"
              type="password"
              placeholder="请再次输入密码"
              autocomplete="new-password"
              :disabled="loading"
            />
          </label>
        </template>

        <button
          type="submit"
          class="login-submit"
          :disabled="loading || !isFormValid"
        >
          {{ loading ? (mode === 'login' ? '登录中...' : '注册中...') : (mode === 'login' ? '登录' : '注册') }}
        </button>
      </form>

      <!-- 底部切换提示 -->
      <p class="login-toggle-hint">
        <template v-if="mode === 'login'">
          还没有账号？<a class="toggle-link" @click="switchMode('register')">去注册</a>
        </template>
        <template v-else>
          已有账号？<a class="toggle-link" @click="switchMode('login')">去登录</a>
        </template>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'blank' })

const route = useRoute()
const router = useRouter()

const mode = ref<'login' | 'register'>('login')
const account = ref('')
const password = ref('')
const email = ref('')
const code = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const errorMsg = ref<string | null>(null)

const redirectPath = computed(() => {
  const raw = route.query.redirect as string | undefined
  if (!raw || raw === '/login' || raw === '/') return '/home'
  return raw
})

// 已登录用户直接跳转
onMounted(() => {
  if (useAuthStore().isAuthenticated) {
    router.replace(redirectPath.value)
  }
})

const isFormValid = computed(() => {
  if (!account.value || !password.value) return false
  if (mode.value === 'register') {
    if (!email.value || !code.value || password.value.length < 6) return false
    if (password.value !== confirmPassword.value) return false
  }
  return true
})

function switchMode(newMode: 'login' | 'register') {
  mode.value = newMode
  errorMsg.value = null
  code.value = ''
  confirmPassword.value = ''
  countdown.value = 0
}

async function sendCode() {
  if (sendingCode.value || countdown.value > 0 || !email.value) return

  sendingCode.value = true
  errorMsg.value = null

  try {
    const api = useApi()
    await api.post('/api/auth/send-code', { email: email.value }, false)
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e: any) {
    errorMsg.value = e?.message || '发送失败，请重试'
  } finally {
    sendingCode.value = false
  }
}

async function handleSubmit() {
  if (loading.value || !isFormValid.value) return

  loading.value = true
  errorMsg.value = null

  try {
    const { login, register } = useAuth()
    if (mode.value === 'login') {
      await login({ account: account.value, password: password.value })
    } else {
      await register({
        username: account.value,
        email: email.value,
        password: password.value,
        code: code.value,
      })
    }
    router.push(redirectPath.value)
  } catch (e: any) {
    errorMsg.value = e?.message || '操作失败，请重试'
  } finally {
    loading.value = false
  }
}

// 切换模式时清空错误和确认密码
watch(mode, () => {
  errorMsg.value = null
  confirmPassword.value = ''
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--canvas);
  padding: var(--space-md);
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-xl);
  padding: var(--space-2xl);
  box-shadow: var(--shadow-2);
}

/* Tab 切换 — 遵循 category-tab 模式 */
.login-tabs {
  display: flex;
  gap: var(--space-xs);
  margin-bottom: var(--space-lg);
}

.tab-btn {
  flex: 1;
  padding: 8px 14px;
  border-radius: var(--radius-pill);
  border: none;
  background: transparent;
  color: var(--muted);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  line-height: var(--leading-normal);
  cursor: pointer;
  transition: var(--transition-hover);
}
.tab-btn.active {
  background: var(--canvas-soft);
  color: var(--ink);
}

/* 错误提示 */
.login-error {
  font-size: var(--text-caption);
  color: #c0392b;
  padding: var(--space-xs) var(--space-sm);
  background: #fde8e8;
  border-radius: var(--radius-md);
  margin: 0 0 var(--space-md);
}

/* 表单 — 遵循 text-input 规范 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.login-label {
  display: flex;
  flex-direction: column;
  gap: var(--space-xxs);
}

.login-label span {
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  color: var(--body);
}

.login-input {
  height: 40px;
  padding: 0 var(--space-sm);
  border-radius: var(--radius-md);
  border: 1px solid var(--hairline);
  background: var(--canvas-soft);
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  transition: var(--transition-hover);
}
.login-input::placeholder { color: var(--muted-soft); }
.login-input:focus { outline: none; border-color: var(--primary); }
.login-input:disabled { opacity: 0.5; }

/* 验证码行：邮箱 + 发送按钮 */
.code-row {
  display: flex;
  gap: var(--space-xs);
}
.code-input { flex: 1; }

.send-code-btn {
  flex-shrink: 0;
  height: 40px;
  padding: 0 var(--space-md);
  border-radius: var(--radius-pill);
  border: 1px solid var(--hairline);
  background: var(--canvas);
  color: var(--body);
  font-family: var(--font-sans);
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  white-space: nowrap;
  cursor: pointer;
  transition: var(--transition-hover);
}
.send-code-btn:hover:not(:disabled) { border-color: var(--primary); color: var(--primary); }
.send-code-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* 提交按钮 — 黑色胶囊 */
.login-submit {
  margin-top: var(--space-xs);
  width: 100%;
  height: 40px;
  border-radius: var(--radius-pill);
  border: none;
  background: var(--primary);
  color: var(--on-primary);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  line-height: 1;
  cursor: pointer;
  transition: var(--transition-hover);
}
.login-submit:hover:not(:disabled) { background: var(--primary-active); }
.login-submit:disabled { opacity: 0.5; cursor: not-allowed; }

/* 底部切换提示 */
.login-toggle-hint {
  margin: var(--space-lg) 0 0;
  text-align: center;
  font-size: var(--text-body-sm);
  color: var(--muted);
}
.toggle-link {
  color: var(--link);
  cursor: pointer;
  text-decoration: underline;
  transition: var(--transition-hover);
}
.toggle-link:hover { color: var(--ink); }
</style>
