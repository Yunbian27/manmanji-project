<template>
  <Transition name="slide">
    <aside v-if="open" class="editor-publish-settings">
      <div class="settings-header">
        <h3 class="settings-title">发布设置</h3>
        <button class="settings-close" @click="$emit('close')">
          <IconX :size="18" />
        </button>
      </div>

      <div class="settings-body">
        <label class="settings-field">
          <span class="field-label">分类</span>
          <select v-model="publishSettings.categoryId" class="settings-select">
            <option :value="null">未选择</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </option>
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
              <AppTag
                v-for="tagId in publishSettings.tagIds"
                :key="tagId"
                variant="default"
              >
                {{ getTagName(tagId) }}
                <button class="tag-remove" @click="removeTag(tagId)">
                  <IconX :size="10" />
                </button>
              </AppTag>
            </div>
          </div>
        </label>

        <label class="settings-field">
          <span class="field-label">封面图片 URL</span>
          <input
            v-model="publishSettings.coverUrl"
            class="settings-input"
            placeholder="https://..."
          />
        </label>

        <label class="settings-field">
          <span class="field-label">摘要</span>
          <textarea
            v-model="publishSettings.summary"
            class="settings-textarea"
            placeholder="文章摘要（可选，最多 500 字）"
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
          <input
            v-model="publishSettings.sourceUrl"
            class="settings-input"
            placeholder="https://..."
          />
        </label>
      </div>
    </aside>
  </Transition>
</template>

<script setup lang="ts">
defineProps<{ open: boolean }>()
defineEmits<{ close: [] }>()

const editor = injectEditor()
const { publishSettings } = editor

const tagInput = ref('')

interface CategoryItem { id: number; name: string; slug: string }
const categories = ref<CategoryItem[]>([])

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
  // Try to find in categories first; fallback for user-typed tags
  const cat = categories.value.find(c => c.id === tagId)
  return cat?.name ?? `标签 ${tagId}`
}
</script>

<style scoped>
.editor-publish-settings {
  position: fixed;
  right: 0;
  top: var(--nav-height);
  width: 320px;
  height: calc(100vh - var(--nav-height));
  background: var(--surface-card);
  border-left: 1px solid var(--hairline);
  display: flex;
  flex-direction: column;
  z-index: 45;
}

.settings-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-md) var(--space-lg);
  border-bottom: 1px solid var(--hairline);
  flex-shrink: 0;
}

.settings-title {
  font-size: var(--text-title-sm);
  font-weight: var(--weight-semibold);
  color: var(--ink);
}

.settings-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  background: transparent;
  border: none;
  color: var(--muted);
  cursor: pointer;
  transition: background-color 0.15s ease;
}
.settings-close:hover { background: var(--surface-elevated); color: var(--body); }

.settings-body {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-lg);
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.settings-field {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
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
  font-size: var(--text-body-sm);
  color: var(--body);
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-md);
  padding: var(--space-xs) var(--space-sm);
  outline: none;
  transition: border-color 0.15s ease;
}
.settings-select:focus,
.settings-input:focus,
.settings-textarea:focus {
  border-color: var(--hairline-strong);
}

.settings-textarea {
  resize: vertical;
  line-height: var(--leading-normal);
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-xxs);
  margin-top: var(--space-xxs);
}

.tag-remove {
  display: inline-flex;
  align-items: center;
  margin-left: 4px;
  padding: 0;
  background: transparent;
  border: none;
  color: var(--muted);
  cursor: pointer;
}
.tag-remove:hover { color: var(--error); }

.settings-toggle-row {
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}

.toggle-btn {
  position: relative;
  width: 44px;
  height: 24px;
  border-radius: 12px;
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
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #0a0a0a;
  transition: transform 0.2s ease;
}
.toggle-btn.on .toggle-knob { transform: translateX(20px); }

/* slide transition */
.slide-enter-active,
.slide-leave-active {
  transition: transform 0.25s ease;
}
.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
}
</style>
