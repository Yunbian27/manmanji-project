<template>
  <div class="ai-assistant">
    <!-- Floating trigger button -->
    <button
      class="ai-trigger"
      :class="{ active: isOpen }"
      @click="isOpen = !isOpen"
      aria-label="AI 助手"
    >
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M12 2a4 4 0 0 1 4 4v2a4 4 0 0 1-8 0V6a4 4 0 0 1 4-4z" />
        <path d="M16 14H8a6 6 0 0 0-6 6v1h20v-1a6 6 0 0 0-6-6z" />
        <circle cx="12" cy="11" r="1" fill="currentColor" />
      </svg>
    </button>

    <!-- Chat panel -->
    <Transition name="ai-panel">
      <div v-if="isOpen" class="ai-panel">
        <div class="ai-panel-header">
          <span class="ai-panel-title">AI 助手</span>
          <button class="ai-close" @click="isOpen = false" aria-label="关闭">
            <IconX :size="16" />
          </button>
        </div>

        <div class="ai-messages" ref="msgContainer">
          <div v-if="messages.length === 0" class="ai-empty">
            <p>你好！我是慢慢记的 AI 助手</p>
            <p class="ai-empty-hint">有什么我可以帮你的吗？</p>
          </div>
          <div
            v-for="(msg, i) in messages"
            :key="i"
            class="ai-msg"
            :class="msg.role === 'user' ? 'ai-msg--user' : 'ai-msg--bot'"
          >
            {{ msg.content }}
          </div>
          <div v-if="sending" class="ai-msg ai-msg--bot ai-typing">
            <span class="dot" /><span class="dot" /><span class="dot" />
          </div>
        </div>

        <form class="ai-input-row" @submit.prevent="sendMsg">
          <input
            v-model="input"
            type="text"
            class="ai-input"
            placeholder="输入你的问题..."
            :disabled="sending"
          />
          <button type="submit" class="ai-send-btn" :disabled="!input.trim() || sending">
            <IconSend :size="16" />
          </button>
        </form>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
interface ChatMessage {
  role: 'user' | 'bot'
  content: string
}

const isOpen = ref(false)
const input = ref('')
const messages = ref<ChatMessage[]>([])
const sending = ref(false)
const msgContainer = ref<HTMLElement | null>()

function sendMsg() {
  const text = input.value.trim()
  if (!text || sending.value) return

  messages.value.push({ role: 'user', content: text })
  input.value = ''
  sending.value = true

  // Simulate AI response
  setTimeout(() => {
    messages.value.push({
      role: 'bot',
      content: '这是一个模拟的 AI 回复。在实际部署中，这里会连接到后端的 LLM 服务。',
    })
    sending.value = false
    nextTick(() => {
      if (msgContainer.value) {
        msgContainer.value.scrollTop = msgContainer.value.scrollHeight
      }
    })
  }, 1000)

  nextTick(() => {
    if (msgContainer.value) {
      msgContainer.value.scrollTop = msgContainer.value.scrollHeight
    }
  })
}

watch(isOpen, (val) => {
  if (val) {
    nextTick(() => {
      if (msgContainer.value) {
        msgContainer.value.scrollTop = msgContainer.value.scrollHeight
      }
    })
  }
})
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  bottom: var(--space-xl);
  right: var(--space-xl);
  z-index: var(--z-ai-button);
}

.ai-trigger {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-full);
  border: none;
  background: var(--primary);
  color: #0a0a0a;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-ai);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.ai-trigger:hover {
  transform: scale(1.06);
  box-shadow: var(--shadow-ai-hover);
}

.ai-panel {
  position: fixed;
  bottom: calc(var(--space-xl) + 56px);
  right: var(--space-xl);
  width: 400px;
  max-height: 560px;
  border-radius: var(--radius-lg);
  background: var(--surface-card);
  border: 1px solid var(--hairline);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: var(--z-ai-panel);
}

.ai-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-sm) var(--space-md);
  border-bottom: 1px solid var(--hairline);
  flex-shrink: 0;
}
.ai-panel-title {
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
  color: var(--ink);
}
.ai-close {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  border: none;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ai-close:hover { background: var(--surface-elevated); color: var(--ink); }

.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-md);
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  max-height: 400px;
}

.ai-empty { text-align: center; color: var(--muted); margin-top: var(--space-lg); }
.ai-empty-hint { font-size: var(--text-caption); color: var(--muted-soft); margin-top: var(--space-xxs); }

.ai-msg {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  font-size: var(--text-body-sm);
  line-height: var(--leading-relaxed);
  word-break: break-word;
}

.ai-msg--user {
  align-self: flex-end;
  background: var(--primary);
  color: #0a0a0a;
  border-bottom-right-radius: var(--radius-xs);
}

.ai-msg--bot {
  align-self: flex-start;
  background: var(--surface-elevated);
  color: var(--body);
  border-bottom-left-radius: var(--radius-xs);
}

.ai-typing { display: flex; gap: 4px; padding: 14px; }
.ai-typing .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--muted-soft);
  animation: typing 1.4s infinite ease-in-out both;
}
.ai-typing .dot:nth-child(1) { animation-delay: 0s; }
.ai-typing .dot:nth-child(2) { animation-delay: 0.16s; }
.ai-typing .dot:nth-child(3) { animation-delay: 0.32s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

.ai-input-row {
  display: flex;
  gap: var(--space-xs);
  padding: var(--space-sm) var(--space-md);
  border-top: 1px solid var(--hairline);
  flex-shrink: 0;
}
.ai-input {
  flex: 1;
  height: 40px;
  padding: 0 14px;
  border-radius: var(--radius-md);
  border: 1px solid var(--hairline);
  background: var(--surface-card);
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
}
.ai-input:focus { outline: none; border-color: var(--primary); }
.ai-input::placeholder { color: var(--muted-soft); }
.ai-send-btn {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  border: none;
  background: var(--primary);
  color: #0a0a0a;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: var(--transition-hover);
}
.ai-send-btn:hover:not(:disabled) { background: var(--primary-active); }
.ai-send-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* Panel transitions */
.ai-panel-enter-active { transition: all 0.2s ease; }
.ai-panel-leave-active { transition: all 0.15s ease; }
.ai-panel-enter-from { opacity: 0; transform: translateY(12px) scale(0.96); }
.ai-panel-leave-to { opacity: 0; transform: translateY(8px) scale(0.98); }

@media (max-width: 480px) {
  .ai-panel { width: calc(100vw - 32px); right: 16px; }
}
</style>
