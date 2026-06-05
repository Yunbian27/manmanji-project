<!--
  pages/home.vue — 个人知识库主页
  语雀式 2 栏布局：左侧栏满高 + 内容区自有顶栏
  1:1 复刻 sujian-demo.html
-->
<template>
  <div class="body">
    <!-- ===== Left Sidebar ===== -->
    <aside class="sidebar">
      <div class="sidebar-logo">
        <img src="/favicon.svg" alt="慢慢记" class="sidebar-logo-mark" />
      慢慢记
      </div>

      <div class="sidebar-fixed">
        <div class="tag-filter-wrap">
          <button class="sidebar-action-btn" @click="popoverVisible = !popoverVisible">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
            分组管理
          </button>
          <div class="tag-popover" :class="{ visible: popoverVisible }">
            <span
              v-for="tag in allTags"
              :key="tag"
              class="tag-popover-chip"
              :class="{ selected: activeTags.includes(tag) }"
              @click="toggleTag(tag)"
            >{{ tag }}</span>
          </div>
        </div>
      </div>

      <div class="sidebar-scroll" ref="sidebarScrollRef">
        <div class="sidebar-actions">
          <template v-for="item in statusItems" :key="item.key">
            <!-- 已发布: collapsible dropdown -->
            <template v-if="item.key === 'PUBLISHED'">
              <button
                class="sidebar-action-btn"
                :class="{ active: activeStatus === 'PUBLISHED' && !publishedSubFilter }"
                @click="togglePublished"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" v-html="item.icon" />
                {{ item.label }}
                <span class="sidebar-action-chevron" :class="{ expanded: publishedExpanded }">
                  <svg width="10" height="6" viewBox="0 0 10 6" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M1 1L5 5L9 1"/></svg>
                </span>
                <span class="sidebar-action-count">{{ item.count }}</span>
              </button>
              <div v-if="publishedExpanded" class="sidebar-sub-items">
                <button
                  class="sidebar-sub-btn"
                  :class="{ active: publishedSubFilter === 'PUBLIC' }"
                  @click="selectPublishedSub('PUBLIC')"
                >
                  公开文章
                  <span class="sidebar-action-count">{{ item.publicCount }}</span>
                </button>
                <button
                  class="sidebar-sub-btn"
                  :class="{ active: publishedSubFilter === 'PRIVATE' }"
                  @click="selectPublishedSub('PRIVATE')"
                >
                  私密笔记
                  <span class="sidebar-action-count">{{ item.privateCount }}</span>
                </button>
              </div>
            </template>
            <!-- 其他菜单: 原样不动 -->
            <button
              v-else
              class="sidebar-action-btn"
              :class="{ active: activeStatus === item.key }"
              @click="activeStatus = item.key"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" v-html="item.icon" />
              {{ item.label }}
              <span class="sidebar-action-count">{{ item.count }}</span>
            </button>
          </template>
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
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M2 12h20"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
          探索社区
        </button>
      </div>
    </aside>

    <!-- ===== Content Area ===== -->
    <main class="content-area">
      <div class="content-topbar">
        <div class="content-topbar-left">
          <div class="content-topbar-search-wrap">
            <svg class="content-topbar-search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
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
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/></svg>
            </button>
          </div>
          <span class="center-meta-text" v-if="selectedArticle">{{ selectedArticle.updatedAt }}</span>
        </div>
        <div class="content-topbar-right">
          <button class="btn-primary" @click="onPublish">发布文章</button>
          <div ref="avatarContainer" class="avatar-wrapper">
            <div class="topbar-avatar" @click="showDropdown = !showDropdown">
              {{ avatarChar }}
            </div>
            <Transition name="dropdown">
              <div v-if="showDropdown" class="avatar-dropdown">
                <div class="dropdown-user">
                  <p class="dropdown-nickname">{{ authStore.user?.nickname }}</p>
                  <p class="dropdown-username">{{ authStore.user?.username }}</p>
                </div>
                <div class="dropdown-divider" />
                <button class="dropdown-item dropdown-item-danger" @click="handleLogout">
                  退出登录
                </button>
              </div>
            </Transition>
          </div>
        </div>
      </div>

      <div class="content-body" ref="contentBodyRef">
        <div v-if="selectedArticle" class="content-inner">
          <div class="article-body">
            <p>{{ mockContent }}</p>
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
import type { StudyArticle } from '~/types'
import { onClickOutside } from '@vueuse/core'

definePageMeta({ layout: 'blank' })

const authStore = useAuthStore()
const router = useRouter()

// --- Data ---
const articles = ref<StudyArticle[]>([])
const selectedArticle = ref<StudyArticle | null>(null)
const activeStatus = ref('ALL')
const activeTags = ref<string[]>([])
const searchQuery = ref('')
const popoverVisible = ref(false)
const showDropdown = ref(false)
const publishedExpanded = ref(false)
const publishedSubFilter = ref<'PUBLIC' | 'PRIVATE' | null>(null)

// --- Refs for scrollbar ---
const sidebarScrollRef = ref<HTMLElement | null>(null)
const contentBodyRef = ref<HTMLElement | null>(null)
const avatarContainer = ref<HTMLElement | null>(null)

// --- Compute ---
const avatarChar = computed(() => authStore.user?.nickname?.charAt(0) || '慢')

const allTags = computed(() => {
  const tagSet = new Set<string>()
  articles.value.forEach(a => a.tags?.forEach(t => tagSet.add(t)))
  return Array.from(tagSet)
})

const statusItems = computed(() => [
  {
    key: 'ALL',
    label: '全部',
    icon: '<line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/>',
    count: articles.value.length,
  },
  {
    key: 'PUBLISHED',
    label: '已发布',
    icon: '<polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>',
    count: articles.value.filter(a => a.status === 'PUBLISHED').length,
    publicCount: articles.value.filter(a => a.status === 'PUBLISHED' && a.visibility === 'PUBLIC').length,
    privateCount: articles.value.filter(a => a.status === 'PUBLISHED' && a.visibility === 'PRIVATE').length,
  },
  {
    key: 'UNPUBLISHED',
    label: '草稿',
    icon: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>',
    count: articles.value.filter(a => a.status === 'UNPUBLISHED').length,
  },
  {
    key: 'BOOKMARKED',
    label: '收藏',
    icon: '<path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>',
    count: articles.value.filter(a => a.status === 'BOOKMARKED').length,
  },
])

const filteredNotes = computed(() => {
  let list = articles.value
  if (activeStatus.value !== 'ALL') {
    if (activeStatus.value === 'PUBLISHED' && publishedSubFilter.value) {
      list = list.filter(a => a.status === 'PUBLISHED' && a.visibility === publishedSubFilter.value)
    } else {
      list = list.filter(a => a.status === activeStatus.value)
    }
  }
  if (activeTags.value.length) {
    list = list.filter(a => activeTags.value.some(t => a.tags?.includes(t)))
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(a => a.title.toLowerCase().includes(q))
  }
  return list
})

const mockContent = '在构建分布式系统时，一致性模型的选择往往是架构决策中最关键、也最困难的一环。从严格到宽松，每种模型都代表着一组特定的取舍——在可用性、性能和正确性之间寻找平衡点。理解每种模型的能力边界和适用场景，是成为优秀后端工程师的必经之路。'

// --- Methods ---
function toggleTag(tag: string) {
  const idx = activeTags.value.indexOf(tag)
  if (idx >= 0) {
    activeTags.value.splice(idx, 1)
  } else {
    activeTags.value.push(tag)
  }
}

function selectArticle(article: StudyArticle) {
  selectedArticle.value = article
}

function togglePublished() {
  if (publishedExpanded.value) {
    publishedExpanded.value = false
  } else {
    publishedExpanded.value = true
    activeStatus.value = 'PUBLISHED'
    publishedSubFilter.value = null
  }
}

function selectPublishedSub(visibility: 'PUBLIC' | 'PRIVATE') {
  activeStatus.value = 'PUBLISHED'
  publishedSubFilter.value = visibility
}

function viewDetail() {
  if (!selectedArticle.value) return
  window.open(`/write?articleId=${selectedArticle.value.id}`, '_blank')
}

function onPublish() {
  window.open('/write', '_blank')
}

async function handleLogout() {
  const { logout } = useAuth()
  await logout()
  showDropdown.value = false
  navigateTo('/login')
}

// --- Init ---
onMounted(async () => {
  const { listStudyArticles } = useArticle()
  articles.value = await listStudyArticles()
  if (articles.value.length > 0 && articles.value[0]) {
    selectedArticle.value = articles.value[0]
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

// --- Close popover & dropdown on outside click ---
onClickOutside(avatarContainer, () => {
  showDropdown.value = false
})

onMounted(() => {
  document.addEventListener('click', (e) => {
    const target = e.target as HTMLElement
    if (!target.closest('.tag-filter-wrap')) {
      popoverVisible.value = false
    }
  })
})
</script>

<style scoped>
/* ===== Root ===== */
.body {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--surface-soft);
}

/* ===== Left Sidebar ===== */
.sidebar {
  width: var(--sidebar-width);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: var(--surface);
  border-right: 1px solid var(--hairline);
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg) var(--spacing-md) var(--spacing-sm);
  font-family: var(--font-sans);
  font-size: var(--heading-4);
  font-weight: var(--weight-semibold);
  color: var(--ink);
  flex-shrink: 0;
}
.sidebar-logo-mark {
  width: 32px;
  height: 32px;
  border-radius: var(--rounded-md);
  background: var(--ink);
  color: var(--canvas);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: var(--weight-semibold);
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Noto Sans SC', sans-serif;
  flex-shrink: 0;
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
  color: var(--steel);
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
.sidebar-action-btn svg { opacity: 0.5; flex-shrink: 0; }
.sidebar-action-btn.active svg { opacity: 1; }

.sidebar-action-count {
  margin-left: auto;
  font-size: var(--caption);
  color: var(--muted);
}

.sidebar-action-chevron {
  display: inline-flex;
  align-items: center;
  color: var(--muted);
  transition: transform 0.2s var(--ease);
}
.sidebar-action-chevron.expanded {
  transform: rotate(180deg);
}

.sidebar-sub-items {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.sidebar-sub-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  width: 100%;
  padding: 5px var(--spacing-sm) 5px 32px;
  border: none;
  border-radius: var(--rounded-sm);
  background: transparent;
  color: var(--steel);
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  font-weight: var(--weight-regular);
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s var(--ease), color 0.15s var(--ease);
  text-align: left;
}
.sidebar-sub-btn:hover {
  background: var(--hairline-soft);
  color: var(--ink);
}
.sidebar-sub-btn.active {
  background: var(--canvas);
  color: var(--ink);
  font-weight: var(--weight-medium);
  box-shadow: rgba(15, 15, 15, 0.06) 0px 1px 3px 0px;
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
  color: var(--steel);
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
.sidebar-community-btn svg { flex-shrink: 0; opacity: 0.5; }

/* ===== Content Area ===== */
.content-area {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: var(--canvas);
}

/* Content topbar */
.content-topbar {
  flex-shrink: 0;
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
.topbar-avatar {
  width: 28px; height: 28px;
  border-radius: var(--rounded-full);
  background: var(--primary);
  color: var(--on-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--body-sm-medium);
  font-weight: var(--weight-medium);
  flex-shrink: 0;
  cursor: pointer;
  transition: opacity 0.15s var(--ease);
}
.topbar-avatar:hover { opacity: 0.85; }

.avatar-wrapper { position: relative; flex-shrink: 0; }

.avatar-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 160px;
  padding: var(--spacing-xs);
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-xl);
  box-shadow: rgba(15, 15, 15, 0.08) 0px 4px 12px 0px;
  z-index: var(--z-modal);
}

.dropdown-user { padding: 8px 12px; }
.dropdown-nickname {
  font-size: var(--body-sm);
  font-weight: var(--weight-medium);
  color: var(--ink);
  margin: 0;
  line-height: 1.4;
}
.dropdown-username {
  font-size: var(--caption);
  color: var(--muted);
  margin: 2px 0 0;
  line-height: 1.4;
}

.dropdown-divider {
  height: 1px;
  background: var(--hairline);
  margin: 4px 0;
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 8px 12px;
  border: none;
  border-radius: var(--rounded-sm);
  background: transparent;
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: var(--body-sm);
  font-weight: var(--weight-regular);
  line-height: 1.4;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s var(--ease);
}
.dropdown-item:hover { background: var(--hairline-soft); }
.dropdown-item-danger:hover { color: var(--semantic-error); }

.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s var(--ease), transform 0.15s var(--ease);
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
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
  flex: 1;
  overflow-y: auto;
  display: flex;
  justify-content: center;
  padding: var(--spacing-xxxl) var(--spacing-xxl);
}
.content-inner {
  width: 100%;
  max-width: 720px;
  background: var(--canvas);
  border-radius: var(--rounded-lg);
  box-shadow: rgba(15, 15, 15, 0.04) 0px 1px 2px 0px;
  padding: var(--spacing-xl);
  height: fit-content;
}
.content-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--muted);
  font-size: var(--body-md);
}

/* Article */
.article-body {
  font-family: var(--font-sans);
  font-size: var(--body-md);
  font-weight: var(--weight-regular);
  line-height: var(--leading-body);
  color: var(--charcoal);
}
.article-body p { margin-bottom: var(--spacing-md); }

/* ===== Tag popover ===== */
.tag-filter-wrap { position: relative; }
.tag-popover {
  display: none;
  position: absolute;
  left: 0;
  top: 100%;
  margin-top: var(--spacing-xs);
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-lg);
  padding: var(--spacing-sm);
  min-width: 240px;
  z-index: 200;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
  box-shadow: rgba(15, 15, 15, 0.08) 0px 4px 12px 0px;
}
.tag-popover.visible { display: flex; }
.tag-popover-chip {
  padding: var(--spacing-xs) var(--spacing-sm);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-full);
  background: transparent;
  color: var(--steel);
  font-size: var(--body-sm-medium);
  font-weight: var(--weight-medium);
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.15s var(--ease);
}
.tag-popover-chip:hover { border-color: var(--hairline-strong); }
.tag-popover-chip.selected {
  background: var(--ink-deep);
  border-color: var(--ink-deep);
  color: var(--on-dark);
}

/* ===== Scrollbar auto-hide ===== */
.sidebar-scroll::-webkit-scrollbar,
.content-body::-webkit-scrollbar { width: 5px; }
.sidebar-scroll::-webkit-scrollbar-track,
.content-body::-webkit-scrollbar-track { background: transparent; }
.sidebar-scroll::-webkit-scrollbar-thumb,
.content-body::-webkit-scrollbar-thumb { background: transparent; border-radius: 3px; }
.sidebar-scroll.scrolling::-webkit-scrollbar-thumb,
.content-body.scrolling::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.15); }
.sidebar-scroll::-webkit-scrollbar-thumb:hover,
.content-body::-webkit-scrollbar-thumb:hover { background: rgba(0,0,0,0.25); }

/* ===== Responsive ===== */
@media (max-width: 767px) {
  .sidebar { display: none; }
  .content-body { padding: var(--spacing-md); }
  .content-inner { padding: var(--spacing-md); }
}
</style>
