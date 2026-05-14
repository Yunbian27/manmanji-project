<template>
  <div class="code-block">
    <div class="code-block-header">
      <span class="code-lang">{{ language }}</span>
      <button class="code-copy" @click="copyCode">
        {{ copied ? '已复制' : '复制代码' }}
      </button>
    </div>
    <pre class="code-pre"><code ref="codeRef" v-text="code" /></pre>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  code: string
  language: string
}>()

const copied = ref(false)
let timer: ReturnType<typeof setTimeout> | null = null

async function copyCode() {
  try {
    await navigator.clipboard.writeText(code)
    copied.value = true
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => { copied.value = false }, 2000)
  } catch {
    // clipboard API not available
  }
}
</script>

<style scoped>
.code-block {
  border-radius: var(--radius-lg);
  border: 1px solid var(--hairline);
  background: var(--surface-card);
  overflow: hidden;
  margin: var(--space-lg) 0;
}

.code-block-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-sm) var(--space-md);
  border-bottom: 1px solid var(--hairline);
}

.code-lang {
  font-size: var(--text-uppercase);
  font-weight: var(--weight-semibold);
  line-height: var(--leading-normal);
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

.code-pre {
  padding: 20px;
  color: var(--body);
  font-size: var(--text-code);
  line-height: var(--leading-relaxed);
  tab-size: 2;
  overflow-x: auto;
  margin: 0;
}

.code-pre code { font-family: var(--font-mono); }

/* Syntax tokens */
:deep(.kw) { color: var(--accent-blue); }
:deep(.str) { color: var(--primary); }
:deep(.fn) { color: var(--accent-emerald); }
:deep(.cm) { color: var(--muted-soft); }
:deep(.num) { color: var(--warning); }
:deep(.type) { color: var(--accent-emerald); }
</style>
