<!--
  CodeBlock.vue — 代码块展示组件
  用于独立使用的代码展示（非 markdown 渲染场景）
  markdown 中的代码块由 ArticleBody 的 markdown-it 直接渲染为 HTML

  功能：
  - 语言标签头（12px/600/全大写）
  - 复制代码按钮（navigator.clipboard API）
  - 横向滚动（overflow-x: auto）
  - 语法高亮类名（.kw .str .fn .cm .num .type）
-->
<template>
  <div class="code-block">
    <!-- 头部：语言标签 + 复制按钮 -->
    <div class="code-block-header">
      <span class="code-lang">{{ language }}</span>
      <button class="code-copy" @click="copyCode">
        {{ copied ? '已复制' : '复制代码' }}
      </button>
    </div>
    <!-- 代码内容 -->
    <pre class="code-pre"><code v-text="code" /></pre>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  code: string
  language: string
}>()

// 复制状态：2 秒后自动恢复
const copied = ref(false)
let timer: ReturnType<typeof setTimeout> | null = null

async function copyCode() {
  try {
    await navigator.clipboard.writeText(props.code)  // 剪贴板 API
    copied.value = true
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => { copied.value = false }, 2000)
  } catch {
    // clipboard API 不可用时静默失败（如 HTTP 环境下）
  }
}
</script>

<style scoped>
.code-block {
  border-radius: var(--radius-lg);        /* 12px */
  border: 1px solid var(--hairline);
  background: var(--surface-card);
  overflow: hidden;                       /* 裁剪圆角 */
  margin: var(--space-lg) 0;
}
.code-block-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-sm) var(--space-md);
  border-bottom: 1px solid var(--hairline);
}

/* 语言标签：12px/600/全大写/加宽字间距 */
.code-lang {
  font-size: var(--text-uppercase);
  font-weight: var(--weight-semibold);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;
  color: var(--muted);
}
.code-copy {
  font-family: var(--font-sans);
  font-size: var(--text-uppercase);
  color: var(--muted);
  background: transparent;
  border: none;
  cursor: pointer;
  padding: var(--space-xxs) var(--space-xs);
  border-radius: var(--radius-sm);
  transition: var(--transition-hover);
}
.code-copy:hover { color: var(--primary); }

/* 代码内容区 */
.code-pre {
  padding: 20px;
  color: var(--body);
  font-size: var(--text-code);            /* 14px */
  line-height: var(--leading-relaxed);
  tab-size: 2;                            /* Tab 缩进宽度 */
  overflow-x: auto;                       /* 超长代码横向滚动 */
  margin: 0;
}
.code-pre code { font-family: var(--font-mono); }

/* 语法高亮：通过 :deep() 穿透 scoped 样式 */
:deep(.kw) { color: var(--accent-blue); }          /* 关键字 */
:deep(.str) { color: var(--primary); }             /* 字符串（黄色） */
:deep(.fn) { color: var(--accent-emerald); }       /* 函数名（绿色） */
:deep(.cm) { color: var(--muted-soft); }           /* 注释（灰色） */
:deep(.num) { color: var(--warning); }             /* 数字（琥珀色） */
:deep(.type) { color: var(--accent-emerald); }     /* 类型（绿色） */
</style>
