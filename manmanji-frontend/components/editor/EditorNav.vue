<!--
  EditorNav.vue — 编辑器左侧功能区（Notion DESIGN.md 规范）
  视图切换 | Markdown 快捷插入 | 图片上传 | 保存/发布
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
      <input ref="fileInput" type="file" accept="image/*" hidden @change="onFileSelected" />
      <button class="action-btn secondary" :disabled="uploading" @click="fileInput?.click()">
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
  padding: var(--spacing-md) var(--spacing-sm);
  border-bottom: 1px solid var(--hairline);
}

.section-label {
  font-size: var(--caption);
  font-weight: var(--weight-medium);
  color: var(--muted);
  margin-bottom: var(--spacing-xs);
  display: block;
  padding: 0 var(--spacing-xs);
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
  padding: 4px;
  height: 44px;
  border: none;
  border-radius: var(--rounded-md);
  background: transparent;
  color: var(--steel);
  font-family: var(--font-sans);
  font-size: 13px;
  cursor: pointer;
  transition: background-color 0.15s var(--ease), color 0.15s var(--ease);
}
.md-btn:hover { background: var(--hairline-soft); color: var(--ink); }
.md-btn-label {
  font-size: var(--caption);
  color: var(--muted);
}

/* 视图切换 */
.view-mode-btns {
  display: flex;
  border-radius: var(--rounded-md);
  border: 1px solid var(--hairline);
  overflow: hidden;
}
.view-btn {
  flex: 1;
  font-family: var(--font-sans);
  font-size: var(--caption);
  font-weight: var(--weight-medium);
  color: var(--steel);
  background: transparent;
  border: none;
  padding: 6px 0;
  cursor: pointer;
  transition: background-color 0.15s var(--ease), color 0.15s var(--ease);
}
.view-btn:not(:last-child) { border-right: 1px solid var(--hairline); }
.view-btn:hover { color: var(--ink); }
.view-btn.active { background: var(--hairline-soft); color: var(--ink); }

/* 操作按钮 */
.sidebar-actions {
  margin-top: auto;
  border-bottom: none;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.action-btn {
  width: 100%;
  height: 40px;
  padding: 0 var(--spacing-md);
  font-family: var(--font-sans);
  font-size: var(--button-md);
  font-weight: var(--weight-medium);
  line-height: var(--leading-button);
  border-radius: var(--rounded-md);
  cursor: pointer;
  transition: background-color 0.15s var(--ease);
}
.action-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.action-btn.primary {
  background: var(--primary);
  color: var(--on-primary);
  border: none;
}
.action-btn.primary:hover:not(:disabled) { background: var(--primary-pressed); }

.action-btn.secondary {
  background: var(--canvas);
  color: var(--ink);
  border: 1px solid var(--hairline-strong);
}
.action-btn.secondary:hover:not(:disabled) { background: var(--hairline-soft); }

.save-status {
  font-size: var(--caption);
  color: var(--muted);
  text-align: center;
}
</style>
