<template>
  <div class="editor-toolbar">
    <div class="toolbar-left">
      <button
        v-for="btn in formatButtons"
        :key="btn.label"
        class="toolbar-btn"
        :title="btn.label"
        @click="emit('format', btn.before, btn.after, btn.placeholder)"
      >
        <span class="btn-icon">{{ btn.icon }}</span>
        <span class="btn-label">{{ btn.label }}</span>
      </button>
    </div>

    <div class="toolbar-center">
      <button
        v-for="btn in insertButtons"
        :key="btn.label"
        class="toolbar-btn"
        :title="btn.label"
        @click="btn.id === 'image' ? handleImage() : emit('format', btn.before, btn.after, btn.placeholder)"
      >
        <span class="btn-icon">{{ btn.icon }}</span>
        <span class="btn-label">{{ btn.label }}</span>
      </button>
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        hidden
        @change="onFileSelected"
      />
    </div>

    <div class="toolbar-right">
      <button class="toolbar-btn" title="历史版本">
        <span class="btn-icon">↺</span>
        <span class="btn-label">历史</span>
      </button>
      <button class="toolbar-btn" title="导出">
        <span class="btn-icon">↗</span>
        <span class="btn-label">导出</span>
      </button>
      <button class="toolbar-btn" title="更多设置" @click="emit('openPublishSettings')">
        <span class="btn-icon">⚙</span>
        <span class="btn-label">设置</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
const emit = defineEmits<{
  format: [before: string, after: string, placeholder: string]
  openPublishSettings: []
}>()

const formatButtons = [
  { icon: 'B', label: '粗体', before: '**', after: '**', placeholder: '粗体文字' },
  { icon: 'I', label: '斜体', before: '*', after: '*', placeholder: '斜体文字' },
  { icon: 'S̶', label: '删除线', before: '~~', after: '~~', placeholder: '删除线' },
  { icon: 'H', label: '标题', before: '\n## ', after: '', placeholder: '标题' },
  { icon: '<>', label: '行内代码', before: '`', after: '`', placeholder: '代码' },
]

const insertButtons = [
  { id: 'list', icon: '•', label: '列表', before: '\n- ', after: '', placeholder: '列表项' },
  { id: 'code', icon: '{}', label: '代码块', before: '\n```\n', after: '\n```\n', placeholder: '语言\n代码' },
  { id: 'image', icon: '🖼', label: '图片', before: '', after: '', placeholder: '' },
  { id: 'link', icon: '🔗', label: '链接', before: '[', after: '](url)', placeholder: '链接文字' },
  { id: 'table', icon: '⊞', label: '表格', before: '\n| 列1 | 列2 |\n| --- | --- |\n| ', after: ' | ', placeholder: '内容' },
  { id: 'hr', icon: '—', label: '分割线', before: '\n---\n', after: '', placeholder: '' },
  { id: 'quote', icon: '❝', label: '引用', before: '\n> ', after: '', placeholder: '引用内容' },
]

const fileInput = ref<HTMLInputElement | null>(null)

function handleImage() {
  fileInput.value?.click()
}

async function onFileSelected(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  try {
    const { uploadImage } = useArticle()
    const url = await uploadImage(file)
    emit('format', '![', `](${url})`, '图片描述')
  } catch (err) {
    alert(err instanceof Error ? err.message : '上传失败')
  } finally {
    input.value = ''
  }
}
</script>

<style scoped>
.editor-toolbar {
  height: 44px;
  background: var(--canvas);
  border-bottom: 1px solid var(--hairline);
  padding: 0 var(--spacing-lg);
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.toolbar-left,
.toolbar-center,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 2px;
}

.toolbar-left {
  padding-right: var(--spacing-sm);
  border-right: 1px solid var(--hairline);
  margin-right: var(--spacing-sm);
}

.toolbar-center {
  flex: 1;
  padding-right: var(--spacing-sm);
  border-right: 1px solid var(--hairline);
  margin-right: var(--spacing-sm);
}

.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 30px;
  padding: 0 8px;
  border: none;
  border-radius: var(--rounded-sm);
  background: transparent;
  color: var(--steel);
  font-family: var(--font-sans);
  font-size: var(--caption);
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.12s var(--ease), color 0.12s var(--ease);
}

.toolbar-btn:hover {
  background: var(--hairline-soft);
  color: var(--ink);
}

.btn-icon {
  font-size: 13px;
  font-weight: var(--weight-semibold);
  width: 16px;
  text-align: center;
}

.btn-label {
  font-size: 11px;
  color: var(--muted);
}

.toolbar-btn:hover .btn-label {
  color: var(--slate);
}

@media (max-width: 768px) {
  .btn-label { display: none; }
  .toolbar-btn { padding: 0 6px; }
}

@media (max-width: 599px) {
  .editor-toolbar { padding: 0 var(--spacing-xs); }
  .toolbar-left { border-right: none; margin-right: 0; padding-right: 0; }
  .toolbar-right { display: none; }
}
</style>
