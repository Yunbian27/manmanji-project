<!--
  EditorNav.vue — 编辑器左侧功能区
  - 视图切换
  - Markdown 快捷插入
  - 发布设置 / 保存
-->
<template>
  <aside class="editor-sidebar">
    <!-- 视图切换 -->
    <div class="sidebar-section">
      <span class="section-label">视图</span>
      <div class="view-mode-btns">
        <button :class="['view-btn', { active: viewMode === 'edit' }]" @click="viewMode = 'edit'">编辑</button>
        <button :class="['view-btn', { active: viewMode === 'split' }]" @click="viewMode = 'split'">双栏</button>
        <button :class="['view-btn', { active: viewMode === 'preview' }]" @click="viewMode = 'preview'">预览</button>
      </div>
    </div>

    <!-- Markdown 快捷插入 -->
    <div class="sidebar-section">
      <span class="section-label">快捷插入</span>
      <div class="md-grid">
        <button
          v-for="btn in mdButtons"
          :key="btn.label"
          class="md-btn"
          :title="btn.label"
          @click="emit('insertMarkdown', btn.before, btn.after, btn.placeholder)"
        >
          {{ btn.icon }}
          <span class="md-btn-label">{{ btn.label }}</span>
        </button>
      </div>
    </div>

    <!-- 插入图片 -->
    <div class="sidebar-section">
      <span class="section-label">插入图片</span>
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        hidden
        @change="onFileSelected"
      />
      <button
        class="action-btn secondary"
        :disabled="uploading"
        @click="fileInput?.click()"
      >
        {{ uploading ? '上传中...' : '选择图片' }}
      </button>
    </div>

    <!-- 操作 -->
    <div class="sidebar-section sidebar-actions">
      <button class="action-btn secondary" :disabled="isSaving" @click="$emit('openPublishSettings')">
        发布设置
      </button>
      <button class="action-btn primary" :disabled="isSaving" @click="handleSave">
        {{ isSaving ? '保存中...' : '保存' }}
      </button>
      <span v-if="lastSavedAt" class="save-status">已保存 {{ lastSavedAt }}</span>
    </div>
  </aside>
</template>

<script setup lang="ts">
const emit = defineEmits<{
  insertMarkdown: [before: string, after: string, placeholder: string]
  openPublishSettings: []
  close: []
}>()

const editor = injectEditor()
const { viewMode, isSaving, lastSavedAt } = editor

const mdButtons = [
  { icon: 'B', label: '粗体', before: '**', after: '**', placeholder: '粗体文字' },
  { icon: 'I', label: '斜体', before: '*', after: '*', placeholder: '斜体文字' },
  { icon: 'H', label: '标题', before: '\n## ', after: '', placeholder: '标题' },
  { icon: '🔗', label: '链接', before: '[', after: '](url)', placeholder: '链接文字' },
  { icon: '<>', label: '行内代码', before: '`', after: '`', placeholder: '代码' },
  { icon: '{}', label: '代码块', before: '\n```\n', after: '\n```\n', placeholder: '语言\n代码' },
  { icon: '❝', label: '引用', before: '\n> ', after: '', placeholder: '引用内容' },
  { icon: '•', label: '无序列表', before: '\n- ', after: '', placeholder: '列表项' },
  { icon: '1.', label: '有序列表', before: '\n1. ', after: '', placeholder: '列表项' },
  { icon: '—', label: '分割线', before: '\n---\n', after: '', placeholder: '' },
  { icon: '⊞', label: '表格', before: '\n| 列1 | 列2 |\n| --- | --- |\n| ', after: ' | ', placeholder: '内容' },
]

async function handleSave() {
  await editor.save()
}

const fileInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)

async function onFileSelected(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  uploading.value = true
  try {
    const { uploadImage } = useArticle()
    const url = await uploadImage(file)
    emit('insertMarkdown', '![', `](${url})`, '图片描述')
  } catch (err) {
    alert(err instanceof Error ? err.message : '上传失败')
  } finally {
    uploading.value = false
    input.value = ''
  }
}
</script>

<style scoped>
.editor-sidebar {
  width: 220px;
  flex-shrink: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  background: var(--canvas);
  border-right: 1px solid var(--hairline);
}

.sidebar-section {
  padding: var(--space-md) var(--space-sm);
  border-bottom: 1px solid var(--hairline);
}

.section-label {
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  color: var(--muted);
  margin-bottom: var(--space-sm);
  display: block;
  padding: 0 var(--space-xs);
}

/* Markdown 按钮网格 */
.md-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 2px;
}

.md-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  padding: var(--space-xxs);
  height: 44px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--muted);
  font-family: var(--font-sans);
  font-size: 13px;
  cursor: pointer;
  transition: var(--transition-hover);
}
.md-btn:hover { background: var(--canvas-soft); color: var(--ink); }
.md-btn-label {
  font-size: var(--text-caption);
  color: var(--muted-soft);
}

/* 视图切换 */
.view-mode-btns {
  display: flex;
  border-radius: var(--radius-pill);
  border: 1px solid var(--hairline);
  overflow: hidden;
}
.view-btn {
  flex: 1;
  font-family: var(--font-sans);
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  color: var(--muted);
  background: transparent;
  border: none;
  padding: var(--space-xxs) 0;
  cursor: pointer;
  transition: var(--transition-hover);
}
.view-btn:not(:last-child) { border-right: 1px solid var(--hairline); }
.view-btn:hover { color: var(--body); }
.view-btn.active { background: var(--canvas-soft); color: var(--ink); }

/* 操作按钮 — DESIGN.md button-primary / button-secondary */
.sidebar-actions {
  margin-top: auto;
  border-bottom: none;
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.action-btn {
  width: 100%;
  height: 40px;
  padding: 0 var(--space-md);
  font-family: var(--font-sans);
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
  line-height: 1;
  border-radius: var(--radius-pill);
  cursor: pointer;
  transition: var(--transition-hover);
}
.action-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.action-btn.primary {
  background: var(--primary);
  color: var(--on-primary);
}
.action-btn.primary:hover:not(:disabled) { background: var(--primary-active); }

.action-btn.secondary {
  background: var(--canvas);
  color: var(--ink);
  border: 1px solid var(--hairline);
}
.action-btn.secondary:hover:not(:disabled) { background: var(--canvas-soft); }

.save-status {
  font-size: var(--text-caption);
  color: var(--muted-soft);
  text-align: center;
}
</style>
