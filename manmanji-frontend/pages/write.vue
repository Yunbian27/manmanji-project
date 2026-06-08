<!--
  pages/write.vue — Markdown 文章编辑页
  Nuxt 路由：/write
  使用 editor 布局（自定义顶部导航栏常驻 + 无 Footer）
-->
<template>
  <div class="write-page">
    <EditorView :article-id="articleId" @close="handleClose" />

    <!-- 发布设置（EditorView 同级，常驻双栏下方，浏览器滚动可见） -->
    <div class="settings-divider" />
    <div class="publish-settings-area">
      <div class="ps-container">
        <h3 class="ps-title">发布设置</h3>
        <div class="ps-form">
          <div class="ps-field">
            <span class="ps-label">标签</span>
            <div class="tag-picker-wrap">
              <button type="button" class="tag-picker-trigger" @click="showTagPicker = !showTagPicker">
                <span v-if="local.tagIds.length === 0" class="picker-placeholder">选择标签</span>
                <div v-else class="tag-list">
                  <AppTag v-for="tagId in local.tagIds" :key="tagId" variant="default">{{ getTagName(tagId) }}<button class="tag-remove" @click.stop="removeTag(tagId)"><IconX :size="10" /></button></AppTag>
                </div>
                <svg class="picker-chevron" :class="{ open: showTagPicker }" width="10" height="6" viewBox="0 0 10 6" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M1 1L5 5L9 1"/></svg>
              </button>
              <div v-if="showTagPicker" class="tag-picker-popover">
                <button v-for="tag in availableTags" :key="tag.id" type="button" :class="['tag-picker-chip', { selected: local.tagIds.includes(tag.id) }]" @click="toggleTag(tag.id)">{{ tag.name }}</button>
              </div>
            </div>
          </div>
          <div class="ps-field">
            <span class="ps-label">分组</span>
            <div class="group-picker-wrap">
              <button type="button" class="tag-picker-trigger" @click="showGroupPicker = !showGroupPicker">
                <span v-if="local.groupNames.length === 0" class="picker-placeholder">选择或输入分组</span>
                <div v-else class="tag-list"><span v-for="(name, i) in local.groupNames" :key="i" class="group-chip">{{ name }}<button class="tag-remove" @click.stop="removeGroup(i)"><IconX :size="10" /></button></span></div>
                <svg class="picker-chevron" :class="{ open: showGroupPicker }" width="10" height="6" viewBox="0 0 10 6" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M1 1L5 5L9 1"/></svg>
              </button>
              <div v-if="showGroupPicker" class="group-picker-popover">
                <button v-for="g in filteredGroups" :key="g.id" type="button" :class="['tag-picker-chip', { selected: local.groupNames.includes(g.name) }]" @click="toggleGroup(g.name)">{{ g.name }}</button>
                <div v-if="filteredGroups.length === 0" class="picker-empty">暂无已有分组</div>
                <div class="group-picker-divider" />
                <input v-model="groupInput" class="group-picker-input" placeholder="输入新分组名，按回车添加" @keydown.enter.prevent="addGroup" />
              </div>
            </div>
          </div>
          <div class="ps-field">
            <span class="ps-label">封面图片</span>
            <div class="cover-upload-area">
              <input ref="coverInputRef" type="file" accept="image/*" class="cover-file-input" @change="handleCoverFile" />
              <button type="button" class="cover-upload-btn" @click="coverInputRef?.click()">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>选择图片
              </button>
              <div v-if="local.coverUrl" class="cover-preview-wrap"><img :src="local.coverUrl" class="cover-preview-img" /><button type="button" class="cover-remove-btn" @click="removeCover"><IconX :size="14" /></button></div>
            </div>
          </div>
          <div class="ps-field">
            <span class="ps-label">文章类型</span>
            <div class="radio-group">
              <button type="button" :class="['radio-btn', { active: local.articleType === 'ORIGINAL' }]" @click="local.articleType = 'ORIGINAL'">原创</button>
              <button type="button" :class="['radio-btn', { active: local.articleType === 'REPOST' }]" @click="local.articleType = 'REPOST'">转载</button>
            </div>
            <div v-if="local.articleType === 'REPOST'" class="repost-url-area"><input v-model="local.sourceUrl" class="form-input" placeholder="请输入转载文章URL" /><p class="repost-hint">转载请务必获得原作者授权</p></div>
          </div>
          <div class="ps-field">
            <span class="ps-label">文章摘要</span>
            <textarea v-model="local.summary" class="form-textarea" placeholder="文章摘要（最多 500 字）" maxlength="500" rows="2" />
          </div>
          <div class="ps-field">
            <span class="ps-label">可见范围</span>
            <select v-model="local.visibility" class="form-select">
              <option value="PRIVATE">仅自己可见</option>
              <option value="PUBLIC">公开可见</option>
            </select>
          </div>
          <p v-if="publishError" class="ps-error">{{ publishError }}</p>
          <div class="ps-actions">
            <button class="ps-btn-publish" :disabled="isSaving" @click="handlePublishSettings">{{ isSaving ? '发布中...' : '发布文章' }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { EDITOR_KEY, type PublishSettings, type EditorState } from '~/composables/useArticleEditor'
import type { GroupVO } from '~/types'

definePageMeta({ layout: 'editor' })

const route = useRoute()
const router = useRouter()
const articleId = ref(0)

watch(() => route.query.articleId, (val) => { articleId.value = Number(val) || 0 }, { immediate: true })

function handleClose() { router.push('/home') }

// ── 发布设置状态（惰性注入，等子组件 provide 后再取）──
const editor = inject<EditorState | null>(EDITOR_KEY, null)
const publishError = computed(() => editor?.publishError.value ?? null)
const isSaving = computed(() => editor?.isSaving.value ?? false)

const showTagPicker = ref(false)
const showGroupPicker = ref(false)
const groupInput = ref('')
const coverInputRef = ref<HTMLInputElement | null>(null)
const availableGroups = ref<GroupVO[]>([])

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
  tagIds: [], groupNames: [], coverUrl: '', articleType: 'ORIGINAL',
  summary: '', sourceUrl: '', visibility: 'PUBLIC', creationStatement: 'NONE',
})

const filteredGroups = computed(() => availableGroups.value.filter(g => !local.groupNames.includes(g.name)))

onMounted(async () => {
  await nextTick() // 等子组件 EditorView provide
  if (editor) {
    local.tagIds = [...editor.publishSettings.tagIds]
    local.groupNames = [...editor.publishSettings.groupNames]
    local.coverUrl = editor.publishSettings.coverUrl
    local.articleType = editor.publishSettings.articleType
    local.summary = editor.publishSettings.summary
    local.sourceUrl = editor.publishSettings.sourceUrl
    local.visibility = editor.publishSettings.visibility
    local.creationStatement = editor.publishSettings.creationStatement
  }
  try { availableGroups.value = await useArticle().listGroups() } catch { availableGroups.value = [] }
})

function getTagName(tagId: number) { return availableTags.find(t => t.id === tagId)?.name ?? `标签 ${tagId}` }
function toggleTag(tagId: number) { const i = local.tagIds.indexOf(tagId); i >= 0 ? local.tagIds.splice(i, 1) : local.tagIds.push(tagId) }
function removeTag(tagId: number) { local.tagIds = local.tagIds.filter(id => id !== tagId) }
function toggleGroup(name: string) { const i = local.groupNames.indexOf(name); i >= 0 ? local.groupNames.splice(i, 1) : local.groupNames.push(name) }
function removeGroup(i: number) { local.groupNames.splice(i, 1) }
function addGroup() { const n = groupInput.value.trim(); if (n && !local.groupNames.includes(n)) { local.groupNames.push(n); groupInput.value = '' } }
function handleCoverFile(e: Event) { const f = (e.target as HTMLInputElement).files?.[0]; if (f) { local.coverUrl = URL.createObjectURL(f); (e.target as HTMLInputElement).value = '' } }
function removeCover() { local.coverUrl = '' }

async function handlePublishSettings() {
  if (!editor) return
  Object.assign(editor.publishSettings, local)
  if (editor.titleError.value) { alert(editor.titleError.value); return }
  const ok = await editor.publish()
  if (ok) { alert('发布成功'); router.push('/home') }
}

function onDocClick(e: MouseEvent) {
  const t = e.target as HTMLElement
  if (showTagPicker.value && !t.closest('.tag-picker-wrap')) showTagPicker.value = false
  if (showGroupPicker.value && !t.closest('.group-picker-wrap')) showGroupPicker.value = false
}
onMounted(() => document.addEventListener('click', onDocClick))
onUnmounted(() => document.removeEventListener('click', onDocClick))
</script>

<style scoped>
.write-page { min-height: 100vh; }

.settings-divider { height: 1px; background: var(--hairline); }

.publish-settings-area {
  background: var(--canvas);
  padding: var(--spacing-xxl) var(--spacing-lg);
}

.ps-container { max-width: 720px; margin: 0 auto; }

.ps-title {
  font-family: var(--font-sans);
  font-size: var(--heading-3);
  font-weight: var(--weight-semibold);
  line-height: var(--leading-heading);
  color: var(--ink);
  margin: 0 0 var(--spacing-lg);
}

.ps-form { display: flex; flex-direction: column; gap: var(--spacing-md); }
.ps-field { display: flex; flex-direction: column; gap: 4px; }

.ps-label {
  font-size: var(--body-sm);
  font-weight: var(--weight-medium);
  color: var(--steel);
}

.ps-error {
  font-size: var(--body-sm);
  color: var(--semantic-error);
  padding: var(--spacing-xs) var(--spacing-sm);
  background: #fde8e8;
  border-radius: var(--rounded-md);
  margin: 0;
}

.ps-actions { display: flex; justify-content: flex-end; padding-top: var(--spacing-xs); }

.ps-btn-publish {
  height: 40px;
  padding: 0 var(--spacing-xl);
  border: none;
  border-radius: var(--rounded-md);
  background: var(--primary);
  color: var(--on-primary);
  font-family: var(--font-sans);
  font-size: var(--button-md);
  font-weight: var(--weight-medium);
  cursor: pointer;
  transition: background-color 0.15s var(--ease);
}
.ps-btn-publish:hover:not(:disabled) { background: var(--primary-pressed); }
.ps-btn-publish:disabled { opacity: 0.5; cursor: not-allowed; }

.tag-picker-wrap, .group-picker-wrap { position: relative; }

.tag-picker-trigger {
  display: flex; align-items: center; gap: 4px; width: 100%;
  min-height: var(--spacing-xxl); padding: var(--spacing-xs) var(--spacing-md);
  border: 1px solid var(--hairline-strong); border-radius: var(--rounded-md);
  background: var(--surface); cursor: pointer; transition: border-color 0.15s var(--ease);
}
.tag-picker-trigger:hover { border-color: var(--primary); }

.picker-placeholder { color: var(--muted); font-size: var(--body-md); flex: 1; text-align: left; }

.picker-chevron { margin-left: auto; flex-shrink: 0; color: var(--steel); transition: transform 0.2s var(--ease); }
.picker-chevron.open { transform: rotate(180deg); }

.tag-list { display: flex; flex-wrap: wrap; gap: 4px; }

.tag-remove {
  display: inline-flex; align-items: center; margin-left: 2px; padding: 0;
  background: transparent; border: none; color: var(--muted); cursor: pointer;
}
.tag-remove:hover { color: var(--semantic-error); }

.tag-picker-popover, .group-picker-popover {
  position: absolute; left: 0; right: 0; top: calc(100% + 4px);
  background: var(--canvas); border: 1px solid var(--hairline); border-radius: var(--rounded-lg);
  box-shadow: rgba(15, 15, 15, 0.08) 0px 4px 12px 0px; z-index: 10;
  max-height: 180px; overflow-y: auto;
}
.tag-picker-popover { display: flex; flex-wrap: wrap; gap: 6px; padding: var(--spacing-sm); }
.group-picker-popover { display: flex; flex-direction: column; gap: 0; padding: var(--spacing-sm); }

.tag-picker-chip {
  padding: 4px 10px; border: 1px solid var(--hairline); border-radius: var(--rounded-full);
  background: transparent; color: var(--steel); font-size: var(--body-sm); font-weight: var(--weight-medium);
  cursor: pointer; transition: all 0.15s var(--ease);
}
.tag-picker-chip:hover { border-color: var(--hairline-strong); color: var(--ink); }
.tag-picker-chip.selected { background: var(--ink-deep); border-color: var(--ink-deep); color: var(--on-dark); }

.picker-empty { width: 100%; text-align: center; font-size: var(--body-sm); color: var(--muted); padding: var(--spacing-sm); }

.group-picker-divider { height: 1px; background: var(--hairline-soft); margin: var(--spacing-xs) 0; }

.group-picker-input {
  width: 100%; padding: var(--spacing-xs) var(--spacing-sm);
  border: 1px solid var(--hairline); border-radius: var(--rounded-sm);
  background: var(--surface); color: var(--ink); font-size: var(--body-sm); outline: none;
  transition: border-color 0.15s var(--ease);
}
.group-picker-input::placeholder { color: var(--muted); }
.group-picker-input:focus { border-color: var(--primary); }

.group-chip {
  display: inline-flex; align-items: center; padding: var(--spacing-xs) var(--spacing-md);
  border: 1px solid var(--hairline); border-radius: var(--rounded-full);
  background: var(--surface); color: var(--ink); font-size: var(--body-sm-medium); font-weight: var(--weight-medium);
}

.cover-upload-area { display: flex; flex-wrap: wrap; align-items: flex-start; gap: var(--spacing-sm); }
.cover-file-input { display: none; }

.cover-upload-btn {
  display: inline-flex; align-items: center; gap: 6px; height: var(--spacing-xxl);
  padding: 0 var(--spacing-md); border: 1px dashed var(--hairline-strong); border-radius: var(--rounded-md);
  background: var(--surface); color: var(--steel); font-size: var(--body-sm); font-weight: var(--weight-medium);
  cursor: pointer; transition: border-color 0.15s var(--ease), color 0.15s var(--ease);
}
.cover-upload-btn:hover { border-color: var(--primary); color: var(--primary); }

.cover-preview-wrap { position: relative; width: 80px; height: 56px; border-radius: var(--rounded-md); overflow: hidden; border: 1px solid var(--hairline); }
.cover-preview-img { width: 100%; height: 100%; object-fit: cover; }

.cover-remove-btn {
  position: absolute; top: 2px; right: 2px; width: 20px; height: 20px;
  border-radius: var(--rounded-full); border: none; background: rgba(0, 0, 0, 0.55);
  color: var(--on-dark); cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: background 0.15s var(--ease);
}
.cover-remove-btn:hover { background: rgba(0, 0, 0, 0.75); }

.form-input, .form-select, .form-textarea {
  font-family: var(--font-sans); font-size: var(--body-md); color: var(--ink);
  background: var(--surface); border: 1px solid var(--hairline-strong); border-radius: var(--rounded-md);
  padding: var(--spacing-sm) var(--spacing-md); outline: none; transition: border-color 0.15s var(--ease);
}
.form-input::placeholder, .form-textarea::placeholder { color: var(--muted); }
.form-input:focus, .form-select:focus, .form-textarea:focus { border-color: var(--primary); }

.form-select {
  cursor: pointer; appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg width='10' height='6' viewBox='0 0 10 6' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1L5 5L9 1' stroke='%23787671' stroke-width='1.5' stroke-linecap='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat; background-position: right 12px center; padding-right: 32px;
}
.form-textarea { resize: vertical; line-height: 1.4; }

.radio-group { display: flex; border: 1px solid var(--hairline-strong); border-radius: var(--rounded-md); overflow: hidden; width: fit-content; }
.radio-btn {
  height: 36px; padding: 0 var(--spacing-md); border: none; background: var(--canvas);
  color: var(--steel); font-size: var(--body-sm); font-weight: var(--weight-medium); cursor: pointer;
  transition: background 0.15s var(--ease), color 0.15s var(--ease);
}
.radio-btn:not(:last-child) { border-right: 1px solid var(--hairline-strong); }
.radio-btn:hover { background: var(--hairline-soft); }
.radio-btn.active { background: var(--primary); color: var(--on-primary); }

.repost-url-area { display: flex; flex-direction: column; gap: 4px; margin-top: var(--spacing-xs); }
.repost-hint { font-size: var(--caption); color: var(--muted); margin: 0; }

@media (max-width: 640px) {
  .publish-settings-area { padding: var(--spacing-lg) var(--spacing-md); }
}
</style>
