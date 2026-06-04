<!--
  PublishSettingsModal.vue — 发布设置弹窗（Notion DESIGN.md 规范）
-->
<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click.self="close">
        <div class="modal-card">
          <div class="modal-header">
            <h3 class="modal-title">发布设置</h3>
            <button class="modal-close" @click="close" aria-label="关闭">
              <IconX :size="18" />
            </button>
          </div>

          <form class="modal-form" @submit.prevent="handlePublish">
            <label class="form-field">
              <span class="field-label">分类</span>
              <select v-model="local.categoryId" class="form-select">
                <option :value="null">未选择</option>
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
              </select>
            </label>

            <label class="form-field">
              <span class="field-label">标签</span>
              <div class="tag-input-area">
                <input
                  v-model="tagInput"
                  class="form-input"
                  placeholder="输入标签后按回车"
                  @keydown.enter.prevent="addTag"
                />
                <div v-if="local.tagIds.length > 0" class="tag-list">
                  <AppTag v-for="tagId in local.tagIds" :key="tagId" variant="default">
                    {{ getTagName(tagId) }}
                    <button class="tag-remove" @click="removeTag(tagId)"><IconX :size="10" /></button>
                  </AppTag>
                </div>
              </div>
            </label>

            <label class="form-field">
              <span class="field-label">封面图片 URL</span>
              <input v-model="local.coverUrl" class="form-input" placeholder="https://..." />
            </label>

            <label class="form-field">
              <span class="field-label">摘要</span>
              <textarea
                v-model="local.summary"
                class="form-textarea"
                placeholder="文章摘要（最多 500 字）"
                maxlength="500"
                rows="3"
              />
            </label>

            <div class="form-field form-toggle-row">
              <span class="field-label">原创</span>
              <button
                type="button"
                :class="['toggle-btn', { on: local.isOriginal }]"
                @click="local.isOriginal = !local.isOriginal"
              >
                <span class="toggle-knob" />
              </button>
            </div>

            <label v-if="!local.isOriginal" class="form-field">
              <span class="field-label">转载来源 URL</span>
              <input v-model="local.sourceUrl" class="form-input" placeholder="https://..." />
            </label>

            <p v-if="publishError" class="form-error">{{ publishError }}</p>

            <div class="form-actions">
              <button type="button" class="action-btn secondary" :disabled="isSaving" @click="close">取消</button>
              <button type="submit" class="action-btn primary" :disabled="isSaving">
                {{ isSaving ? '发布中...' : '发布' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import type { PublishSettings } from '~/composables/useArticleEditor'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{ 'update:visible': [value: boolean]; published: [] }>()

const editor = injectEditor()
const { publishSettings, isSaving, publishError } = editor

const tagInput = ref('')

interface CategoryItem { id: number; name: string; slug: string }
const categories = ref<CategoryItem[]>([])

const local = reactive<PublishSettings>({
  categoryId: null,
  tagIds: [],
  coverUrl: '',
  summary: '',
  isOriginal: true,
  sourceUrl: '',
})

watch(() => props.visible, (v) => {
  if (v) {
    local.categoryId = publishSettings.categoryId
    local.tagIds = [...publishSettings.tagIds]
    local.coverUrl = publishSettings.coverUrl
    local.summary = publishSettings.summary
    local.isOriginal = publishSettings.isOriginal
    local.sourceUrl = publishSettings.sourceUrl
  }
})

function close() {
  emit('update:visible', false)
}

function syncToEditor() {
  publishSettings.categoryId = local.categoryId
  publishSettings.tagIds = local.tagIds
  publishSettings.coverUrl = local.coverUrl
  publishSettings.summary = local.summary
  publishSettings.isOriginal = local.isOriginal
  publishSettings.sourceUrl = local.sourceUrl
}

async function handlePublish() {
  syncToEditor()
  try {
    await editor.publish()
    emit('update:visible', false)
    emit('published')
  } catch {
    // error shown via publishError
  }
}

function addTag() {
  const name = tagInput.value.trim()
  if (!name) return
  const existing = categories.value.find(c => c.name === name)
  const tagId = existing ? existing.id : Date.now()
  if (!local.tagIds.includes(tagId)) {
    local.tagIds.push(tagId)
  }
  tagInput.value = ''
}

function removeTag(tagId: number) {
  local.tagIds = local.tagIds.filter(id => id !== tagId)
}

function getTagName(tagId: number): string {
  const cat = categories.value.find(c => c.id === tagId)
  return cat?.name ?? `标签 ${tagId}`
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && props.visible) close()
}
onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: var(--z-modal);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-md);
}

.modal-card {
  width: 100%;
  max-width: 440px;
  max-height: 90vh;
  overflow-y: auto;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-xl);
  padding: var(--spacing-xl);
  box-shadow: rgba(15, 15, 15, 0.08) 0px 4px 12px 0px;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-md);
}

.modal-title {
  font-size: var(--body-md);
  font-weight: var(--weight-medium);
  color: var(--ink);
}

.modal-close {
  width: 36px; height: 36px;
  border-radius: var(--rounded-full);
  border: none;
  background: transparent;
  color: var(--steel);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.15s var(--ease), color 0.15s var(--ease);
}
.modal-close:hover { background: var(--hairline-soft); color: var(--ink); }

.modal-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.form-error {
  font-size: var(--body-sm);
  color: #c0392b;
  padding: var(--spacing-xs) var(--spacing-sm);
  background: #fde8e8;
  border-radius: var(--rounded-md);
  margin: 0;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field-label {
  font-size: var(--body-sm);
  font-weight: var(--weight-medium);
  color: var(--steel);
}

.form-input,
.form-select,
.form-textarea {
  font-family: var(--font-sans);
  font-size: var(--body-md);
  font-weight: var(--weight-regular);
  color: var(--ink);
  background: var(--surface);
  border: 1px solid var(--hairline-strong);
  border-radius: var(--rounded-md);
  padding: var(--spacing-sm) var(--spacing-md);
  outline: none;
  transition: border-color 0.15s var(--ease);
}
.form-input::placeholder,
.form-textarea::placeholder { color: var(--muted); }
.form-input:focus,
.form-select:focus,
.form-textarea:focus { border-color: var(--primary); }

.form-select {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg width='10' height='6' viewBox='0 0 10 6' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1L5 5L9 1' stroke='%23787671' stroke-width='1.5' stroke-linecap='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 32px;
}

.form-textarea { resize: vertical; line-height: 1.4; }

.tag-input-area { display: flex; flex-direction: column; gap: 4px; }
.tag-list { display: flex; flex-wrap: wrap; gap: 4px; }
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

.form-toggle-row {
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
  transition: background-color 0.2s var(--ease);
  padding: 0;
  flex-shrink: 0;
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
  transition: transform 0.2s var(--ease);
}
.toggle-btn.on .toggle-knob { transform: translateX(16px); }

.form-actions {
  display: flex;
  gap: var(--spacing-xs);
  margin-top: var(--spacing-xs);
}

.action-btn {
  flex: 1;
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
</style>
