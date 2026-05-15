<template>
  <aside class="editor-nav">
    <!-- 1. 返回按钮 -->
    <button class="nav-back-btn" @click="handleBack">
      <IconChevronRight :size="14" class="back-icon" />
      <span>返回列表</span>
    </button>

    <!-- 2. 标题输入框 -->
    <div class="nav-title-wrap">
      <input
        v-model="title"
        class="nav-title-input"
        :class="{ 'has-error': titleError }"
        placeholder="输入文章标题..."
        type="text"
        maxlength="200"
        @keydown.enter.prevent
      />
      <span v-if="titleError" class="title-error-msg">{{ titleError }}</span>
    </div>

    <!-- 3. 视图模式切换（竖排） -->
    <div class="nav-mode-group">
      <span class="nav-section-label">视图</span>
      <div class="nav-mode-btns">
        <button
          :class="['nav-mode-btn', { active: viewMode === 'edit' }]"
          @click="viewMode = 'edit'"
        >
          编辑
        </button>
        <button
          :class="['nav-mode-btn', { active: viewMode === 'split' }]"
          @click="viewMode = 'split'"
        >
          双栏
        </button>
        <button
          :class="['nav-mode-btn', { active: viewMode === 'preview' }]"
          @click="viewMode = 'preview'"
        >
          预览
        </button>
      </div>
    </div>

    <!-- 4. 操作按钮 -->
    <div class="nav-actions">
      <button
        class="nav-action-btn secondary"
        :disabled="isSaving"
        @click="handleSaveDraft"
      >
        {{ isSaving ? '保存中...' : '保存草稿' }}
      </button>
      <button
        class="nav-action-btn text"
        @click="publishSettingsOpen = !publishSettingsOpen"
      >
        发布设置
      </button>
      <button
        class="nav-action-btn primary"
        :disabled="isSaving"
        @click="handlePublish"
      >
        {{ isSaving ? '发布中...' : '发布' }}
      </button>
    </div>

    <!-- 5. 保存时间状态 -->
    <span v-if="lastSavedAt" class="nav-save-status">已保存 {{ lastSavedAt }}</span>
  </aside>
</template>

<script setup lang="ts">
const emit = defineEmits<{ close: [] }>()

const editor = injectEditor()
const {
  title, content, publishSettingsOpen, viewMode,
  isSaving, lastSavedAt, titleError,
} = editor

function handleBack() {
  if (content.value || title.value) {
    if (!confirm('有未保存的内容，确定返回吗？')) return
  }
  emit('close')
}

async function handleSaveDraft() {
  await editor.publish('DRAFT')
}

async function handlePublish() {
  const id = await editor.publish('PUBLISHED')
  if (id) emit('close')
}
</script>

<style scoped>
.editor-nav {
  width: 220px;
  flex-shrink: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
  background: var(--surface-soft);
  border-right: 1px solid var(--hairline);
  overflow-y: auto;
}

/* ——— 返回按钮 ——— */
.nav-back-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-xxs);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  color: var(--muted);
  background: transparent;
  border: none;
  cursor: pointer;
  padding: var(--space-xxs) 0;
  border-radius: var(--radius-md);
  transition: color 0.15s ease;
  white-space: nowrap;
  align-self: flex-start;
}
.nav-back-btn:hover { color: var(--body); }
.back-icon { transform: rotate(180deg); flex-shrink: 0; }

/* ——— 标题输入 ——— */
.nav-title-wrap {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-title-input {
  width: 100%;
  height: 40px;
  padding: 0 var(--space-sm);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  color: var(--ink);
  background: var(--surface-card);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-md);
  outline: none;
  transition: border-color 0.15s ease;
  box-sizing: border-box;
}
.nav-title-input::placeholder { color: var(--muted-soft); }
.nav-title-input:focus { border-color: var(--primary); }
.nav-title-input.has-error { border-color: var(--error); }

.title-error-msg {
  font-size: var(--text-uppercase);
  color: var(--error);
}

/* ——— 视图模式 ——— */
.nav-section-label {
  font-size: var(--text-uppercase);
  font-weight: var(--weight-semibold);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;
  color: var(--muted);
  margin-bottom: var(--space-xs);
  display: block;
}

.nav-mode-btns {
  display: flex;
  border-radius: var(--radius-md);
  border: 1px solid var(--hairline);
  overflow: hidden;
}

.nav-mode-btn {
  flex: 1;
  font-family: var(--font-sans);
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  color: var(--muted);
  background: transparent;
  border: none;
  padding: var(--space-xs) 0;
  cursor: pointer;
  transition: background-color 0.15s ease, color 0.15s ease;
  text-align: center;
}
.nav-mode-btn:not(:last-child) { border-right: 1px solid var(--hairline); }
.nav-mode-btn:hover { color: var(--body); }
.nav-mode-btn.active {
  background: var(--surface-card);
  color: var(--ink);
}

/* ——— 操作按钮 ——— */
.nav-actions {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  margin-top: auto;
}

.nav-action-btn {
  width: 100%;
  height: 40px;
  padding: 0 var(--space-lg);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
  line-height: 1;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background-color 0.15s ease, color 0.15s ease;
  white-space: nowrap;
}
.nav-action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.nav-action-btn.primary {
  background: var(--primary);
  color: var(--on-primary);
}
.nav-action-btn.primary:hover:not(:disabled) { background: var(--primary-active); }

.nav-action-btn.secondary {
  background: var(--surface-card);
  color: var(--ink);
}
.nav-action-btn.secondary:hover:not(:disabled) { background: var(--surface-elevated); }

.nav-action-btn.text {
  background: transparent;
  color: var(--body);
}
.nav-action-btn.text:hover:not(:disabled) { color: var(--ink); }

/* ——— 保存状态 ——— */
.nav-save-status {
  font-size: var(--text-uppercase);
  color: var(--muted-soft);
  text-align: center;
}
</style>
