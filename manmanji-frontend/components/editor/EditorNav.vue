<template>
  <header class="editor-nav">
    <div class="editor-nav-left">
      <button class="editor-back-btn" @click="handleBack">
        <IconChevronRight :size="18" class="back-icon" />
        <span>返回列表</span>
      </button>
    </div>

    <div class="editor-nav-center">
      <input
        v-model="title"
        class="editor-title-input"
        :class="{ 'has-error': titleError }"
        placeholder="输入文章标题..."
        type="text"
        maxlength="200"
        @keydown.enter.prevent
      />
      <span v-if="titleError" class="title-error-msg">{{ titleError }}</span>
    </div>

    <div class="editor-nav-right">
      <span v-if="lastSavedAt" class="save-status">
        已保存 {{ lastSavedAt }}
      </span>

      <div class="view-mode-tabs">
        <button
          :class="['mode-btn', { active: viewMode === 'edit' }]"
          @click="viewMode = 'edit'"
          title="仅编辑"
        >
          编辑
        </button>
        <button
          :class="['mode-btn', { active: viewMode === 'split' }]"
          @click="viewMode = 'split'"
          title="双栏对照"
        >
          双栏
        </button>
        <button
          :class="['mode-btn', { active: viewMode === 'preview' }]"
          @click="viewMode = 'preview'"
          title="仅预览"
        >
          预览
        </button>
      </div>

      <AppButton variant="secondary" :disabled="isSaving" @click="handleSaveDraft">
        {{ isSaving ? '保存中...' : '保存草稿' }}
      </AppButton>
      <AppButton variant="text" @click="publishSettingsOpen = !publishSettingsOpen">
        发布设置
      </AppButton>
      <AppButton variant="primary" :disabled="isSaving" @click="handlePublish">
        {{ isSaving ? '发布中...' : '发布' }}
      </AppButton>
    </div>
  </header>
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
  height: var(--nav-height);
  display: flex;
  align-items: center;
  padding: 0 var(--space-lg);
  background: var(--canvas);
  border-bottom: 1px solid var(--hairline);
  gap: var(--space-md);
  flex-shrink: 0;
}

.editor-nav-left {
  flex-shrink: 0;
}

.editor-back-btn {
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
  padding: var(--space-xxs) var(--space-xs);
  border-radius: var(--radius-md);
  transition: color 0.15s ease;
  white-space: nowrap;
}
.editor-back-btn:hover { color: var(--body); }
.back-icon { transform: rotate(180deg); flex-shrink: 0; }

.editor-nav-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.editor-title-input {
  width: 100%;
  max-width: 600px;
  background: transparent;
  border: none;
  color: var(--ink);
  font-size: var(--text-title-lg);
  font-weight: var(--weight-bold);
  font-family: var(--font-sans);
  padding: var(--space-xs) 0;
  outline: none;
  border-bottom: 2px solid transparent;
  transition: border-color 0.15s ease;
}
.editor-title-input:focus { border-bottom-color: var(--hairline-strong); }
.editor-title-input.has-error { border-bottom-color: var(--error); }
.editor-title-input::placeholder { color: var(--muted-soft); }

.title-error-msg {
  font-size: var(--text-caption);
  color: var(--error);
  margin-top: 2px;
}

.editor-nav-right {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  flex-shrink: 0;
}

.save-status {
  font-size: var(--text-caption);
  color: var(--muted-soft);
  margin-right: var(--space-xs);
  white-space: nowrap;
}

.view-mode-tabs {
  display: flex;
  border-radius: var(--radius-md);
  background: var(--surface-card);
  border: 1px solid var(--hairline);
  overflow: hidden;
}

.mode-btn {
  font-family: var(--font-sans);
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  color: var(--muted);
  background: transparent;
  border: none;
  padding: 6px 14px;
  cursor: pointer;
  transition: background-color 0.15s ease, color 0.15s ease;
}
.mode-btn:not(:last-child) { border-right: 1px solid var(--hairline); }
.mode-btn:hover { color: var(--body); }
.mode-btn.active {
  background: var(--primary);
  color: #0a0a0a;
}
</style>
