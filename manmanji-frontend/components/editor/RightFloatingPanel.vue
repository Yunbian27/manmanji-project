<template>
  <div class="right-floating">
    <div class="right-actions">
      <button
        v-for="action in actions"
        :key="action.id"
        :class="['action-trigger', { active: activePanel === action.id }]"
        :title="action.label"
        @click="togglePanel(action.id)"
      >
        <span class="trigger-icon">{{ action.icon }}</span>
      </button>
    </div>

    <Transition name="panel-slide">
      <div v-if="activePanel" class="slide-panel">
        <div class="panel-header">
          <span class="panel-title">{{ activePanelData?.label }}</span>
          <button class="panel-close" @click="activePanel = null">
            <IconLucideX class="icon-md" />
          </button>
        </div>
        <div class="panel-body">
          <div v-if="activePanel === 'toc'" class="toc-list">
            <div v-if="!tocItems || tocItems.length === 0" class="panel-empty">暂无大纲</div>
            <a
              v-for="(item, i) in tocItems"
              :key="item.id"
              :class="['toc-item', `toc-level-${item.level}`]"
              @click.prevent="emit('navigateToHeading', i)"
            >
              {{ item.text }}
            </a>
          </div>
          <div v-else-if="activePanel === 'ai'" class="panel-placeholder">
            <div class="placeholder-icon">🤖</div>
            <p class="placeholder-title">AI 助手</p>
            <p class="placeholder-desc">智能写作辅助功能开发中，敬请期待...</p>
          </div>
          <div v-else-if="activePanel === 'help'" class="syntax-help">
            <div class="help-section">
              <h4 class="help-heading">标题</h4>
              <code class="help-code"># H1 &nbsp; ## H2 &nbsp; ### H3 &nbsp; #### H4 &nbsp; ##### H5 &nbsp; ###### H6</code>
            </div>
            <div class="help-section">
              <h4 class="help-heading">强调</h4>
              <code class="help-code">**粗体** &nbsp; *斜体* &nbsp; ~~删除线~~ &nbsp; ==高亮==</code>
            </div>
            <div class="help-section">
              <h4 class="help-heading">链接与图片</h4>
              <code class="help-code">[文字](url) &nbsp; ![图片](url)</code>
            </div>
            <div class="help-section">
              <h4 class="help-heading">代码</h4>
              <code class="help-code">`行内代码` &nbsp; ```语言 代码块```</code>
            </div>
            <div class="help-section">
              <h4 class="help-heading">列表</h4>
              <code class="help-code">- 无序列表 &nbsp; 1. 有序列表 &nbsp; - [ ] 待办 &nbsp; - [x] 已完成</code>
            </div>
            <div class="help-section">
              <h4 class="help-heading">引用</h4>
              <code class="help-code">&gt; 引用内容</code>
            </div>
            <div class="help-section">
              <h4 class="help-heading">表格</h4>
              <code class="help-code">| 列1 | 列2 |<br/>| --- | --- |<br/>| 内容 | 内容 |</code>
            </div>
            <div class="help-section">
              <h4 class="help-heading">分割线</h4>
              <code class="help-code">--- 或 *** 或 ___</code>
            </div>
            <div class="help-section">
              <h4 class="help-heading">数学公式（KaTeX）</h4>
              <code class="help-code">$行内公式$ &nbsp; $$块级公式$$</code>
            </div>
            <div class="help-section">
              <h4 class="help-heading">图表（Mermaid）</h4>
              <code class="help-code">```mermaid<br/>graph TD<br/>  A--&gt;B<br/>```</code>
            </div>
            <div class="help-section">
              <h4 class="help-heading">脚注</h4>
              <code class="help-code">正文内容[^1]<br/><br/>[^1]: 脚注说明</code>
            </div>
            <div class="help-section">
              <h4 class="help-heading">表情符号</h4>
              <code class="help-code">:smile: :rocket: :+1: :tada:</code>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="fade">
      <div v-if="activePanel" class="panel-backdrop" @click="activePanel = null" />
    </Transition>
  </div>
</template>

<script setup lang="ts">
import type { TocItem } from '~/types'

defineProps<{ tocItems?: TocItem[] }>()

const activePanel = ref<string | null>(null)

import IconLucideX from '~icons/lucide/x'
const emit = defineEmits<{ navigateToHeading: [index: number] }>()

const actions = [
  { id: 'ai', label: 'AI 助手', icon: 'AI' },
  { id: 'toc', label: '大纲', icon: '☰' },
  { id: 'help', label: '语法帮助', icon: '?' },
]

const activePanelData = computed(() => actions.find(a => a.id === activePanel.value))

function togglePanel(id: string) {
  activePanel.value = activePanel.value === id ? null : id
}
</script>

<style scoped>
.right-actions {
  position: absolute;
  right: 8px;
  top: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 6;
}

.action-trigger {
  width: 40px;
  height: 40px;
  border-radius: var(--rounded-full);
  border: 1px solid var(--hairline);
  background: var(--canvas);
  color: var(--steel);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s var(--ease);
}

.trigger-icon {
  font-size: 13px;
  font-weight: var(--weight-semibold);
  font-family: var(--font-sans);
}

.action-trigger:hover,
.action-trigger.active {
  background: var(--surface);
  color: var(--ink);
  border-color: var(--hairline-strong);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.slide-panel {
  position: absolute;
  top: var(--spacing-lg);
  bottom: var(--spacing-lg);
  right: 56px;
  width: var(--panel-px, var(--panel-width, 280px));
  background: var(--canvas);
  border-radius: var(--rounded-lg);
  box-shadow: rgba(15, 15, 15, 0.08) 0px 4px 12px 0px;
  z-index: 4;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.25s var(--ease);
  will-change: width;
  transform: translateZ(0);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--hairline);
  flex-shrink: 0;
}

.panel-title {
  font-size: var(--body-sm);
  font-weight: var(--weight-semibold);
  color: var(--ink);
}

.panel-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--rounded-sm);
  background: transparent;
  color: var(--steel);
  cursor: pointer;
  transition: background 0.12s var(--ease);
}

.panel-close:hover {
  background: var(--hairline-soft);
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-md);
}

.panel-empty {
  color: var(--muted);
  font-size: var(--body-sm);
  text-align: center;
  padding: var(--spacing-xl) 0;
}

.panel-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xxl) var(--spacing-md);
  text-align: center;
}

.placeholder-icon {
  font-size: 40px;
  margin-bottom: var(--spacing-md);
}

.placeholder-title {
  font-size: var(--body-md);
  font-weight: var(--weight-semibold);
  color: var(--ink);
  margin: 0 0 var(--spacing-xs);
}

.placeholder-desc {
  font-size: var(--body-sm);
  color: var(--muted);
  margin: 0;
  line-height: var(--leading-body);
}

/* TOC */
.toc-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.toc-item {
  display: block;
  padding: 6px 8px;
  border-radius: var(--rounded-sm);
  text-decoration: none;
  line-height: var(--leading-sm);
  transition: background 0.12s var(--ease), color 0.12s var(--ease);

  /* truncate */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toc-item:hover {
  background: var(--hairline-soft);
}

.toc-level-1 {
  padding-left: 0;
  font-size: 15px;
  font-weight: var(--weight-semibold);
  color: var(--ink);
}

.toc-level-2 {
  padding-left: 8px;
  font-size: var(--body-sm);
  font-weight: var(--weight-medium);
  color: var(--ink);
}

.toc-level-3 {
  padding-left: 18px;
  font-size: var(--caption);
  font-weight: var(--weight-medium);
  color: var(--charcoal);
}

.toc-level-4 {
  padding-left: 28px;
  font-size: var(--caption);
  font-weight: var(--weight-regular);
  color: var(--slate);
}

.toc-level-5 {
  padding-left: 38px;
  font-size: 12px;
  font-weight: var(--weight-regular);
  color: var(--steel);
}

.toc-level-6 {
  padding-left: 48px;
  font-size: 12px;
  font-weight: var(--weight-regular);
  color: var(--muted);
}

/* Syntax help */
.syntax-help {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.help-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.help-heading {
  font-size: var(--caption);
  font-weight: var(--weight-semibold);
  color: var(--ink);
  margin: 0;
}

.help-code {
  display: block;
  padding: 6px 8px;
  background: var(--surface);
  border-radius: var(--rounded-xs);
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--slate);
  line-height: 1.5;
  word-break: break-all;
}
</style>

.panel-backdrop {
  display: none;
}

/* Transitions */
.panel-slide-enter-active,
.panel-slide-leave-active {
  transition: opacity 0.2s var(--ease);
}

.panel-slide-enter-from,
.panel-slide-leave-to {
  opacity: 0;
}

