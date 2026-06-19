<!--
  pages/write.vue — Markdown 文章编辑页
  Nuxt 路由：/write
  使用 editor 布局（自定义顶部导航栏常驻 + 无 Footer）
-->
<template>
  <div class="write-page">
    <!-- 顶部热区（顶栏隐藏时悬停显示） -->
    <div
      v-if="topnavHidden"
      class="topnav-hotzone"
      @mouseenter="onHotzoneEnter"
      @mouseleave="onHotzoneLeave"
    />
    <Transition name="topnav-slide">
      <EditorTopNav
        v-if="!topnavHidden || topnavPeek"
        @mouseenter="onTopnavEnter"
        @mouseleave="onTopnavLeave"
      />
    </Transition>

    <!-- 滚动容器：编辑器 + 发布设置 -->
    <div class="write-body">
      <EditorView @close="handleClose" />

      <!-- 发布设置 -->
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
                  <AppTag v-for="tagId in local.tagIds" :key="tagId" variant="default">{{ getTagName(tagId) }}<button class="tag-remove" @click.stop="removeTag(tagId)"><IconLucideX class="icon-xxs" /></button></AppTag>
                </div>
                <IconLucideChevronDown class="picker-chevron" :class="{ open: showTagPicker }" />
              </button>
              <div v-if="showTagPicker" class="tag-picker-popover">
                <button v-for="tag in availableTags" :key="tag.id" type="button" :class="['tag-picker-chip', { selected: local.tagIds.includes(tag.id) }]" @click="toggleTag(tag.id)">{{ tag.name }}</button>
              </div>
            </div>
          </div>
          <div class="ps-field ps-field-group" @click.stop>
            <span class="ps-label">分组</span>
            <div class="group-tags-area">
              <span v-for="(name, i) in local.groupNames" :key="i" class="group-tag-pill">
                {{ name }}
                <button class="group-tag-remove" @click="removeGroup(i)"><IconLucideX class="icon-xs" /></button>
              </span>
              <button type="button" class="group-add-btn" @click="openGroupPicker">添加分组</button>
            </div>
            <div v-if="showGroupPicker" class="group-modal-overlay">
              <div class="group-modal-dialog">
                <div class="group-modal-header">
                  <span class="group-modal-title">分组</span>
                  <button type="button" class="group-modal-close" @click="showGroupPicker = false; groupSearch = ''">
                    <IconLucideX class="icon-md" />
                  </button>
                </div>
                <input
                  v-model="groupSearch"
                  class="group-modal-search"
                  placeholder="输入分组名，Enter 添加（最多50字）"
                  maxlength="50"
                  @keydown.enter.prevent="handleGroupSearchEnter"
                />
                <div class="group-modal-tags">
                  <button
                    v-for="g in filteredGroups"
                    :key="g.id"
                    type="button"
                    :class="['group-modal-tag', { selected: local.groupNames.includes(g.name) }]"
                    @click="toggleGroup(g)"
                  >{{ g.name }}</button>
                  <div v-if="filteredGroups.length === 0" class="group-modal-empty">暂无匹配分组，按 Enter 创建</div>
                </div>
              </div>
            </div>
          </div>
          <div class="ps-field">
            <span class="ps-label">封面图片</span>
            <div class="cover-upload-area">
              <input ref="coverInputRef" type="file" accept="image/*" class="cover-file-input" @change="handleCoverFile" />
              <button type="button" class="cover-upload-btn" @click="coverInputRef?.click()">
                <IconLucideUpload class="icon-md" />选择图片
              </button>
              <div v-if="local.coverUrl" class="cover-preview-wrap"><img :src="local.coverUrl" class="cover-preview-img" /><button type="button" class="cover-remove-btn" @click="removeCover"><IconLucideX class="icon-sm" /></button></div>
            </div>
          </div>
          <div class="ps-field">
            <span class="ps-label">文章类型</span>
            <div class="visibility-radio-group">
              <label v-for="opt in articleTypeOptions" :key="opt.value"
                class="visibility-radio"
                :class="{ active: local.articleType === opt.value }"
                @click="local.articleType = opt.value"
              >
                <span class="visibility-radio-dot">
                  <span v-if="local.articleType === opt.value" class="visibility-radio-dot-fill" />
                </span>
                <span class="visibility-radio-label">{{ opt.label }}</span>
              </label>
            </div>
            <div v-if="local.articleType === 'REPOST'" class="repost-url-area"><input v-model="local.sourceUrl" class="form-input" placeholder="请输入转载文章URL" /><p class="repost-hint">转载请务必获得原作者授权</p></div>
          </div>
          <div class="ps-field">
            <span class="ps-label">文章摘要</span>
            <textarea v-model="local.summary" class="form-textarea" placeholder="输入文章摘要（最多150字）" maxlength="150" rows="2" />
            <span class="ps-count">{{ local.summary.length }}/150</span>
          </div>
          <div class="ps-field">
            <span class="ps-label">可见范围</span>
            <div class="visibility-radio-group">
              <label v-for="opt in visibilityOptions" :key="opt.value"
                class="visibility-radio"
                :class="{ active: local.visibility === opt.value }"
                @click="local.visibility = opt.value"
              >
                <span class="visibility-radio-dot">
                  <span v-if="local.visibility === opt.value" class="visibility-radio-dot-fill" />
                </span>
                <span class="visibility-radio-label">{{ opt.label }}</span>
              </label>
            </div>
          </div>
          <div class="ps-actions">
            <button class="ps-btn-publish" :disabled="isSaving" @click="handlePublishSettings">{{ isSaving ? '发布中...' : '发布文章' }}</button>
          </div>
        </div>
      </div>
    </div>
    </div><!-- .write-body -->
  </div>
</template>

<script setup lang="ts">
import { createEditorState, provideEditor, type PublishSettings, type EditorState } from '~/composables/useArticleEditor'
import IconLucideChevronDown from '~icons/lucide/chevron-down'
import IconLucideUpload from '~icons/lucide/upload'
import IconLucideX from '~icons/lucide/x'
import type { GroupVO } from '~/types'

definePageMeta({ layout: 'editor', middleware: 'role-guard' })

const route = useRoute()
const router = useRouter()
const articleId = ref(0)

watch(() => route.query.articleId, (val) => { articleId.value = Number(val) || 0 }, { immediate: true })

function handleClose() { router.push('/home') }

// ── Topnav hide/show（相对于 EditorView 提升至此） ──
const topnavHidden = ref(false)
const topnavPeek = ref(false)
let hideTimer: ReturnType<typeof setTimeout> | null = null

provide('topnavHidden', topnavHidden)

function toggleTopnav() {
  topnavHidden.value = !topnavHidden.value
  topnavPeek.value = false
}
provide('toggleTopnav', toggleTopnav)

function onHotzoneEnter() {
  if (!topnavHidden.value) return
  topnavPeek.value = true
  if (hideTimer) { clearTimeout(hideTimer); hideTimer = null }
}

function onHotzoneLeave() {
  if (!topnavHidden.value) return
  hideTimer = setTimeout(() => { topnavPeek.value = false }, 300)
}

function onTopnavEnter() {
  if (!topnavHidden.value) return
  if (hideTimer) { clearTimeout(hideTimer); hideTimer = null }
}

function onTopnavLeave() {
  if (!topnavHidden.value) return
  hideTimer = setTimeout(() => { topnavPeek.value = false }, 300)
}

// ── 在父组件创建 editor 状态并 provide，子组件通过 inject 获取 ──
const editor = createEditorState(articleId.value)
provideEditor(editor)

watch(articleId, async (id) => {
  editor.currentArticleId.value = id
  if (id > 0) {
    await editor.loadFromServer()
    local.tagIds = [...editor.publishSettings.tagIds]
    local.groupNames = [...editor.publishSettings.groupNames]
    local.coverUrl = editor.publishSettings.coverUrl
    local.articleType = editor.publishSettings.articleType
    local.summary = editor.publishSettings.summary
    local.sourceUrl = editor.publishSettings.sourceUrl
    local.visibility = editor.publishSettings.visibility
    local.creationStatement = editor.publishSettings.creationStatement
  }
}, { immediate: true })

const toast = injectToast()
const isSaving = computed(() => editor.isSaving.value ?? false)

const showTagPicker = ref(false)
const showGroupPicker = ref(false)
const groupSearch = ref('')
const coverInputRef = ref<HTMLInputElement | null>(null)
const availableGroups = ref<GroupVO[]>([])

const articleTypeOptions = [
  { value: 'ORIGINAL' as const, label: '原创' },
  { value: 'REPOST' as const, label: '转载' },
]

const visibilityOptions = [
  { value: 'PUBLIC' as const, label: '公开可见' },
  { value: 'PRIVATE' as const, label: '仅我可见' },
  { value: 'FOLLOWER' as const, label: '粉丝可见' },
]

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

const filteredGroups = computed(() => {
  const q = groupSearch.value.trim().toLowerCase()
  if (!q) return availableGroups.value
  return availableGroups.value.filter(g => g.name.toLowerCase().includes(q))
})

onMounted(async () => {
  try { availableGroups.value = await useArticle().listGroups() } catch { availableGroups.value = [] }
})

function getTagName(tagId: number) { return availableTags.find(t => t.id === tagId)?.name ?? `标签 ${tagId}` }
function toggleTag(tagId: number) { const i = local.tagIds.indexOf(tagId); i >= 0 ? local.tagIds.splice(i, 1) : local.tagIds.push(tagId) }
function removeTag(tagId: number) { local.tagIds = local.tagIds.filter(id => id !== tagId) }
function toggleGroup(g: GroupVO) { const i = local.groupNames.indexOf(g.name); i >= 0 ? local.groupNames.splice(i, 1) : local.groupNames.push(g.name) }
function removeGroup(i: number) { local.groupNames.splice(i, 1) }
function openGroupPicker() {
  groupSearch.value = ''
  showGroupPicker.value = true
}
function handleGroupSearchEnter() {
  const q = groupSearch.value.trim()
  if (!q) return
  const match = availableGroups.value.find(g => g.name === q)
  if (match) {
    toggleGroup(match)
  } else if (!local.groupNames.includes(q)) {
    local.groupNames.push(q)
  }
  groupSearch.value = ''
}
function handleCoverFile(e: Event) { const f = (e.target as HTMLInputElement).files?.[0]; if (f) { local.coverUrl = URL.createObjectURL(f); (e.target as HTMLInputElement).value = '' } }
function removeCover() { local.coverUrl = '' }

async function handlePublishSettings() {
  Object.assign(editor.publishSettings, local)
  const ok = await editor.publish()
  if (ok) {
    toast.show('发布成功', 'success')
    router.push('/home')
  } else if (editor.titleError.value) {
    toast.show(editor.titleError.value, 'error')
  } else if (editor.publishError.value) {
    toast.show(editor.publishError.value, 'error')
  }
}

function onDocClick(e: MouseEvent) {
  const t = e.target as HTMLElement
  if (showTagPicker.value && !t.closest('.tag-picker-wrap')) showTagPicker.value = false
  if (showGroupPicker.value && !t.closest('.group-modal-dialog') && !t.closest('.ps-field-group')) showGroupPicker.value = false
}
onMounted(() => document.addEventListener('click', onDocClick))
onUnmounted(() => document.removeEventListener('click', onDocClick))
</script>

<style scoped>
.write-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ── Topnav hotzone ────────────────────────────────────── */
.topnav-hotzone {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 6px;
  z-index: 101;
}

/* ── Topnav slide transition ───────────────────────────── */
.topnav-slide-enter-active,
.topnav-slide-leave-active {
  transition: transform 0.25s var(--ease);
}

.topnav-slide-enter-from,
.topnav-slide-leave-to {
  transform: translateY(-100%);
}

/* ── Scroll body ───────────────────────────────────────── */
.write-body {
  flex: 1;
  overflow-y: auto;
  position: relative;
}

.publish-settings-area {
  background: var(--canvas);
  padding: var(--spacing-xl);
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

.ps-count {
  font-size: var(--caption);
  color: var(--muted);
  align-self: flex-end;
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

.tag-picker-wrap { position: relative; }

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

.tag-picker-popover {
  position: absolute; left: 0; right: 0; top: calc(100% + 4px);
  background: var(--canvas); border: 1px solid var(--hairline); border-radius: var(--rounded-lg);
  box-shadow: rgba(15, 15, 15, 0.08) 0px 4px 12px 0px; z-index: 10;
  max-height: 180px; overflow-y: auto;
  display: flex; flex-wrap: wrap; gap: 6px; padding: var(--spacing-sm);
}

.tag-picker-chip {
  padding: 4px 10px; border: 1px solid var(--hairline); border-radius: var(--rounded-full);
  background: transparent; color: var(--steel); font-size: var(--body-sm); font-weight: var(--weight-medium);
  cursor: pointer; transition: all 0.15s var(--ease);
}
.tag-picker-chip:hover { border-color: var(--hairline-strong); color: var(--ink); }
.tag-picker-chip.selected { background: var(--ink-deep); border-color: var(--ink-deep); color: var(--on-dark); }

.picker-empty { width: 100%; text-align: center; font-size: var(--body-sm); color: var(--muted); padding: var(--spacing-sm); }
/* ── 分组字段定位锚点 ── */
.ps-field-group { position: relative; }

/* ── 分组弹窗（上方弹出）── */
.group-modal-overlay {
  position: absolute; bottom: calc(100% + 8px); left: 0; z-index: 100;
}

.group-modal-dialog {
  background: var(--canvas);
  border-radius: var(--rounded-lg);
  border: 1px solid var(--hairline);
  box-shadow: rgba(15, 15, 15, 0.16) 0px 16px 48px -8px;
  width: 400px; max-width: calc(100vw - 40px); max-height: 50vh;
  display: flex; flex-direction: column;
  overflow: hidden;
}

.group-modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--spacing-lg) var(--spacing-lg) 0;
  flex-shrink: 0;
}

.group-modal-title {
  font-size: var(--heading-4);
  font-weight: var(--weight-semibold);
  color: var(--ink);
}

.group-modal-close {
  display: flex; align-items: center; justify-content: center;
  width: 32px; height: 32px; padding: 0;
  border: none; border-radius: var(--rounded-sm);
  background: transparent; color: var(--steel); cursor: pointer;
  transition: background 0.2s var(--ease), color 0.2s var(--ease);
}
.group-modal-close:hover { background: var(--hairline-soft); color: var(--ink); }

.group-modal-search {
  margin: var(--spacing-lg);
  height: 36px; padding: 0 var(--spacing-md);
  border: 1px solid var(--hairline-strong); border-radius: var(--rounded-md);
  background: var(--canvas); color: var(--ink);
  font-family: var(--font-sans); font-size: var(--body-sm); outline: none;
  transition: border-color 0.2s var(--ease);
  flex-shrink: 0;
}
.group-modal-search::placeholder { color: var(--muted); }
.group-modal-search:focus { border-color: var(--primary); }

.group-modal-tags {
  display: flex; flex-wrap: wrap; gap: var(--spacing-xs);
  padding: 0 var(--spacing-lg) var(--spacing-lg);
  overflow-y: auto;
  min-height: 0;
}

.group-modal-tag {
  display: inline-flex; align-items: center;
  padding: var(--spacing-xs) var(--spacing-md);
  border: 1px solid var(--hairline); border-radius: var(--rounded-full);
  background: transparent; color: var(--steel);
  font-family: var(--font-sans); font-size: var(--body-sm); font-weight: var(--weight-medium);
  cursor: pointer;
  transition: background 0.2s var(--ease), color 0.2s var(--ease), border-color 0.2s var(--ease);
}
.group-modal-tag:hover { border-color: var(--hairline-strong); color: var(--ink); }
.group-modal-tag.selected {
  background: var(--ink-deep); border-color: var(--ink-deep); color: var(--on-dark);
}

.group-modal-empty {
  width: 100%; text-align: center; font-size: var(--body-sm); color: var(--muted);
  padding: var(--spacing-lg) 0;
}

/* ── 分组标签 ── */
.group-tags-area {
  display: flex; flex-wrap: wrap; align-items: center; gap: var(--spacing-xs);
}

.group-tag-pill {
  display: inline-flex; align-items: center; gap: 4px;
  padding: var(--spacing-xs) var(--spacing-md);
  background: var(--surface); color: var(--ink);
  border-radius: var(--rounded-md);
  font-family: var(--font-sans); font-size: var(--body-sm-medium); font-weight: var(--weight-medium);
}

.group-tag-remove {
  display: inline-flex; align-items: center; justify-content: center;
  padding: 0; border: none; background: transparent;
  color: var(--stone); cursor: pointer;
  transition: color 0.2s var(--ease);
}
.group-tag-remove:hover { color: var(--ink); }

.group-add-btn {
  display: inline-flex; align-items: center;
  padding: var(--spacing-xs) var(--spacing-md);
  border: 1px solid var(--hairline); border-radius: var(--rounded-md);
  background: transparent; color: var(--ink);
  font-family: var(--font-sans); font-size: var(--body-sm-medium); font-weight: var(--weight-medium);
  cursor: pointer;
  transition: background 0.2s var(--ease), border-color 0.2s var(--ease);
}
.group-add-btn:hover { background: var(--hairline-soft); border-color: var(--hairline-strong); }

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

/* Visibility radio */
.visibility-radio-group { display: flex; gap: var(--spacing-lg); }

.visibility-radio {
  display: flex; align-items: center; gap: var(--spacing-xs); cursor: pointer;
}

.visibility-radio-dot {
  width: 18px; height: 18px;
  display: flex; align-items: center; justify-content: center;
  border-radius: var(--rounded-full);
  border: 1.5px solid var(--hairline-strong);
  flex-shrink: 0;
  transition: border-color 0.15s var(--ease);
}
.visibility-radio.active .visibility-radio-dot { border-color: var(--primary); }

.visibility-radio-dot-fill {
  width: 8px; height: 8px;
  border-radius: var(--rounded-full);
  background: var(--primary);
}

.visibility-radio-label {
  font-size: var(--body-sm-medium);
  font-weight: var(--weight-medium);
  color: var(--steel);
  transition: color 0.15s var(--ease);
}
.visibility-radio.active .visibility-radio-label { color: var(--ink); }
.visibility-radio:hover { opacity: 0.8; }

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

.repost-url-area { display: flex; flex-direction: column; gap: 4px; margin-top: var(--spacing-xs); }
.repost-hint { font-size: var(--caption); color: var(--muted); margin: 0; }

@media (max-width: 640px) {
  .publish-settings-area { padding: var(--spacing-lg) var(--spacing-md); }
}
</style>
