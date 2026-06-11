<template>
  <div class="editor-view">
    <div class="editor-body" :style="{ '--panel-ratio': panelRatio }">
      <!-- 单卡片容器 -->
      <div class="editor-card">
        <!-- 卡片顶部工具栏 -->
        <div class="card-toolbar">
          <div class="toolbar-left-group">
          <div class="toolbar-left">
            <button
              v-for="btn in formatButtons"
              :key="btn.label"
              class="toolbar-btn"
              :title="btn.label"
              @click="onInsertMarkdown(btn.before, btn.after, btn.placeholder)"
            >
              <span class="btn-icon">{{ btn.icon }}</span>
            </button>
            <div class="dropdown-wrap" ref="headingDropdownRef">
              <button class="toolbar-btn" title="标题" @click="toggleHeading">
                <span class="btn-icon">H</span>
              </button>
              <div v-if="showHeadingMenu" class="toolbar-dropdown">
                <button
                  v-for="h in headingLevels"
                  :key="h.level"
                  class="dropdown-item"
                  @click="insertHeading(h); showHeadingMenu = false"
                >
                  <span class="dropdown-item-prefix">{{ '#'.repeat(h.level) }}</span>
                  {{ h.label }}
                </button>
              </div>
            </div>
            <span class="toolbar-divider" />
            <div class="dropdown-wrap" ref="listDropdownRef">
              <button class="toolbar-btn toolbar-btn--labeled" @click="toggleList">
                <span class="btn-icon">≡</span>
                <span class="btn-label">列表</span>
                <span class="btn-arrow">▾</span>
              </button>
              <div v-if="showListMenu" class="toolbar-dropdown">
                <button
                  v-for="item in listTypes"
                  :key="item.id"
                  class="dropdown-item"
                  @click="insertList(item); showListMenu = false"
                >
                  {{ item.label }}
                </button>
              </div>
            </div>
            <div class="dropdown-wrap" ref="codeDropdownRef">
              <button class="toolbar-btn toolbar-btn--labeled" @click="toggleCode">
                <span class="btn-icon">{ }</span>
                <span class="btn-label">代码块</span>
                <span class="btn-arrow">▾</span>
              </button>
              <div v-if="showCodeMenu" class="toolbar-dropdown toolbar-dropdown--wide">
                <button
                  v-for="lang in codeLanguages"
                  :key="lang.id"
                  class="dropdown-item"
                  @click="insertCodeBlock(lang.id); showCodeMenu = false"
                >
                  {{ lang.label }}
                </button>
              </div>
            </div>
            <div class="dropdown-wrap" ref="moreDropdownRef">
              <button class="toolbar-btn toolbar-btn--labeled" @click="toggleMore">
                <span class="btn-label">更多格式</span>
                <span class="btn-arrow">▾</span>
              </button>
              <div v-if="showMoreMenu" class="toolbar-dropdown">
                <button
                  v-for="item in moreItems"
                  :key="item.label"
                  class="dropdown-item"
                  @click="onInsertMarkdown(item.before, item.after, item.placeholder); showMoreMenu = false"
                >
                  <span class="btn-icon dropdown-item-icon">{{ item.icon }}</span>
                  {{ item.label }}
                </button>
              </div>
            </div>
          </div>

          <div class="toolbar-center">
            <!-- 图片 -->
            <button class="toolbar-btn" title="图片" @click="handleImage">
              <IconLucideImage class="btn-icon-svg" />
            </button>
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              hidden
              @change="onFileSelected"
            />
            <!-- 链接 -->
            <button class="toolbar-btn" title="链接" @click="showLinkDialog = true">
              <IconLucideLink class="btn-icon-svg" />
            </button>
          </div>
          </div>

          <div class="toolbar-view-toggle">
            <div class="view-toggle">
              <button :class="['view-btn', { active: viewMode === 'edit' }]" @click="viewMode = 'edit'">编辑</button>
              <button :class="['view-btn', { active: viewMode === 'split' }]" @click="viewMode = 'split'">对比</button>
              <button :class="['view-btn', { active: viewMode === 'preview' }]" @click="viewMode = 'preview'">预览</button>
            </div>
          </div>

          <div class="toolbar-right">
            <button class="toolbar-btn toolbar-btn--labeled" title="历史版本">
              <IconLucideClock class="btn-icon-svg" />
              <span class="btn-label">历史版本</span>
            </button>
            <div class="dropdown-wrap" ref="actionDropdownRef">
              <button class="toolbar-btn toolbar-btn--labeled" @click="showActionMenu = !showActionMenu">
                <span class="btn-icon">···</span>
                <span class="btn-label">更多操作</span>
                <span class="btn-arrow">▾</span>
              </button>
              <div v-if="showActionMenu" class="toolbar-dropdown toolbar-dropdown--right">
                <button class="dropdown-item" @click="showActionMenu = false">导入</button>
                <button class="dropdown-item" @click="showActionMenu = false">导出</button>
                <button class="dropdown-item" @click="showActionMenu = false">模板</button>
              </div>
            </div>
            <button class="toolbar-btn" :title="topnavHidden ? '展开顶栏' : '收起顶栏'" @click="toggleTopnav">
              <IconLucideChevronDown v-if="topnavHidden" class="btn-icon-svg" />
              <IconLucideChevronUp v-else class="btn-icon-svg" />
            </button>
          </div>
        </div>

        <!-- 卡片内容区 -->
        <div :class="['card-content', { 'card-content--single': viewMode !== 'split' }]">
          <div
            v-if="viewMode !== 'preview'"
            :class="['pane-edit', { 'pane-edit--centered': viewMode === 'edit' }]"
          >
            <div class="pane-edit-inner">
              <EditorTextarea
                ref="textareaRef"
                @scroll="onTextareaScroll"
                @selection-change="onSelectionChange"
              />
            </div>
          </div>

          <div v-if="viewMode === 'split'" class="static-divider" />

          <div
            v-if="viewMode !== 'edit'"
            :class="['pane-preview', { 'pane-preview--split': viewMode === 'split' }]"
          >
            <EditorPreview
              ref="previewRef"
              :centered="viewMode === 'preview'"
              @scroll="onPreviewScroll"
            />
          </div>
        </div>

        <!-- 卡片底部操作栏 -->
        <div class="card-statusbar">
          <div class="statusbar-left">
            <span class="statusbar-stat">{{ wordCount }} 字</span>
            <span class="statusbar-divider">·</span>
            <span class="statusbar-stat">{{ lineCount }} 行</span>
            <span class="statusbar-divider">·</span>
            <span class="statusbar-stat">第 {{ cursorRow }} 行，第 {{ cursorCol }} 列</span>
          </div>
          <div class="statusbar-right">
            <span v-if="lastSavedAt" class="statusbar-timestamp">已保存 {{ lastSavedAt }}</span>
            <button class="statusbar-btn statusbar-btn--save" :disabled="isSaving" @click="handleSave">
              {{ isSaving ? '保存中...' : '保存草稿' }}
            </button>
            <button class="statusbar-btn statusbar-btn--publish" :disabled="isSaving" @click="scrollToSettings">
              发布设置
            </button>
          </div>
        </div>
      </div>

      <!-- 卡片与面板分隔线 -->
      <DraggableDivider v-model:split-ratio="panelRatio" class="card-divider" />

      <!-- 右侧悬浮按钮 -->
      <RightFloatingPanel :toc-items="outlineItems" @navigate-to-heading="handleNavigateToHeading" />
    </div>


    <!-- 链接插入弹窗 -->
    <Transition name="link-dialog">
      <div v-if="showLinkDialog" class="link-dialog-backdrop" @click.self="closeLinkDialog">
        <div class="link-dialog">
          <div class="link-dialog-header">插入链接</div>
          <div class="link-dialog-body">
            <input
              ref="linkTextInput"
              v-model="linkText"
              class="link-dialog-input"
              placeholder="链接名称"
              @keydown.enter="linkUrlInput?.focus()"
            />
            <input
              ref="linkUrlInput"
              v-model="linkUrl"
              class="link-dialog-input"
              placeholder="链接地址（https://...）"
              @keydown.enter="confirmInsertLink"
            />
          </div>
          <div class="link-dialog-footer">
            <button class="link-dialog-btn link-dialog-btn--cancel" @click="closeLinkDialog">取消</button>
            <button class="link-dialog-btn link-dialog-btn--confirm" :disabled="!linkText.trim() || !linkUrl.trim()" @click="confirmInsertLink">确定</button>
          </div>
        </div>
      </div>
    </Transition>

  </div>
</template>

<script setup lang="ts">
import { onClickOutside } from '@vueuse/core'
import type { TocItem } from '~/types'
import IconLucideImage from '~icons/lucide/image'
import IconLucideLink from '~icons/lucide/link'
import IconLucideClock from '~icons/lucide/clock'
import IconLucideChevronDown from '~icons/lucide/chevron-down'
import IconLucideChevronUp from '~icons/lucide/chevron-up'

const emit = defineEmits<{ close: [] }>()

const editor = injectEditor()
const toast = injectToast()

const { content, title, viewMode, isSaving, lastSavedAt } = editor
const panelRatio = ref(0.5)

// Topnav visibility — injected from write.vue
const topnavHidden = inject<Ref<boolean>>('topnavHidden', ref(false))
const toggleTopnav = inject<() => void>('toggleTopnav', () => {})

onBeforeRouteLeave((_to, _from, next) => {
  if (content.value || title.value) {
    if (!confirm('有未保存的内容，确定离开吗？')) {
      next(false)
      return
    }
  }
  next()
})

function handleBeforeUnload(e: BeforeUnloadEvent) {
  if (content.value || title.value) {
    e.preventDefault()
    e.returnValue = ''
  }
}

onMounted(() => window.addEventListener('beforeunload', handleBeforeUnload))
onUnmounted(() => window.removeEventListener('beforeunload', handleBeforeUnload))

// ── Toolbar buttons ──────────────────────────────────────────
const formatButtons = [
  { icon: 'B', label: '粗体', before: '**', after: '**', placeholder: '粗体文字' },
  { icon: 'I', label: '斜体', before: '*', after: '*', placeholder: '斜体文字' },
  { icon: 'S̶', label: '删除线', before: '~~', after: '~~', placeholder: '删除线' },
]

const fileInput = ref<HTMLInputElement | null>(null)

// ── Dropdown toggles ──────────────────────────────────────
function closeAllDropdowns() {
  showHeadingMenu.value = false
  showCodeMenu.value = false
  showListMenu.value = false
  showMoreMenu.value = false
}

function toggleHeading() {
  showCodeMenu.value = false
  showListMenu.value = false
  showMoreMenu.value = false
  showHeadingMenu.value = !showHeadingMenu.value
}

function toggleList() {
  showHeadingMenu.value = false
  showCodeMenu.value = false
  showMoreMenu.value = false
  showListMenu.value = !showListMenu.value
}

function toggleCode() {
  showHeadingMenu.value = false
  showListMenu.value = false
  showMoreMenu.value = false
  showCodeMenu.value = !showCodeMenu.value
}

function toggleMore() {
  showHeadingMenu.value = false
  showListMenu.value = false
  showCodeMenu.value = false
  showMoreMenu.value = !showMoreMenu.value
}

// ── Heading dropdown ──────────────────────────────────────
const showHeadingMenu = ref(false)
const headingDropdownRef = ref<HTMLElement | null>(null)

const headingLevels = [
  { level: 1, label: '一级标题' },
  { level: 2, label: '二级标题' },
  { level: 3, label: '三级标题' },
  { level: 4, label: '四级标题' },
  { level: 5, label: '五级标题' },
  { level: 6, label: '六级标题' },
]

function insertHeading(h: { level: number; label: string }) {
  const prefix = '\n' + '#'.repeat(h.level) + ' '
  onInsertMarkdown(prefix, '', h.label)
}

// ── Code block dropdown ───────────────────────────────────
const showCodeMenu = ref(false)
const codeDropdownRef = ref<HTMLElement | null>(null)

const codeLanguages = [
  { id: '', label: '纯文本' },
  { id: 'javascript', label: 'JavaScript' },
  { id: 'typescript', label: 'TypeScript' },
  { id: 'python', label: 'Python' },
  { id: 'java', label: 'Java' },
  { id: 'go', label: 'Go' },
  { id: 'rust', label: 'Rust' },
  { id: 'c', label: 'C' },
  { id: 'cpp', label: 'C++' },
  { id: 'css', label: 'CSS' },
  { id: 'html', label: 'HTML' },
  { id: 'sql', label: 'SQL' },
  { id: 'bash', label: 'Bash' },
  { id: 'json', label: 'JSON' },
  { id: 'yaml', label: 'YAML' },
  { id: 'markdown', label: 'Markdown' },
]

function insertCodeBlock(lang: string) {
  const before = '\n```' + lang + '\n'
  onInsertMarkdown(before, '\n```\n', '代码')
}

// ── List dropdown ──────────────────────────────────────────
const showListMenu = ref(false)
const listDropdownRef = ref<HTMLElement | null>(null)

const listTypes = [
  { id: 'unordered', label: '无序列表', before: '\n- ', after: '', placeholder: '列表项' },
  { id: 'ordered', label: '有序列表', before: '\n1. ', after: '', placeholder: '列表项' },
  { id: 'task', label: '任务列表', before: '\n- [ ] ', after: '', placeholder: '待办项' },
]

function insertList(item: { id: string; before: string; after: string; placeholder: string }) {
  onInsertMarkdown(item.before, item.after, item.placeholder)
}

// ── More formats dropdown ──────────────────────────────────
const showMoreMenu = ref(false)
const moreDropdownRef = ref<HTMLElement | null>(null)

const moreItems = [
  { icon: '<>', label: '行内代码', before: '`', after: '`', placeholder: '代码' },
  { icon: '⊞', label: '表格', before: '\n| 列1 | 列2 |\n| --- | --- |\n| ', after: ' | ', placeholder: '内容' },
  { icon: '—', label: '分割线', before: '\n---\n', after: '', placeholder: '' },
  { icon: '❝', label: '引用', before: '\n> ', after: '', placeholder: '引用内容' },
]

// ── Close dropdowns on outside click ──────────────────────
onClickOutside(headingDropdownRef, () => { showHeadingMenu.value = false })
onClickOutside(codeDropdownRef, () => { showCodeMenu.value = false })
onClickOutside(listDropdownRef, () => { showListMenu.value = false })
onClickOutside(moreDropdownRef, () => { showMoreMenu.value = false })

// ── Action menu dropdown ──────────────────────────────────
const showActionMenu = ref(false)
const actionDropdownRef = ref<HTMLElement | null>(null)

onClickOutside(actionDropdownRef, () => { showActionMenu.value = false })

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
    onInsertMarkdown('![', `](${url})`, '图片描述')
  } catch (err) {
    toast.show(err instanceof Error ? err.message : '上传失败', 'error')
  } finally {
    input.value = ''
  }
}

// ── Selection / cursor ──────────────────────────────────────
const cursorRow = ref(1)
const cursorCol = ref(1)

function onSelectionChange(_state: { start: number; end: number; text: string; isCollapsed: boolean }) {
  // cursor position updated on scroll
}

function updateCursorPos() {
  const ta = textareaRef.value
  if (!ta) return
  const pos = ta.getCursorLineCol()
  cursorRow.value = pos.row
  cursorCol.value = pos.col
}

// ── Stats ───────────────────────────────────────────────────
const wordCount = computed(() => {
  return content.value.replace(/\s/g, '').length
})

const lineCount = computed(() => {
  if (!content.value) return 1
  return content.value.split('\n').length
})

// ── Outline ──────────────────────────────────────────────────
function extractOutline(text: string): TocItem[] {
  if (!text) return []
  const headingRe = /^(#{1,6})\s+(.+)$/gm
  const items: TocItem[] = []
  let index = 0
  for (const m of text.matchAll(headingRe)) {
    items.push({
      id: `heading-${index++}`,
      text: m[2]!.trim(),
      level: m[1]!.length as TocItem['level'],
    })
  }
  return items
}

const outlineItems = ref<TocItem[]>(extractOutline(content.value))
watch(() => content.value, (text) => {
  outlineItems.value = extractOutline(text)
})

function findNthHeading(text: string, targetIndex: number): number {
  let i = 0
  for (const m of text.matchAll(/^(#{1,6})\s+(.+)$/gm)) {
    if (i === targetIndex) return m.index!
    i++
  }
  return -1
}

function handleNavigateToHeading(index: number) {
  const pos = findNthHeading(content.value, index)
  if (pos < 0) return

  // scroll textarea
  const ta = textareaRef.value?.textareaEl
  if (ta) {
    const linesBefore = content.value.substring(0, pos).split('\n').length
    ta.scrollTop = Math.max(0, (linesBefore - 2) * 22)
  }

  // scroll preview
  if (viewMode.value !== 'edit') {
    previewRef.value?.scrollToHeading(index)
  }
}

// ── Link dialog ──────────────────────────────────────────────
const showLinkDialog = ref(false)
const linkText = ref('')
const linkUrl = ref('')
const linkTextInput = ref<HTMLInputElement | null>(null)
const linkUrlInput = ref<HTMLInputElement | null>(null)

watch(showLinkDialog, (val) => {
  if (val) {
    linkText.value = ''
    linkUrl.value = ''
    nextTick(() => linkTextInput.value?.focus())
  }
})

function confirmInsertLink() {
  if (!linkText.value.trim() || !linkUrl.value.trim()) return
  textareaRef.value?.insertAtCursor('[', `](${linkUrl.value.trim()})`, linkText.value.trim())
  closeLinkDialog()
}

function closeLinkDialog() {
  showLinkDialog.value = false
}

// ── Scroll sync ─────────────────────────────────────────────
interface TextareaExposed {
  syncScroll: (ratio: number) => void
  insertAtCursor: (before: string, after: string, placeholder: string) => void
  textareaEl: HTMLTextAreaElement | null
  selectionState: { start: number; end: number; text: string; isCollapsed: boolean }
  getCursorLineCol: () => { row: number; col: number }
}

const textareaRef = ref<TextareaExposed | null>(null)
const previewRef = ref<{ syncScroll: (ratio: number) => void; scrollToHeading: (index: number) => void } | null>(null)

let isTextareaDriven = false
let isPreviewDriven = false

function onTextareaScroll(ratio: number) {
  if (isPreviewDriven) return
  isTextareaDriven = true
  previewRef.value?.syncScroll(ratio)
  requestAnimationFrame(() => { isTextareaDriven = false })
  updateCursorPos()
}

function onPreviewScroll(ratio: number) {
  if (isTextareaDriven) return
  isPreviewDriven = true
  textareaRef.value?.syncScroll(ratio)
  requestAnimationFrame(() => { isPreviewDriven = false })
}

function onInsertMarkdown(before: string, after: string, placeholder: string) {
  textareaRef.value?.insertAtCursor(before, after, placeholder)
}

// ── Actions ─────────────────────────────────────────────────
async function handleSave() {
  await editor.save()
}

function handleClose() {
  emit('close')
}

function scrollToSettings() {
  const el = document.querySelector('.write-body')
  if (el) el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' })
}

onMounted(() => {})
</script>

<style scoped>
.editor-view {
  height: 100%;
  background: var(--surface-soft);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

/* ── Body ─────────────────────────────────────────────── */
.editor-body {
  --panel-width: calc(200px + 160px * min(var(--panel-ratio), 0.5) + max(0px, (100vw - 1092px) * max(var(--panel-ratio) - 0.5, 0) / 0.5));
  flex: 1;
  overflow: hidden;
  padding: var(--spacing-lg);
  padding-right: 64px;
  position: relative;
  transition: padding-right 0.25s var(--ease);
}

.editor-body:has(.slide-panel) {
  padding-right: calc(var(--panel-px, var(--panel-width, 280px)) + 72px);
}

/* ── Card-Panel Divider ────────────────────────────────── */
.card-divider {
  display: none;
  position: absolute;
  top: var(--spacing-lg);
  bottom: var(--spacing-lg);
  right: calc(var(--panel-px, var(--panel-width, 280px)) + 56px);
  z-index: 5;
}

.editor-body:has(.slide-panel) .card-divider {
  display: flex;
}

/* ── Single Card ──────────────────────────────────────── */
.editor-card {
  container-type: inline-size;
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--canvas);
  border-radius: var(--rounded-lg);
  box-shadow: rgba(15, 15, 15, 0.08) 0px 4px 12px 0px;
  overflow: hidden;
  will-change: width;
  transform: translateZ(0);
}

/* ── Toolbar ──────────────────────────────────────────── */
.card-toolbar {
  height: 44px;
  flex-shrink: 0;
  border-bottom: 1px solid var(--hairline);
  padding: 0 var(--spacing-md);
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
}

.toolbar-left-group {
  display: flex;
  align-items: center;
}

.toolbar-left,
.toolbar-center,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 2px;
}

.toolbar-left {
  flex-shrink: 0;
  padding-right: var(--spacing-sm);
  border-right: 1px solid var(--hairline);
  margin-right: var(--spacing-sm);
}

.toolbar-center {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 2px;
  padding-right: var(--spacing-sm);
  border-right: 1px solid var(--hairline);
  margin-right: var(--spacing-sm);
}

.toolbar-view-toggle {
  /* centered by grid column */
}

.view-toggle {
  display: flex;
  gap: 4px;
}

.view-btn {
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  font-weight: var(--weight-medium);
  color: var(--steel);
  background: transparent;
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-full);
  padding: var(--spacing-xs) var(--spacing-md);
  cursor: pointer;
  transition: all 0.15s var(--ease);
  white-space: nowrap;
}

.view-btn:hover { color: var(--ink); border-color: var(--hairline-strong); }

.view-btn.active {
  color: var(--on-dark);
  background: var(--ink-deep);
  border-color: var(--ink-deep);
}

.toolbar-right {
  gap: var(--spacing-xs);
  flex-shrink: 0;
  justify-self: end;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  padding: 0;
  border: none;
  border-radius: var(--rounded-sm);
  background: transparent;
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: var(--caption);
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.12s var(--ease);
}

.toolbar-btn:hover {
  background: var(--hairline-soft);
}

.btn-icon {
  font-size: 15px;
  font-weight: var(--weight-semibold);
  line-height: 1;
}

.btn-icon-svg {
  flex-shrink: 0;
  color: var(--ink);
}

.toolbar-btn--labeled {
  width: auto;
  padding: 0 8px;
  gap: 4px;
}

.toolbar-btn--labeled .btn-label {
  font-size: var(--caption);
  color: var(--slate);
}

.toolbar-btn--labeled .btn-arrow {
  font-size: 20px;
  color: var(--slate);
  margin-left: -2px;
  line-height: 1;
}

.dropdown-item-icon {
  font-size: 14px;
  width: 20px;
  text-align: center;
  flex-shrink: 0;
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  background: var(--hairline);
  margin: 0 6px;
  flex-shrink: 0;
}

/* ── Dropdown menus ────────────────────────────────────── */
.dropdown-wrap {
  position: relative;
}

.toolbar-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  min-width: 130px;
  padding: 4px;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-md);
  box-shadow: rgba(15, 15, 15, 0.08) 0px 4px 12px 0px;
  z-index: 50;
  max-height: 280px;
  overflow-y: auto;
}

.toolbar-dropdown--right {
  left: auto;
  right: 0;
}

.toolbar-dropdown--wide {
  min-width: 140px;
  column-count: 2;
  column-gap: 4px;
}

.toolbar-dropdown--wide .dropdown-item {
  break-inside: avoid;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  padding: 6px 10px;
  border: none;
  border-radius: var(--rounded-xs);
  background: transparent;
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: var(--caption);
  text-align: left;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.1s var(--ease);
}

.dropdown-item:hover {
  background: var(--hairline-soft);
}

.dropdown-item-prefix {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--muted);
  width: 48px;
  flex-shrink: 0;
}

/* ── Card Status Bar ───────────────────────────────────── */
.card-statusbar {
  height: 36px;
  flex-shrink: 0;
  border-top: 1px solid var(--hairline);
  padding: 0 var(--spacing-md);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.statusbar-left,
.statusbar-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.statusbar-stat {
  font-size: var(--caption);
  color: var(--steel);
  white-space: nowrap;
}

.statusbar-divider {
  color: var(--hairline-strong);
  font-size: var(--caption);
}

.statusbar-timestamp {
  color: var(--muted);
  font-size: var(--caption);
  white-space: nowrap;
}

.statusbar-btn {
  height: 28px;
  padding: 0 var(--spacing-sm);
  border: none;
  border-radius: var(--rounded-sm);
  font-family: var(--font-sans);
  font-size: var(--caption);
  font-weight: var(--weight-medium);
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.12s var(--ease), color 0.12s var(--ease);
}

.statusbar-btn--save {
  background: var(--surface);
  color: var(--steel);
}

.statusbar-btn--save:hover:not(:disabled) {
  background: var(--hairline-soft);
  color: var(--ink);
}

.statusbar-btn--publish {
  background: var(--primary);
  color: var(--on-primary);
}

.statusbar-btn--publish:hover:not(:disabled) {
  background: var(--primary-pressed);
}

.statusbar-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ── Content Area ─────────────────────────────────────── */
.card-content {
  flex: 1;
  display: flex;
  flex-direction: row;
  overflow: hidden;
}

.card-content--single > :only-child {
  width: 100%;
}

.pane-edit {
  flex: 1;
  background: var(--canvas);
  overflow: hidden;
}

.pane-edit--centered {
  display: flex;
  justify-content: center;
}

.pane-edit-inner {
  width: 100%;
  height: 100%;
}

.pane-edit--centered .pane-edit-inner {
  max-width: 720px;
}

.pane-preview {
  flex: 1;
  background: var(--canvas);
  overflow: hidden;
}

.pane-preview--split {
  background: var(--surface);
}

.static-divider {
  width: 1px;
  flex-shrink: 0;
  background: var(--hairline);
}

/* ── Link Dialog ──────────────────────────────────────── */
.link-dialog-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.2);
  z-index: 300;
  display: flex;
  align-items: center;
  justify-content: center;
}

.link-dialog {
  background: var(--canvas);
  border-radius: var(--rounded-lg);
  box-shadow: rgba(15, 15, 15, 0.16) 0px 16px 48px -8px;
  width: 400px;
  max-width: calc(100vw - 40px);
  overflow: hidden;
}

.link-dialog-header {
  font-size: var(--body-sm);
  font-weight: var(--weight-semibold);
  color: var(--ink);
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--hairline);
}

.link-dialog-body {
  padding: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.link-dialog-input {
  height: 40px;
  padding: 0 var(--spacing-md);
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  color: var(--ink);
  background: var(--surface);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-md);
  outline: none;
  transition: border-color 0.15s var(--ease);
}

.link-dialog-input::placeholder { color: var(--muted); }
.link-dialog-input:focus { border-color: var(--primary); }

.link-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-xs);
  padding: var(--spacing-md) var(--spacing-lg);
  border-top: 1px solid var(--hairline);
}

.link-dialog-btn {
  height: 36px;
  padding: 0 var(--spacing-md);
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  font-weight: var(--weight-medium);
  border: none;
  border-radius: var(--rounded-md);
  cursor: pointer;
  transition: background 0.12s var(--ease), opacity 0.12s var(--ease);
}

.link-dialog-btn--cancel {
  background: var(--surface);
  color: var(--steel);
}

.link-dialog-btn--cancel:hover {
  background: var(--hairline-soft);
}

.link-dialog-btn--confirm {
  background: var(--primary);
  color: var(--on-primary);
}

.link-dialog-btn--confirm:hover:not(:disabled) {
  background: var(--primary-pressed);
}

.link-dialog-btn--confirm:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* Link dialog transition */
.link-dialog-enter-active,
.link-dialog-leave-active {
  transition: opacity 0.15s var(--ease);
}

.link-dialog-enter-active .link-dialog,
.link-dialog-leave-active .link-dialog {
  transition: transform 0.15s var(--ease);
}

.link-dialog-enter-from,
.link-dialog-leave-to {
  opacity: 0;
}

.link-dialog-enter-from .link-dialog {
  transform: scale(0.96);
}

.link-dialog-leave-to .link-dialog {
  transform: scale(0.96);
}

/* ── Container Queries（卡片宽度响应，面板挤压时也会触发）── */
@container (max-width: 900px) {
  .toolbar-btn--labeled .btn-label { display: none; }
  .toolbar-btn--labeled {
    padding: 0 6px;
    gap: 0;
  }
  .toolbar-divider { margin: 0 4px; }
  .view-btn { padding: 4px 8px; font-size: var(--caption); }
}

@container (max-width: 768px) {
  .card-toolbar {
    display: flex;
    overflow-x: auto;
    -ms-overflow-style: none;
    scrollbar-width: none;
  }
  .card-toolbar::-webkit-scrollbar { display: none; }
  .toolbar-left-group { flex-shrink: 0; }
  .toolbar-view-toggle { flex-shrink: 0; }
  .toolbar-right { flex-shrink: 0; margin-left: 0; }
  .toolbar-btn { width: 28px; height: 28px; }
  .btn-icon { font-size: 14px; }
  .toolbar-btn--labeled {
    width: 28px;
    padding: 0;
    justify-content: center;
  }
  .toolbar-btn--labeled .btn-arrow { display: none; }
  .toolbar-divider { margin: 0 2px; }
  .toolbar-left,
  .toolbar-center {
    padding-right: 4px;
    margin-right: 4px;
  }
  .view-btn { padding: 3px 6px; font-size: 12px; }
}

@container (max-width: 640px) {
  .card-toolbar { padding: 0 var(--spacing-xs); display: flex; }
  .toolbar-left-group { flex-shrink: 0; }
  .toolbar-view-toggle { flex-shrink: 0; }
  .toolbar-right { flex-shrink: 0; margin-left: auto; }
  .toolbar-left { border-right: none; margin-right: 0; padding-right: 0; }
  .toolbar-center { border-right: none; margin-right: 0; padding-right: 0; }
  .view-btn { padding: 3px 6px; font-size: 11px; }
  .statusbar-stat,
  .statusbar-divider,
  .statusbar-timestamp { display: none; }
}

/* ── Media Queries（视口级布局）────────────────────────── */
@media (max-width: 900px) {
  .editor-body { --panel-width: calc(180px + 80px * min(var(--panel-ratio), 0.5) + max(0px, (100vw - 897px) * max(var(--panel-ratio) - 0.5, 0) / 0.5)); }
  .editor-body:has(.slide-panel) { padding-right: calc(var(--panel-px, var(--panel-width)) + 72px); }
}

@media (max-width: 768px) {
  .editor-body { --panel-width: calc(160px + 40px * min(var(--panel-ratio), 0.5) + max(0px, (100vw - 772px) * max(var(--panel-ratio) - 0.5, 0) / 0.5)); }
  .editor-body:has(.slide-panel) { padding-right: calc(var(--panel-px, var(--panel-width)) + 72px); }
}

@media (max-width: 640px) {
  .editor-body { padding: var(--spacing-xs); padding-right: var(--spacing-xs); }
  .editor-body:has(.slide-panel) { padding-right: calc(var(--panel-px, var(--panel-width)) + 56px); }
  .card-divider { right: calc(var(--panel-px, var(--panel-width)) + 56px); }
  .pane-edit--centered .pane-edit-inner { max-width: none; }
}
</style>
