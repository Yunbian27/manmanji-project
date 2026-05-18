<!--
  EditorNav.vue — 编辑器左侧功能区
  - Markdown 快捷插入
  - 发布设置（从 EditorPublishSettings 迁移）
  - 视图切换 + 保存草稿/发布
-->
<template>
  <aside class="editor-sidebar">
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

    <!-- 发布设置 -->
    <div class="sidebar-section">
      <span class="section-label">发布设置</span>
      <div class="settings-body">
        <label class="settings-field">
          <span class="field-label">分类</span>
          <select :value="publishSettings.categoryId" class="settings-select" @change="onCategoryChange">
            <option :value="null">未选择</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
          </select>
        </label>

        <label class="settings-field">
          <span class="field-label">标签</span>
          <div class="tag-input-area">
            <input
              v-model="tagInput"
              class="settings-input"
              placeholder="输入标签后按回车"
              @keydown.enter.prevent="addTag"
            />
            <div v-if="publishSettings.tagIds.length > 0" class="tag-list">
              <AppTag v-for="tagId in publishSettings.tagIds" :key="tagId" variant="default">
                {{ getTagName(tagId) }}
                <button class="tag-remove" @click="removeTag(tagId)"><IconX :size="10" /></button>
              </AppTag>
            </div>
          </div>
        </label>

        <label class="settings-field">
          <span class="field-label">封面图片 URL</span>
          <input v-model="publishSettings.coverUrl" class="settings-input" placeholder="https://..." />
        </label>

        <label class="settings-field">
          <span class="field-label">摘要</span>
          <textarea
            v-model="publishSettings.summary"
            class="settings-textarea"
            placeholder="文章摘要（最多 500 字）"
            maxlength="500"
            rows="3"
          />
        </label>

        <label class="settings-field settings-toggle-row">
          <span class="field-label">原创</span>
          <button
            type="button"
            :class="['toggle-btn', { on: publishSettings.isOriginal }]"
            @click="publishSettings.isOriginal = !publishSettings.isOriginal"
          >
            <span class="toggle-knob" />
          </button>
        </label>

        <label v-if="!publishSettings.isOriginal" class="settings-field">
          <span class="field-label">转载来源 URL</span>
          <input v-model="publishSettings.sourceUrl" class="settings-input" placeholder="https://..." />
        </label>
      </div>
    </div>

    <!-- 视图切换 -->
    <div class="sidebar-section">
      <span class="section-label">视图</span>
      <div class="view-mode-btns">
        <button :class="['view-btn', { active: viewMode === 'edit' }]" @click="viewMode = 'edit'">编辑</button>
        <button :class="['view-btn', { active: viewMode === 'split' }]" @click="viewMode = 'split'">双栏</button>
        <button :class="['view-btn', { active: viewMode === 'preview' }]" @click="viewMode = 'preview'">预览</button>
      </div>
    </div>

    <!-- 操作 -->
    <div class="sidebar-section sidebar-actions">
      <button class="action-btn secondary" :disabled="isSaving" @click="handleSaveDraft">
        {{ isSaving ? '保存中...' : '保存草稿' }}
      </button>
      <button class="action-btn primary" :disabled="isSaving" @click="handlePublish">
        {{ isSaving ? '发布中...' : '发布' }}
      </button>
      <span v-if="lastSavedAt" class="save-status">已保存 {{ lastSavedAt }}</span>
    </div>
  </aside>
</template>

<script setup lang="ts">
const emit = defineEmits<{
  insertMarkdown: [before: string, after: string, placeholder: string]
  close: []
}>()

const editor = injectEditor()
const {
  publishSettings, viewMode, isSaving, lastSavedAt,
} = editor

const tagInput = ref('')

interface CategoryItem { id: number; name: string; slug: string }
const categories = ref<CategoryItem[]>([])

function onCategoryChange(e: Event) {
  const val = (e.target as HTMLSelectElement).value
  publishSettings.categoryId = val ? Number(val) : null
}

const mdButtons = [
  { icon: 'B', label: '粗体', before: '**', after: '**', placeholder: '粗体文字' },
  { icon: 'I', label: '斜体', before: '*', after: '*', placeholder: '斜体文字' },
  { icon: 'H', label: '标题', before: '\n## ', after: '', placeholder: '标题' },
  { icon: '🔗', label: '链接', before: '[', after: '](url)', placeholder: '链接文字' },
  { icon: '🖼', label: '图片', before: '![', after: '](url)', placeholder: '图片描述' },
  { icon: '<>', label: '行内代码', before: '`', after: '`', placeholder: '代码' },
  { icon: '{}', label: '代码块', before: '\n```\n', after: '\n```\n', placeholder: '语言\n代码' },
  { icon: '❝', label: '引用', before: '\n> ', after: '', placeholder: '引用内容' },
  { icon: '•', label: '无序列表', before: '\n- ', after: '', placeholder: '列表项' },
  { icon: '1.', label: '有序列表', before: '\n1. ', after: '', placeholder: '列表项' },
  { icon: '—', label: '分割线', before: '\n---\n', after: '', placeholder: '' },
  { icon: '⊞', label: '表格', before: '\n| 列1 | 列2 |\n| --- | --- |\n| ', after: ' | ', placeholder: '内容' },
]

function addTag() {
  const name = tagInput.value.trim()
  if (!name) return
  const existing = categories.value.find(c => c.name === name)
  const tagId = existing ? existing.id : Date.now()
  if (!publishSettings.tagIds.includes(tagId)) {
    publishSettings.tagIds.push(tagId)
  }
  tagInput.value = ''
}

function removeTag(tagId: number) {
  publishSettings.tagIds = publishSettings.tagIds.filter(id => id !== tagId)
}

function getTagName(tagId: number): string {
  const cat = categories.value.find(c => c.id === tagId)
  return cat?.name ?? `标签 ${tagId}`
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

/* 发布设置 */
.settings-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.settings-field {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.field-label {
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  color: var(--muted);
}

.settings-select,
.settings-input,
.settings-textarea {
  font-family: var(--font-sans);
  font-size: var(--text-caption);
  color: var(--ink);
  background: var(--canvas-soft);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-md);
  padding: var(--space-xxs) var(--space-xs);
  outline: none;
  transition: border-color 0.15s ease;
}
.settings-select:focus,
.settings-input:focus,
.settings-textarea:focus { border-color: var(--hairline-strong); }
.settings-textarea { resize: vertical; line-height: var(--leading-normal); }

.tag-input-area { display: flex; flex-direction: column; gap: var(--space-xxs); }
.tag-list { display: flex; flex-wrap: wrap; gap: var(--space-xxs); }
.tag-remove {
  display: inline-flex;
  align-items: center;
  margin-left: 2px;
  padding: 0;
  background: transparent;
  border: none;
  color: var(--muted);
  cursor: pointer;
}
.tag-remove:hover { color: #c0392b; }

.settings-toggle-row {
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}

.toggle-btn {
  position: relative;
  width: 36px;
  height: 20px;
  border-radius: 10px;
  background: var(--hairline-strong);
  border: none;
  cursor: pointer;
  transition: background-color 0.2s ease;
  padding: 0;
}
.toggle-btn.on { background: var(--primary); }
.toggle-knob {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--on-primary);
  transition: transform 0.2s ease;
}
.toggle-btn.on .toggle-knob { transform: translateX(16px); }

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

/* 操作按钮 */
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
  padding: 0 var(--space-lg);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  line-height: 1;
  border: none;
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
  background: transparent;
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
