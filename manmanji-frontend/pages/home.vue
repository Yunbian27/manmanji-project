<!--
  pages/home.vue — 个人知识库主页
  语雀式 2 栏布局：左侧栏满高 + 内容区自有顶栏
  1:1 复刻 sujian-demo.html
-->
<template>
  <div class="body">
    <!-- ===== Left Sidebar ===== -->
    <aside class="sidebar">
      <SidebarHeader />

      <div class="sidebar-fixed">
        <div class="group-manage-wrap">
          <button class="sidebar-action-btn" @click="groupModalVisible = !groupModalVisible">
            <IconLucideFolder class="icon-sm" />
            分组管理
          </button>
          <div v-if="groupModalVisible" ref="groupPopoverRef" class="group-modal-popover" @click.stop>
            <div class="group-modal-card">
              <div class="group-modal-header">
                <h3 class="group-modal-title">分组管理</h3>
                <button class="group-modal-close" @click="groupModalVisible = false" aria-label="关闭">
                  <IconLucideX class="icon-md" />
                </button>
              </div>
              <input
                v-model="groupSearch"
                class="group-modal-search-input"
                placeholder="请输入文字搜索，Enter键可添加自定义分组（最多50字）"
                maxlength="50"
                @keydown.enter.prevent="handleGroupSearchEnter"
              />
              <div class="group-modal-grid">
                <button
                  v-for="g in filteredModalGroups"
                  :key="g.id"
                  class="group-modal-chip"
                  :class="{ active: activeGroupIds.includes(g.id) }"
                  @click="toggleGroup(g)"
                >
                  {{ g.name }}
                  <span class="group-modal-chip-remove" @click.stop="handleDeleteGroup(g.id)">
                    <IconLucideX class="icon-xs" />
                  </span>
                </button>
                <div v-if="filteredModalGroups.length === 0" class="group-modal-empty">暂无匹配分组，按 Enter 创建</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="sidebar-scroll" ref="sidebarScrollRef">
        <div class="sidebar-actions">
          <button v-for="item in statusItems" :key="item.key"
            class="sidebar-action-btn"
            :class="{ active: activeStatus === item.key }"
            @click="activeStatus = item.key"
          >
            <component :is="iconMap[item.iconKey]" class="icon-sm" />
            {{ item.label }}
            <span class="sidebar-action-count">{{ item.count }}</span>
          </button>
        </div>

        <div class="note-list-header">文章列表</div>
        <div class="note-list">
          <div
            v-for="article in filteredNotes"
            :key="article.id"
            class="note-item"
            :class="{ active: selectedArticle?.id === article.id }"
            @click="selectArticle(article)"
          >
            <div class="note-item-title">{{ article.title }}</div>
          </div>
        </div>
        <div v-if="searchQuery && filteredNotes.length === 0" class="note-empty">未找到相关笔记</div>
      </div>

      <div class="sidebar-footer">
        <button class="sidebar-community-btn" @click="router.push('/community')">
          <IconLucideGlobe class="icon-sm" />
          探索社区
        </button>
      </div>
    </aside>

    <!-- ===== Content Area ===== -->
    <main class="content-area">
      <div class="content-topbar">
        <div class="content-topbar-left">
          <div class="content-topbar-search-wrap">
            <IconLucideSearch class="content-topbar-search-icon" />
            <input
              class="content-topbar-search"
              v-model="searchQuery"
              type="text"
              placeholder="搜索笔记..."
            />
          </div>
        </div>
        <div class="content-topbar-center">
          <div class="center-title-row">
            <span class="center-title-text">{{ selectedArticle?.title || '' }}</span>
            <button class="center-detail-btn" @click="viewDetail">
              <IconLucideExternalLink class="icon-md" />
            </button>
          </div>
          <span class="center-meta-text" v-if="selectedArticle">{{ selectedArticle.updateTime }}</span>
        </div>
        <div class="content-topbar-right">
          <button class="btn-primary" @click="onPublish">发布文章</button>
        </div>
      </div>

      <div class="content-body" ref="contentBodyRef">
        <div v-if="selectedArticle" class="content-inner">
          <div v-if="contentLoading" class="content-loading">加载中...</div>
          <div v-else class="markdown-body">
            <div class="article-content" v-html="renderedHtml" />
          </div>
        </div>
        <div v-else class="content-empty">
          <p>选择左侧笔记开始阅读</p>
        </div>
      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import type { StudyArticle, GroupVO } from '~/types'
import type { Component } from 'vue'
import { useMarkdownRenderer } from '~/composables/useMarkdownRenderer'
import { onClickOutside } from '@vueuse/core'

definePageMeta({ layout: 'blank', middleware: 'role-guard' })

import IconLucideFolder from '~icons/lucide/folder'
import IconLucideX from '~icons/lucide/x'
import IconLucideList from '~icons/lucide/list'
import IconLucideGlobe from '~icons/lucide/globe'
import IconLucideLock from '~icons/lucide/lock'
import IconLucideBookmark from '~icons/lucide/bookmark'
import IconLucideSearch from '~icons/lucide/search'
import IconLucideExternalLink from '~icons/lucide/external-link'

const authStore = useAuthStore()
const router = useRouter()

// --- Data ---
const articles = ref<StudyArticle[]>([])
const selectedArticle = ref<StudyArticle | null>(null)
const activeStatus = ref('ALL')
const activeTagNames = ref<string[]>([])
const activeGroupIds = ref<number[]>([])
const groups = ref<GroupVO[]>([])
const searchQuery = ref('')
const groupModalVisible = ref(false)
const groupSearch = ref('')
const groupPopoverRef = ref<HTMLElement | null>(null)

onClickOutside(groupPopoverRef, () => { groupModalVisible.value = false })

// --- Refs for scrollbar ---
const sidebarScrollRef = ref<HTMLElement | null>(null)
const contentBodyRef = ref<HTMLElement | null>(null)

// --- Compute ---
const iconMap: Record<string, Component> = {
  ALL: IconLucideList,
  PUBLIC: IconLucideGlobe,
  PRIVATE: IconLucideLock,
  BOOKMARKED: IconLucideBookmark,
}

const statusItems = computed(() => [
  {
    key: 'ALL',
    label: '全部',
    iconKey: 'ALL',
    count: articles.value.length,
  },
  {
    key: 'PUBLIC',
    label: '公开文章',
    iconKey: 'PUBLIC',
    count: articles.value.filter(a => a.visibility === 'PUBLIC').length,
  },
  {
    key: 'PRIVATE',
    label: '私密笔记',
    iconKey: 'PRIVATE',
    count: articles.value.filter(a => a.visibility === 'PRIVATE').length,
  },
  {
    key: 'BOOKMARKED',
    label: '收藏',
    iconKey: 'BOOKMARKED',
    count: articles.value.filter(a => a.status === 'BOOKMARKED').length,
  },
])

const filteredNotes = computed(() => {
  let list = articles.value
  if (activeStatus.value === 'PUBLIC') {
    list = list.filter(a => a.visibility === 'PUBLIC')
  } else if (activeStatus.value === 'PRIVATE') {
    list = list.filter(a => a.visibility === 'PRIVATE')
  } else if (activeStatus.value === 'BOOKMARKED') {
    list = list.filter(a => a.status === 'BOOKMARKED')
  }
  if (activeTagNames.value.length) {
    list = list.filter(a => activeTagNames.value.some(t => a.tags?.includes(t)))
  }
  if (activeGroupIds.value.length) {
    list = list.filter(a => activeGroupIds.value.some(gid => a.groupIds?.includes(gid)))
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(a => a.title.toLowerCase().includes(q))
  }
  return list
})

const filteredModalGroups = computed(() => {
  const q = groupSearch.value.trim().toLowerCase()
  if (!q) return groups.value
  return groups.value.filter(g => g.name.toLowerCase().includes(q))
})

const articleRawContent = ref('')
const { renderedHtml } = useMarkdownRenderer(articleRawContent)
const contentLoading = ref(false)

// --- Methods ---
async function handleGroupSearchEnter() {
  const q = groupSearch.value.trim()
  if (!q) return
  if (!groups.value.some(g => g.name === q)) {
    try {
      const newId = await useArticle().createGroup(q)
      groups.value.push({ id: newId, name: q })
    } catch { /* 后端报错则忽略 */ }
  }
  groupSearch.value = ''
}

async function handleDeleteGroup(id: number) {
  try {
    await useArticle().deleteGroup(id)
    const removed = groups.value.find(g => g.id === id)
    groups.value = groups.value.filter(g => g.id !== id)
    if (removed) {
      const idx = activeGroupIds.value.indexOf(removed.id)
      if (idx >= 0) activeGroupIds.value.splice(idx, 1)
    }
  } catch {
    // 后端报错则忽略
  }
}
function toggleTag(tag: string) {
  const idx = activeTagNames.value.indexOf(tag)
  if (idx >= 0) {
    activeTagNames.value.splice(idx, 1)
  } else {
    activeTagNames.value.push(tag)
  }
}

function toggleGroup(g: GroupVO) {
  const idx = activeGroupIds.value.indexOf(g.id)
  if (idx >= 0) {
    activeGroupIds.value.splice(idx, 1)
  } else {
    activeGroupIds.value.push(g.id)
  }
}

async function selectArticle(article: StudyArticle) {
  selectedArticle.value = article
  contentLoading.value = true
  try {
    const vo = await useArticle().getArticle(article.id)
    articleRawContent.value = vo.content
  } catch {
    articleRawContent.value = ''
  } finally {
    contentLoading.value = false
  }
}

function viewDetail() {
  if (!selectedArticle.value) return
  window.open(`/write?articleId=${selectedArticle.value.id}`, '_blank')
}

function onPublish() {
  window.open('/write', '_blank')
}

// --- Modal close cleanup ---
watch(groupModalVisible, (v) => {
  if (!v) {
    groupSearch.value = ''
  }
})

// --- Init ---
onMounted(async () => {
  const { listStudyArticles, listGroups } = useArticle()
  const [arts, gs] = await Promise.all([listStudyArticles(), listGroups().catch(() => [] as GroupVO[])])
  articles.value = arts
  groups.value = gs
  if (articles.value.length > 0 && articles.value[0]) {
    await selectArticle(articles.value[0])
  }
})

// --- Auto-hide scrollbar ---
function initScrollbar(el: HTMLElement) {
  let timer: ReturnType<typeof setTimeout>
  el.addEventListener('scroll', () => {
    if (!timer) el.classList.add('scrolling')
    clearTimeout(timer)
    timer = setTimeout(() => {
      el.classList.remove('scrolling')
      timer = null as unknown as ReturnType<typeof setTimeout>
    }, 400)
  })
}

onMounted(() => {
  if (sidebarScrollRef.value) initScrollbar(sidebarScrollRef.value)
  if (contentBodyRef.value) initScrollbar(contentBodyRef.value)
})

</script>

<style scoped>
/* ===== Root ===== */
.body {
  display: flex;
  min-height: 100vh;
  background: var(--surface-soft);
}

/* ===== Left Sidebar ===== */
.sidebar {
  width: var(--sidebar-width);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
  z-index: 1;
  background: var(--surface);
  border-right: 1px solid var(--hairline);
}

/* Fixed top zone */
.sidebar-fixed {
  flex-shrink: 0;
  padding: 0 var(--spacing-md);
}

.sidebar-action-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  width: 100%;
  padding: 6px var(--spacing-sm);
  border: none;
  border-radius: var(--rounded-sm);
  background: transparent;
  color: var(--charcoal);
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  font-weight: var(--weight-regular);
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s var(--ease), color 0.15s var(--ease);
  text-align: left;
}
.sidebar-action-btn:hover { background: var(--hairline-soft); color: var(--ink); }
.sidebar-action-btn.active {
  background: var(--canvas);
  color: var(--ink);
  font-weight: var(--weight-medium);
  box-shadow: rgba(15, 15, 15, 0.06) 0px 1px 3px 0px;
}
.sidebar-action-btn svg { flex-shrink: 0; }
.sidebar-action-btn.active svg { opacity: 1; }

.sidebar-action-count {
  margin-left: auto;
  font-size: var(--caption);
  color: var(--muted);
}

/* Scrollable zone */
.sidebar-scroll {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  padding: 0 var(--spacing-md);
}
.sidebar-actions {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: var(--spacing-xs) 0;
}

/* Note list */
.note-list-header {
  position: sticky;
  top: 0;
  z-index: 1;
  padding: var(--spacing-sm) var(--spacing-sm) var(--spacing-xs);
  background: var(--surface);
  font-size: var(--caption);
  font-weight: var(--weight-semibold);
  color: var(--muted);
  letter-spacing: 0.04em;
}
.note-list { flex: 1; }
.note-item {
  padding: 6px var(--spacing-sm);
  border-radius: var(--rounded-sm);
  cursor: pointer;
  transition: background 0.15s var(--ease), box-shadow 0.15s var(--ease);
}
.note-item:hover { background: var(--hairline-soft); }
.note-item.active {
  background: var(--canvas);
  box-shadow: rgba(15, 15, 15, 0.06) 0px 1px 3px 0px;
}
.note-item-title {
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  font-weight: var(--weight-regular);
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.note-item.active .note-item-title { font-weight: var(--weight-medium); }
.note-empty {
  display: flex;
  justify-content: center;
  padding: var(--spacing-xxl) var(--spacing-md);
  font-size: var(--body-sm);
  color: var(--muted);
}

/* Sidebar footer */
.sidebar-footer {
  flex-shrink: 0;
  padding: var(--spacing-sm) var(--spacing-md);
  border-top: 1px solid var(--hairline-soft);
}

.sidebar-community-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  width: 100%;
  padding: 6px var(--spacing-sm);
  border: none;
  border-radius: var(--rounded-sm);
  background: transparent;
  color: var(--charcoal);
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  font-weight: var(--weight-regular);
  cursor: pointer;
  transition: background 0.15s var(--ease), color 0.15s var(--ease);
}
.sidebar-community-btn:hover {
  background: var(--hairline-soft);
  color: var(--ink);
}
.sidebar-community-btn svg { flex-shrink: 0; }

/* ===== Content Area ===== */
.content-area {
  flex: 1;
  min-width: 0;
  background: var(--canvas);
}

/* Content topbar */
.content-topbar {
  position: sticky;
  top: 0;
  z-index: 1;
  height: var(--topbar-height);
  display: flex;
  align-items: center;
  padding: 0 var(--spacing-xxl);
  border-bottom: 1px solid var(--hairline);
  background: var(--canvas);
}
.content-topbar-left { flex: 1; display: flex; align-items: center; }
.content-topbar-search-wrap {
  position: relative;
  display: flex;
  align-items: center;
}
.content-topbar-search-icon {
  position: absolute;
  left: 10px;
  pointer-events: none;
  color: var(--muted);
}
.content-topbar-search {
  width: 300px;
  max-width: 100%;
  height: 36px;
  padding: var(--spacing-xs) var(--spacing-sm) var(--spacing-xs) 32px;
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-md);
  background: var(--surface);
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  font-weight: var(--weight-regular);
  outline: none;
  transition: border-color 0.15s var(--ease);
}
.content-topbar-search::placeholder { color: var(--muted); }
.content-topbar-search:focus { border-color: var(--primary); }
.content-topbar-center {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0;
  max-width: 400px;
  min-width: 0;
}

.center-title-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  max-width: 100%;
  min-width: 0;
}

.center-title-text {
  font-size: var(--body-md);
  font-weight: var(--weight-medium);
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.center-detail-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  border: none;
  border-radius: var(--rounded-sm);
  background: transparent;
  color: var(--steel);
  cursor: pointer;
  flex-shrink: 0;
  position: relative;
  transition: background 0.15s var(--ease), color 0.15s var(--ease);
}
.center-detail-btn:hover {
  background: var(--hairline-soft);
  color: var(--ink);
}

.center-detail-btn::after {
  content: '查看详情';
  position: absolute;
  bottom: calc(100% + 6px);
  left: 50%;
  transform: translateX(-50%);
  padding: 4px 8px;
  background: var(--ink);
  color: var(--on-dark);
  font-family: var(--font-sans);
  font-size: var(--caption);
  border-radius: var(--rounded-xs);
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.15s var(--ease);
  box-shadow: rgba(15, 15, 15, 0.08) 0px 4px 12px 0px;
}
.center-detail-btn:hover::after {
  opacity: 1;
}

.center-meta-text {
  font-size: var(--caption);
  color: var(--muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}
.content-topbar-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--spacing-md);
  flex-shrink: 0;
}

@media (max-width: 1023px) {
  .content-topbar { padding: 0 var(--spacing-md); }
  .content-topbar-search { width: 200px; }
  .content-topbar-center { display: none; }
}

@media (max-width: 599px) {
  .content-topbar-search { width: 140px; }
  .btn-primary { padding: 8px 14px; font-size: var(--caption); }
}
/* Button primary */
.btn-primary {
  padding: 10px 18px;
  border: none;
  border-radius: var(--rounded-md);
  background: var(--primary);
  color: var(--on-primary);
  font-family: var(--font-sans);
  font-size: var(--button-md);
  font-weight: var(--weight-medium);
  line-height: var(--leading-button);
  cursor: pointer;
  white-space: nowrap;
  flex-shrink: 0;
  transition: background 0.15s var(--ease);
}
.btn-primary:hover { background: var(--primary-pressed); }

/* Content body */
.content-body {
  display: flex;
  justify-content: center;
  padding: var(--spacing-xxxl) var(--spacing-xxl);
}
.content-inner {
  width: 100%;
  display: flex;
  justify-content: center;
  background: var(--canvas);
  border-radius: var(--rounded-lg);
  box-shadow: rgba(15, 15, 15, 0.04) 0px 1px 2px 0px;
  padding: var(--spacing-xl);
  height: fit-content;
}
.content-inner .markdown-body {
  width: 100%;
  max-width: max(calc(50% - 24.5px), calc(50vw - 92.5px));
}
.content-empty,
.content-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--muted);
  font-size: var(--body-md);
}

/* ===== Group popover ===== */
.group-manage-wrap { position: relative; }

.group-modal-popover {
  position: absolute; left: 100%; top: 0; z-index: var(--z-modal);
  margin-left: var(--spacing-sm);
}

.group-modal-card {
  width: 480px;
  max-height: 400px;
  display: flex;
  flex-direction: column;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-lg);
  padding: var(--spacing-lg);
  box-shadow: rgba(15, 15, 15, 0.16) 0px 16px 48px -8px;
}

.group-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-md);
  flex-shrink: 0;
}

.group-modal-title {
  font-family: var(--font-sans);
  font-size: var(--heading-5);
  font-weight: var(--weight-semibold);
  color: var(--ink);
  margin: 0;
}

.group-modal-close {
  width: 28px; height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--rounded-sm);
  border: none;
  background: transparent;
  color: var(--steel);
  cursor: pointer;
  transition: background-color 0.2s var(--ease), color 0.2s var(--ease);
}
.group-modal-close:hover { background: var(--hairline-soft); color: var(--ink); }

.group-modal-search-input {
  width: 100%;
  height: 36px;
  padding: 0 var(--spacing-md);
  margin-bottom: var(--spacing-md);
  border: 1px solid var(--hairline-strong);
  border-radius: var(--rounded-md);
  background: var(--canvas);
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  outline: none;
  flex-shrink: 0;
  transition: border-color 0.2s var(--ease);
}
.group-modal-search-input::placeholder { color: var(--muted); }
.group-modal-search-input:focus { border-color: var(--primary); }

.group-modal-grid {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
  align-content: flex-start;
  min-height: 0;
}

.group-modal-chip {
  display: inline-flex; align-items: center;
  padding: var(--spacing-xs) var(--spacing-md);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-full);
  background: transparent;
  color: var(--steel);
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  font-weight: var(--weight-medium);
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.2s var(--ease), border-color 0.2s var(--ease), color 0.2s var(--ease);
}
.group-modal-chip:hover {
  background: var(--hairline-soft);
  border-color: var(--hairline-strong);
  color: var(--ink);
}
.group-modal-chip.active {
  background: var(--ink-deep);
  border-color: var(--ink-deep);
  color: var(--on-dark);
}

.group-modal-chip-remove {
  display: inline-flex;
  align-items: center;
  margin-left: 6px;
  padding: 0;
  border: none;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
  transition: color 0.2s var(--ease);
}
.group-modal-chip-remove:hover { color: var(--semantic-error); }
.group-modal-chip.active .group-modal-chip-remove { color: var(--on-dark-muted); }
.group-modal-chip.active .group-modal-chip-remove:hover { color: var(--on-dark); }

.group-modal-empty {
  width: 100%;
  text-align: center;
  font-size: var(--body-sm);
  color: var(--muted);
  padding: var(--spacing-md) 0;
}

/* ===== Scrollbar auto-hide（仅侧边栏）===== */
.sidebar-scroll::-webkit-scrollbar { width: 5px; }
.sidebar-scroll::-webkit-scrollbar-track { background: transparent; }
.sidebar-scroll::-webkit-scrollbar-thumb { background: transparent; border-radius: 3px; }
.sidebar-scroll.scrolling::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.15); }
.sidebar-scroll::-webkit-scrollbar-thumb:hover { background: rgba(0,0,0,0.25); }

/* ===== Responsive ===== */
@media (max-width: 767px) {
  .sidebar { display: none; }
  .content-body { padding: var(--spacing-md); }
  .content-inner { padding: var(--spacing-md); }
}
</style>
