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
            <!-- ① 标签 -->
            <div class="form-field">
              <span class="field-label">标签</span>
              <div class="tag-picker-wrap">
                <button type="button" class="tag-picker-trigger" @click="showTagPicker = !showTagPicker">
                  <span v-if="local.tagIds.length === 0" class="picker-placeholder">选择标签</span>
                  <div v-else class="tag-list">
                    <AppTag v-for="tagId in local.tagIds" :key="tagId" variant="default">
                      {{ getTagName(tagId) }}
                      <button class="tag-remove" @click.stop="removeTag(tagId)"><IconX :size="10" /></button>
                    </AppTag>
                  </div>
                  <svg class="picker-chevron" :class="{ open: showTagPicker }" width="10" height="6" viewBox="0 0 10 6" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M1 1L5 5L9 1"/></svg>
                </button>
                <div v-if="showTagPicker" class="tag-picker-popover">
                  <button
                    v-for="tag in availableTags"
                    :key="tag.id"
                    type="button"
                    :class="['tag-picker-chip', { selected: local.tagIds.includes(tag.id) }]"
                    @click="toggleTag(tag.id)"
                  >{{ tag.name }}</button>
                  <div v-if="availableTags.length === 0" class="picker-empty">暂无可选标签</div>
                </div>
              </div>
            </div>

            <!-- ② 分组 -->
            <div class="form-field">
              <span class="field-label">分组</span>
              <div class="group-input-area">
                <input
                  v-model="groupInput"
                  class="form-input"
                  placeholder="输入分组名后按回车"
                  @keydown.enter.prevent="addGroup"
                />
                <div v-if="local.groupNames.length > 0" class="tag-list">
                  <span v-for="(name, i) in local.groupNames" :key="i" class="group-chip">
                    {{ name }}
                    <button class="tag-remove" @click="removeGroup(i)"><IconX :size="10" /></button>
                  </span>
                </div>
              </div>
            </div>

            <!-- ③ 封面图片 -->
            <div class="form-field">
              <span class="field-label">封面图片</span>
              <div class="cover-upload-area">
                <input
                  ref="fileInputRef"
                  type="file"
                  accept="image/*"
                  class="cover-file-input"
                  @change="handleCoverFile"
                />
                <button type="button" class="cover-upload-btn" @click="fileInputRef?.click()">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
                  选择图片
                </button>
                <div v-if="local.coverUrl" class="cover-preview-wrap">
                  <img :src="local.coverUrl" class="cover-preview-img" />
                  <button type="button" class="cover-remove-btn" @click="removeCover">
                    <IconX :size="14" />
                  </button>
                </div>
              </div>
            </div>

            <!-- ④ 文章类型 -->
            <div class="form-field">
              <span class="field-label">文章类型</span>
              <div class="radio-group">
                <button
                  type="button"
                  :class="['radio-btn', { active: local.articleType === 'ORIGINAL' }]"
                  @click="local.articleType = 'ORIGINAL'"
                >原创</button>
                <button
                  type="button"
                  :class="['radio-btn', { active: local.articleType === 'REPOST' }]"
                  @click="local.articleType = 'REPOST'"
                >转载</button>
              </div>
              <Transition name="expand">
                <div v-if="local.articleType === 'REPOST'" class="repost-url-area">
                  <input v-model="local.sourceUrl" class="form-input" placeholder="请输入转载文章URL" />
                  <p class="repost-hint">转载请务必获得原作者授权</p>
                </div>
              </Transition>
            </div>

            <!-- ⑤ 文章摘要 -->
            <label class="form-field">
              <span class="field-label">文章摘要</span>
              <textarea
                v-model="local.summary"
                class="form-textarea"
                placeholder="文章摘要（最多 500 字）"
                maxlength="500"
                rows="3"
              />
            </label>

            <!-- ⑥ 可见范围 -->
            <label class="form-field">
              <span class="field-label">可见范围</span>
              <select v-model="local.visibility" class="form-select">
                <option value="PRIVATE">仅自己可见</option>
                <option value="PUBLIC">公开可见</option>
              </select>
            </label>

            <!-- ⑦ 创作声明 -->
            <label class="form-field">
              <span class="field-label">创作声明</span>
              <select v-model="local.creationStatement" class="form-select">
                <option value="NONE">无声明</option>
                <option value="AI_ASSISTED">部分内容由AI完成</option>
                <option value="NETWORK_SOURCED">内容来源于网络</option>
                <option value="PERSONAL_OPINION">个人观点</option>
              </select>
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

const showTagPicker = ref(false)
const groupInput = ref('')
const fileInputRef = ref<HTMLInputElement | null>(null)

const availableTags = [
  { id: 1, name: 'Java' }, { id: 2, name: '并发' }, { id: 3, name: 'PostgreSQL' },
  { id: 4, name: '数据库' }, { id: 5, name: 'Kubernetes' }, { id: 6, name: '分布式' },
  { id: 7, name: 'Spring Boot' }, { id: 8, name: '前端' }, { id: 9, name: 'Go' },
  { id: 10, name: 'Redis' }, { id: 11, name: '源码' }, { id: 12, name: '设计模式' },
  { id: 13, name: 'Linux' }, { id: 14, name: 'Python' }, { id: 15, name: '安全' },
  { id: 16, name: 'TypeScript' }, { id: 17, name: 'Elasticsearch' }, { id: 18, name: '消息队列' },
  { id: 19, name: 'Nginx' }, { id: 20, name: 'Docker' }, { id: 21, name: '微服务' },
  { id: 22, name: '性能优化' }, { id: 23, name: '网络' }, { id: 24, name: '运维' },
  { id: 25, name: 'AI' }, { id: 26, name: '机器学习' }, { id: 27, name: 'Rust' },
  { id: 28, name: '工程实践' }, { id: 29, name: '搜索' },
]

const local = reactive<PublishSettings>({
  tagIds: [],
  groupNames: [],
  coverUrl: '',
  articleType: 'ORIGINAL',
  summary: '',
  sourceUrl: '',
  visibility: 'PUBLIC',
  creationStatement: 'NONE',
})

watch(() => props.visible, (v) => {
  if (v) {
    local.tagIds = [...publishSettings.tagIds]
    local.groupNames = [...publishSettings.groupNames]
    local.coverUrl = publishSettings.coverUrl
    local.articleType = publishSettings.articleType
    local.summary = publishSettings.summary
    local.sourceUrl = publishSettings.sourceUrl
    local.visibility = publishSettings.visibility
    local.creationStatement = publishSettings.creationStatement
    showTagPicker.value = false
    groupInput.value = ''
  }
})

function close() {
  emit('update:visible', false)
}

function syncToEditor() {
  publishSettings.tagIds = local.tagIds
  publishSettings.groupNames = local.groupNames
  publishSettings.coverUrl = local.coverUrl
  publishSettings.articleType = local.articleType
  publishSettings.summary = local.summary
  publishSettings.sourceUrl = local.sourceUrl
  publishSettings.visibility = local.visibility
  publishSettings.creationStatement = local.creationStatement
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

function toggleTag(tagId: number) {
  const idx = local.tagIds.indexOf(tagId)
  if (idx >= 0) {
    local.tagIds.splice(idx, 1)
  } else {
    local.tagIds.push(tagId)
  }
}

function removeTag(tagId: number) {
  local.tagIds = local.tagIds.filter(id => id !== tagId)
}

function getTagName(tagId: number): string {
  const tag = availableTags.find(t => t.id === tagId)
  return tag?.name ?? `标签 ${tagId}`
}

function addGroup() {
  const name = groupInput.value.trim()
  if (!name || local.groupNames.includes(name)) return
  local.groupNames.push(name)
  groupInput.value = ''
}

function removeGroup(index: number) {
  local.groupNames.splice(index, 1)
}

function handleCoverFile(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  local.coverUrl = URL.createObjectURL(file)
  input.value = ''
}

function removeCover() {
  local.coverUrl = ''
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && props.visible) {
    if (showTagPicker.value) {
      showTagPicker.value = false
      return
    }
    close()
  }
}

function onClickOutside(e: MouseEvent) {
  if (showTagPicker.value) {
    const target = e.target as HTMLElement
    if (!target.closest('.tag-picker-wrap')) {
      showTagPicker.value = false
    }
  }
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))
onMounted(() => document.addEventListener('click', onClickOutside))
onUnmounted(() => document.removeEventListener('click', onClickOutside))
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
  color: var(--semantic-error);
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

/* ① Tag picker */
.tag-picker-wrap { position: relative; }

.tag-picker-trigger {
  display: flex;
  align-items: center;
  gap: 4px;
  width: 100%;
  min-height: var(--spacing-xxl);
  padding: var(--spacing-xs) var(--spacing-md);
  border: 1px solid var(--hairline-strong);
  border-radius: var(--rounded-md);
  background: var(--surface);
  cursor: pointer;
  transition: border-color 0.15s var(--ease);
}
.tag-picker-trigger:hover { border-color: var(--primary); }

.picker-placeholder {
  color: var(--muted);
  font-size: var(--body-md);
  flex: 1;
  text-align: left;
}

.picker-chevron {
  margin-left: auto;
  flex-shrink: 0;
  color: var(--steel);
  transition: transform 0.2s var(--ease);
}
.picker-chevron.open { transform: rotate(180deg); }

.tag-picker-popover {
  position: absolute;
  left: 0;
  right: 0;
  top: calc(100% + 4px);
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-lg);
  padding: var(--spacing-sm);
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  z-index: 10;
  box-shadow: rgba(15, 15, 15, 0.08) 0px 4px 12px 0px;
  max-height: 180px;
  overflow-y: auto;
}

.tag-picker-chip {
  padding: 4px 10px;
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-full);
  background: transparent;
  color: var(--steel);
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  font-weight: var(--weight-medium);
  cursor: pointer;
  transition: all 0.15s var(--ease);
}
.tag-picker-chip:hover { border-color: var(--hairline-strong); color: var(--ink); }
.tag-picker-chip.selected {
  background: var(--ink-deep);
  border-color: var(--ink-deep);
  color: var(--on-dark);
}

.picker-empty {
  width: 100%;
  text-align: center;
  font-size: var(--body-sm);
  color: var(--muted);
  padding: var(--spacing-sm);
}

/* Shared tag/group chips */
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
.tag-remove:hover { color: var(--semantic-error); }

/* ② Group chips */
.group-input-area { display: flex; flex-direction: column; gap: 4px; }

.group-chip {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-full);
  background: var(--surface);
  color: var(--slate);
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  font-weight: var(--weight-medium);
}

/* ③ Cover upload */
.cover-upload-area {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: var(--spacing-sm);
}

.cover-file-input {
  display: none;
}

.cover-upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: var(--spacing-xxl);
  padding: 0 var(--spacing-md);
  border: 1px dashed var(--hairline-strong);
  border-radius: var(--rounded-md);
  background: var(--surface);
  color: var(--steel);
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  font-weight: var(--weight-medium);
  cursor: pointer;
  transition: border-color 0.15s var(--ease), color 0.15s var(--ease);
}
.cover-upload-btn:hover { border-color: var(--primary); color: var(--primary); }
.cover-upload-btn svg { flex-shrink: 0; }

.cover-preview-wrap {
  position: relative;
  width: 80px;
  height: 56px;
  border-radius: var(--rounded-md);
  overflow: hidden;
  border: 1px solid var(--hairline);
}

.cover-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-remove-btn {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  border-radius: var(--rounded-full);
  border: none;
  background: rgba(0, 0, 0, 0.55);
  color: var(--on-dark);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s var(--ease);
}
.cover-remove-btn:hover { background: rgba(0, 0, 0, 0.75); }

/* ④ Article type radio */
.radio-group {
  display: flex;
  gap: 0;
  border: 1px solid var(--hairline-strong);
  border-radius: var(--rounded-md);
  overflow: hidden;
}

.radio-btn {
  flex: 1;
  height: 36px;
  padding: 0 var(--spacing-md);
  border: none;
  background: var(--canvas);
  color: var(--steel);
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  font-weight: var(--weight-medium);
  cursor: pointer;
  transition: background 0.15s var(--ease), color 0.15s var(--ease);
}
.radio-btn:not(:last-child) { border-right: 1px solid var(--hairline-strong); }
.radio-btn:hover { background: var(--hairline-soft); }
.radio-btn.active {
  background: var(--primary);
  color: var(--on-primary);
}

.repost-url-area {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: var(--spacing-xs);
}

.repost-hint {
  font-size: var(--caption);
  color: var(--muted);
  margin: 0;
}

/* Form actions */
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

/* Transitions */
.expand-enter-active,
.expand-leave-active {
  transition: opacity 0.2s var(--ease), max-height 0.25s var(--ease);
  overflow: hidden;
}
.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
}
.expand-enter-to,
.expand-leave-from {
  max-height: 80px;
}
</style>
