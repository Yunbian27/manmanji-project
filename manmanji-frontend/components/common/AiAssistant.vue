<!--
  AiAssistant.vue — AI 浮动助手
  右下角固定的悬浮按钮 + 可展开的对话面板

  记住：这是整个项目中唯一使用 box-shadow 的地方！
  设计规范明确禁止其他元素使用阴影。

  两种状态：
  - 关闭：右下角 48×48 黄色圆形按钮
  - 打开：按钮上方弹出 400×560px 对话面板

  消息类型：
  - user: 右对齐，黑色气泡 + 白色字
  - bot:  左对齐，浅灰气泡 + 黑色字
  - typing: 三个跳动的小圆点动画
-->
<template>
  <div class="ai-assistant">
    <!-- 触发按钮：48×48 圆形，黄色底 -->
    <button
      class="ai-trigger"
      :class="{ active: isOpen }"
      @click="isOpen = !isOpen"
      aria-label="AI 助手"
    >
      <!-- 简单机器人 SVG 图标 -->
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M12 2a4 4 0 0 1 4 4v2a4 4 0 0 1-8 0V6a4 4 0 0 1 4-4z" />
        <path d="M16 14H8a6 6 0 0 0-6 6v1h20v-1a6 6 0 0 0-6-6z" />
        <circle cx="12" cy="11" r="1" fill="currentColor" />
      </svg>
    </button>

    <!-- 对话面板：Transition 动画 -->
    <Transition name="ai-panel">
      <div v-if="isOpen" class="ai-panel">
        <!-- 面板头部 -->
        <div class="ai-panel-header">
          <span class="ai-panel-title">AI 助手</span>
          <button class="ai-close" @click="isOpen = false" aria-label="关闭">
            <IconX :size="16" />
          </button>
        </div>

        <!-- 消息列表：空状态 / 消息列表 / 打字动画 -->
        <div class="ai-messages" ref="msgContainer">
          <!-- 空状态：引导文案 -->
          <div v-if="messages.length === 0" class="ai-empty">
            <p>你好！我是慢慢记的 AI 助手</p>
            <p class="ai-empty-hint">有什么我可以帮你的吗？</p>
          </div>

          <!-- 聊天消息 -->
          <div
            v-for="(msg, i) in messages"
            :key="i"
            class="ai-msg"
            :class="msg.role === 'user' ? 'ai-msg--user' : 'ai-msg--bot'"
          >
            {{ msg.content }}
          </div>

          <!-- 打字中动画：三个小圆点 -->
          <div v-if="sending" class="ai-msg ai-msg--bot ai-typing">
            <span class="dot" /><span class="dot" /><span class="dot" />
          </div>
        </div>

        <!-- 输入区域 -->
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
// 消息类型定义
interface ChatMessage {
  role: 'user' | 'bot'
  content: string
}

const isOpen = ref(false)                    // 面板开关
const input = ref('')                        // 输入框内容（v-model 绑定）
const messages = ref<ChatMessage[]>([])      // 聊天记录
const sending = ref(false)                   // 是否正在等待回复
const msgContainer = ref<HTMLElement | null>() // 消息区域 DOM 引用（用于滚动到底部）

/** 发送消息（演示用模拟回复） */
function sendMsg() {
  const text = input.value.trim()
  if (!text || sending.value) return

  messages.value.push({ role: 'user', content: text })  // 添加用户消息
  input.value = ''
  sending.value = true

  // 模拟 AI 回复（实际应调用后端 SSE API）
  setTimeout(() => {
    messages.value.push({
      role: 'bot',
      content: '这是一个模拟的 AI 回复。在实际部署中，这里会连接到后端的 LLM 服务。',
    })
    sending.value = false
    scrollToBottom()
  }, 1000)

  scrollToBottom()
}

/** 新消息来时滚动到底部 */
function scrollToBottom() {
  // nextTick: 等 Vue DOM 更新完成后再执行
  nextTick(() => {
    if (msgContainer.value) {
      msgContainer.value.scrollTop = msgContainer.value.scrollHeight
    }
  })
}

// watch: 监听 isOpen 变化，打开时滚动到底部
watch(isOpen, (val) => {
  if (val) scrollToBottom()
})
</script>

<style scoped>
/* === 悬浮触发按钮 === */
.ai-assistant {
  position: fixed;
  bottom: var(--space-xl);                  /* 距底部 32px */
  right: var(--space-xl);                   /* 距右侧 32px */
  z-index: var(--z-ai-button);             /* 90 */
}
.ai-trigger {
  width: 48px; height: 48px;
  border-radius: var(--radius-pill);
  border: none;
  background: var(--primary);              /* 黑色底 */
  color: var(--on-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-3);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.ai-trigger:hover {
  transform: scale(1.06);
  box-shadow: var(--shadow-2);
}

/* === 对话面板 === */
.ai-panel {
  position: fixed;
  bottom: calc(var(--space-xl) + 56px);    /* 按钮上方 56px */
  right: var(--space-xl);
  width: 400px;
  max-height: 560px;
  border-radius: var(--radius-xl);         /* 16px */
  background: var(--canvas);
  border: 1px solid var(--hairline);
  box-shadow: var(--shadow-2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: var(--z-ai-panel);             /* 89 — 比按钮低一级 */
}
.ai-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-sm) var(--space-md);
  border-bottom: 1px solid var(--hairline);
  flex-shrink: 0;
}
.ai-panel-title { font-size: var(--text-body-sm); font-weight: var(--weight-medium); color: var(--ink); }
.ai-close {
  width: 28px; height: 28px;
  border-radius: var(--radius-pill);
  border: none;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ai-close:hover { background: var(--canvas-soft); color: var(--ink); }

/* === 消息列表 === */
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

/* === 聊天气泡 === */
.ai-msg {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  font-size: var(--text-body-sm);
  line-height: var(--leading-relaxed);
  word-break: break-word;
}
/* 用户消息：右对齐 + 黑色背景 + 白色字 */
.ai-msg--user {
  align-self: flex-end;
  background: var(--primary);
  color: var(--on-primary);
  border-bottom-right-radius: 4px;  /* 右下角收窄（气泡尾巴效果） */
}
/* 机器人消息：左对齐 + 浅灰背景 */
.ai-msg--bot {
  align-self: flex-start;
  background: var(--canvas-soft);
  color: var(--ink);
  border-bottom-left-radius: 4px;
}

/* === 打字指示器（三个跳动圆点） === */
.ai-typing { display: flex; gap: 4px; padding: 14px; }
.ai-typing .dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: var(--muted-soft);
  /* animation: 无限循环，每个圆点有不同延迟产生波浪效果 */
  animation: typing 1.4s infinite ease-in-out both;
}
.ai-typing .dot:nth-child(1) { animation-delay: 0s; }
.ai-typing .dot:nth-child(2) { animation-delay: 0.16s; }
.ai-typing .dot:nth-child(3) { animation-delay: 0.32s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* === 输入区 === */
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
  background: var(--canvas-soft);
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
}
.ai-input:focus { outline: none; border-color: var(--primary); }
.ai-input::placeholder { color: var(--muted-soft); }
.ai-send-btn {
  width: 40px; height: 40px;
  border-radius: var(--radius-pill);
  border: none;
  background: var(--primary);              /* 黑色发送按钮 */
  color: var(--on-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: var(--transition-hover);
}
.ai-send-btn:hover:not(:disabled) { background: var(--primary-active); }
.ai-send-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* === 面板过渡动画 === */
.ai-panel-enter-active { transition: all 0.2s ease; }
.ai-panel-leave-active { transition: all 0.15s ease; }
.ai-panel-enter-from { opacity: 0; transform: translateY(12px) scale(0.96); }
.ai-panel-leave-to { opacity: 0; transform: translateY(8px) scale(0.98); }

/* 手机端面板占满宽度 */
@media (max-width: 480px) {
  .ai-panel { width: calc(100vw - 32px); right: 16px; }
}
</style>
