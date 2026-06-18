<!--
  pages/manage.vue — 个人管理页面
  左侧菜单 + 右侧内容，视觉风格对齐 admin.vue
-->
<template>
  <div class="body">
    <!-- ===== Left Sidebar ===== -->
    <aside class="sidebar">
      <SidebarHeader />

      <div class="sidebar-scroll">
        <nav class="sidebar-menu">
          <button
            class="sidebar-menu-item"
            :class="{ active: activeTab === 'profile' }"
            @click="activeTab = 'profile'"
          >
            <IconLucideUser />
            个人资料
          </button>
          <button
            class="sidebar-menu-item"
            :class="{ active: activeTab === 'articles' }"
            @click="activeTab = 'articles'"
          >
            <IconLucideFileText />
            内容管理
          </button>
        </nav>

        <button class="sidebar-back" @click="router.push('/home')">
          <IconLucideArrowLeft />
          返回知识库
        </button>
      </div>
    </aside>

    <!-- ===== Content Area ===== -->
    <main class="content">
      <div class="content-body">
        <div class="content-inner">
          <!-- ── 内容管理 ── -->
          <template v-if="activeTab === 'articles'">
            <div class="filter-bar">
              <button
                v-for="f in statusFilters"
                :key="f.key"
                class="segmented-tab"
                :class="{ active: articleFilter === f.key }"
                @click="articleFilter = f.key"
              >
                {{ f.label }}
              </button>
            </div>

            <div v-if="articles.length > 0" class="manage-list">
              <div v-for="a in articles" :key="a.id" class="manage-row">
                <!-- 缩略图 -->
                <div class="manage-thumb">
                  <img v-if="a.coverUrl" :src="a.coverUrl" alt="" @error="onThumbError" />
                  <span class="manage-thumb-placeholder" :class="{ show: !a.coverUrl }">未设置封面</span>
                </div>

                <div class="manage-body">
                  <p class="manage-title">{{ a.title || '未命名' }}</p>
                  <p class="manage-summary">{{ a.summary || '暂无摘要' }}</p>
                  <p v-if="a.status === 'REJECTED' && a.reviewReason" class="manage-review-reason">驳回理由：{{ a.reviewReason }}</p>
                  <p class="manage-meta">
                    <span :class="statusBadgeClass(a.status)">{{ statusLabel(a.status) }}</span>
                    <span class="meta-dot">·</span>
                    <span>{{ visibilityLabel(a.visibility) }}</span>
                    <span class="meta-dot">·</span>
                    <span>阅 {{ a.viewCount }}</span>
                    <span class="meta-dot">·</span>
                    <span>赞 {{ a.likeCount }}</span>
                    <span class="meta-dot">·</span>
                    <span>评 {{ a.commentCount }}</span>
                    <span class="meta-dot">·</span>
                    <span>藏 {{ a.bookmarkCount }}</span>
                    <span class="meta-dot">·</span>
                    {{ formatTime(a.updatedAt) }}
                  </p>
                </div>

                <div class="manage-actions">
                  <button class="btn-detail" @click="editArticle(a.id)">编辑</button>
                  <button class="btn-detail danger" @click="handleDelete(a)">删除</button>
                </div>
              </div>
            </div>

            <div v-else class="empty-state">
              <p class="empty-state-text">暂无文章</p>
              <button class="btn-ghost" @click="router.push('/write')">去写一篇</button>
            </div>

            <!-- ── 分页 ── -->
            <div class="pagination">
              <span class="page-total">共 {{ totalCount }} 条</span>
              <button
                class="page-btn"
                :disabled="currentPage <= 1"
                @click="goToPage(currentPage - 1)"
              >
                上一页
              </button>
              <template v-for="(p, i) in visiblePages" :key="i">
                <span v-if="p.value === null" class="page-ellipsis">…</span>
                <button
                  v-else
                  class="page-btn"
                  :class="{ active: p.value === currentPage }"
                  @click="goToPage(p.value)"
                >
                  {{ p.label }}
                </button>
              </template>
              <button
                class="page-btn"
                :disabled="currentPage >= totalPages"
                @click="goToPage(currentPage + 1)"
              >
                下一页
              </button>
            </div>
          </template>

          <!-- ── 个人资料 ── -->
          <div v-if="activeTab === 'profile'" class="profile-form">
            <div class="form-group">
              <label class="form-label">头像</label>
              <div class="avatar-upload-row">
                <div class="avatar-preview">{{ avatarChar }}</div>
                <span class="btn-ghost">上传头像</span>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">昵称</label>
              <input class="form-input" v-model="profile.nickname" placeholder="你的昵称" />
            </div>
            <button class="btn-primary" @click="saveProfile">保存</button>
            <p v-if="profileSaved" class="profile-saved-hint">已保存</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import type { ArticleManage } from '~/types'
import IconLucideFileText from '~icons/lucide/file-text'
import IconLucideUser from '~icons/lucide/user'
import IconLucideArrowLeft from '~icons/lucide/arrow-left'

definePageMeta({ layout: 'blank', middleware: 'role-guard' })

const router = useRouter()
const auth = useAuthStore()

const avatarChar = computed(() => auth.user?.nickname?.charAt(0) || '慢')

// ── Tab ──
const route = useRoute()
const activeTab = ref<'articles' | 'profile'>((route.query.tab as string) === 'profile' ? 'profile' : 'articles')
watch(() => route.query.tab, (v) => { activeTab.value = v === 'profile' ? 'profile' : 'articles' })

// ── Articles ──
const articles = ref<ArticleManage[]>([])
const articleFilter = ref('ALL')
const currentPage = ref(1)
const totalCount = ref(0)
const pageSize = 10

const totalPages = computed(() => Math.max(1, Math.ceil(totalCount.value / pageSize)))

interface PageItem {
  label: string
  value: number | null
}

const visiblePages = computed<PageItem[]>(() => {
  const total = totalPages.value
  const cur = currentPage.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => ({ label: String(i + 1), value: i + 1 }))
  const pages: PageItem[] = [{ label: '1', value: 1 }]
  if (cur > 3) pages.push({ label: '…', value: null })
  const start = Math.max(2, cur - 1)
  const end = Math.min(total - 1, cur + 1)
  for (let i = start; i <= end; i++) pages.push({ label: String(i), value: i })
  if (cur < total - 2) pages.push({ label: '…', value: null })
  pages.push({ label: String(total), value: total })
  return pages
})

const statusFilters = computed(() => [
  { key: 'ALL', label: '全部' },
  { key: 'PUBLISHED', label: '已发布' },
  { key: 'REVIEWING', label: '审核中' },
  { key: 'REJECTED', label: '未通过' },
  { key: 'DRAFT', label: '草稿' },
])

function formatTime(iso: string) {
  if (!iso) return ''
  const d = new Date(iso)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function statusLabel(s: string) {
  const map: Record<string, string> = { DRAFT: '草稿', PUBLISHED: '已发布', REVIEWING: '审核中', REJECTED: '未通过', PRIVATE: '私密' }
  return map[s] || s
}

function statusBadgeClass(s: string) {
  const map: Record<string, string> = { DRAFT: 'status-draft', PUBLISHED: 'status-published', REVIEWING: 'status-reviewing', REJECTED: 'status-rejected' }
  return map[s] || 'status-draft'
}

function visibilityLabel(v?: string) {
  return v === 'PRIVATE' ? '私密' : '公开'
}

function onThumbError(e: Event) {
  const img = e.target as HTMLImageElement
  img.style.display = 'none'
  const placeholder = img.nextElementSibling as HTMLElement
  if (placeholder) placeholder.classList.add('show')
}

function editArticle(id: number) {
  router.push(`/write?articleId=${id}`)
}

async function handleDelete(a: ArticleManage) {
  if (!confirm(`确定删除「${a.title || '未命名'}」吗？`)) return
  try {
    const api = useApi()
    await api.delete(`/api/articles/${a.id}`)
    articles.value = articles.value.filter(x => x.id !== a.id)
  } catch {
    // ignore
  }
}

// ── Profile ──
const profile = reactive({
  nickname: auth.user?.nickname || '',
})

const profileSaved = ref(false)

async function saveProfile() {
  try {
    auth.user!.nickname = profile.nickname
    profileSaved.value = true
    setTimeout(() => profileSaved.value = false, 2000)
  } catch {
    // ignore
  }
}

// ── Fetch ──
const { listArticleManages } = useArticle()

async function fetchArticles() {
  const res = await listArticleManages(currentPage.value, pageSize, articleFilter.value)
  articles.value = res?.records || []
  totalCount.value = res?.total || 0
  await nextTick()
  document.querySelectorAll('.manage-thumb img').forEach(img => {
    if (!(img as HTMLImageElement).complete || (img as HTMLImageElement).naturalWidth === 0) {
      img.dispatchEvent(new Event('error'))
    }
  })
}

function goToPage(page: number) {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return
  currentPage.value = page
  fetchArticles()
}

watch(articleFilter, () => {
  currentPage.value = 1
  fetchArticles()
})

onMounted(() => fetchArticles())
</script>

<style scoped>
/* ===== Layout ===== */
.body {
  display: flex; min-height: 100vh;
  background: var(--surface-soft);
}

/* ===== Sidebar ===== */
.sidebar {
  width: var(--sidebar-width); flex-shrink: 0;
  display: flex; flex-direction: column;
  background: var(--surface);
  border-right: 1px solid var(--hairline);
  position: sticky; top: 0; height: 100vh;
}

.sidebar-scroll {
  flex: 1; overflow-y: auto;
}

.sidebar-menu {
  display: flex; flex-direction: column; gap: 2px;
  padding: var(--spacing-sm) var(--spacing-md);
}

.sidebar-menu-item {
  display: flex; align-items: center; gap: var(--spacing-xs);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--rounded-md);
  font-family: var(--font-sans); font-size: var(--body-sm);
  font-weight: var(--weight-regular); color: var(--charcoal);
  cursor: pointer; border: none; background: transparent;
  text-align: left; transition: background 0.15s var(--ease);
  line-height: 1.5;
}

.sidebar-menu-item:hover { background: var(--hairline-soft); }

.sidebar-menu-item.active {
  background: var(--canvas); font-weight: var(--weight-medium); color: var(--ink);
}

.sidebar-back {
  margin-top: auto; display: flex; align-items: center; gap: var(--spacing-xs);
  padding: var(--spacing-xs) var(--spacing-sm); border-radius: var(--rounded-md);
  font-family: var(--font-sans); font-size: var(--body-sm); color: var(--steel);
  cursor: pointer; border: none; background: transparent;
  transition: color 0.15s var(--ease);
  margin: var(--spacing-xl) var(--spacing-md);
}

.sidebar-back:hover { color: var(--ink); }

/* ===== Content ===== */
.content {
  flex: 1; min-width: 0;
  display: flex; flex-direction: column;
  background: var(--canvas);
}

.content-body {
  padding: var(--spacing-xxl);
}

.content-inner { width: 100%; }

/* ===== Filter Bar ===== */
.filter-bar {
  display: flex; align-items: center; gap: var(--spacing-xs);
  margin-bottom: var(--spacing-md);
}

.segmented-tab {
  padding: var(--spacing-sm) var(--spacing-md);
  border: none;
  border-bottom: 2px solid transparent;
  font-family: var(--font-sans); font-size: var(--body-sm-medium);
  font-weight: var(--weight-medium); color: var(--steel);
  background: transparent; cursor: pointer; line-height: var(--leading-sm);
  transition: color 0.15s var(--ease);
}

.segmented-tab:hover { color: var(--ink); }

.segmented-tab.active {
  color: var(--ink);
  border-bottom-color: var(--ink);
}

/* ===== Manage List ===== */
.manage-list { display: flex; flex-direction: column; }

.manage-row {
  display: flex; align-items: center; gap: var(--spacing-xl);
  padding: var(--spacing-lg) 0;
}

.manage-row:first-child { padding-top: 0; }
.manage-row:last-child { padding-bottom: 0; }

.manage-row:not(:last-child) {
  border-bottom: 1px solid var(--hairline-soft);
}

/* ── Thumbnail ── */
.manage-thumb {
  width: 176px; height: 112px; flex-shrink: 0;
  border-radius: var(--rounded-sm); overflow: hidden;
  background: var(--surface);
}

.manage-thumb img {
  width: 100%; height: 100%; object-fit: cover; display: block;
}

.manage-thumb-placeholder {
  display: none; align-items: center; justify-content: center;
  width: 100%; height: 100%;
  background: var(--surface);
  font-family: var(--font-sans); font-size: 13px; color: var(--muted);
}

.manage-thumb-placeholder.show { display: flex; }

/* ── Body ── */
.manage-body {
  flex: 1; min-width: 0;
  display: flex; flex-direction: column; gap: 4px;
}

.manage-title {
  font-family: var(--font-sans); font-size: var(--body-md);
  font-weight: var(--weight-medium); color: var(--ink);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  margin: 0;
}

.manage-summary {
  font-family: var(--font-sans); font-size: var(--body-sm); color: var(--steel);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  margin: 0;
}

.manage-review-reason {
  font-family: var(--font-sans); font-size: 13px; color: var(--slate);
  padding: 2px var(--spacing-sm); background: var(--surface);
  border-radius: var(--rounded-sm);
  margin: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}

.manage-meta {
  font-family: var(--font-sans); font-size: 13px; color: var(--muted);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  margin: 0; display: flex; align-items: center; gap: 4px;
}

.meta-dot { color: var(--hairline-strong); }

/* ── Status Labels ── */
.status-draft     { color: var(--steel); }
.status-published { color: var(--semantic-success); }
.status-reviewing { color: var(--semantic-warning); }
.status-rejected  { color: var(--semantic-error); }

/* ── Actions ── */
.manage-actions {
  display: flex; align-items: center; gap: var(--spacing-xxs); flex-shrink: 0;
}

/* ===== Buttons ===== */
.btn-detail {
  display: inline-flex; align-items: center; justify-content: center;
  height: 32px; padding: 0 14px;
  border: none; border-radius: var(--rounded-sm);
  font-family: var(--font-sans); font-size: var(--body-sm-medium);
  font-weight: var(--weight-medium); color: var(--ink);
  background: transparent; cursor: pointer;
  transition: background 0.15s var(--ease);
}

.btn-detail:hover { background: var(--hairline-soft); }

.btn-detail.danger { color: var(--semantic-error); }
.btn-detail.danger:hover { background: #fde8e8; }

.btn-ghost {
  display: inline-flex; align-items: center; justify-content: center;
  height: 36px; padding: 0 16px;
  border: none; border-radius: var(--rounded-sm);
  font-family: var(--font-sans); font-size: 14px; font-weight: 500;
  background: transparent; color: var(--ink);
  cursor: pointer; transition: background 0.15s var(--ease);
}

.btn-ghost:hover { background: var(--hairline-soft); }

.btn-primary {
  display: inline-flex; align-items: center; justify-content: center;
  height: 36px; padding: 0 16px;
  border: none; border-radius: var(--rounded-md);
  font-family: var(--font-sans); font-size: 14px; font-weight: 500;
  background: var(--primary); color: var(--on-primary);
  cursor: pointer; transition: background 0.15s var(--ease);
}

.btn-primary:hover { background: var(--primary-pressed); }

/* ===== Empty ===== */
.empty-state { padding: var(--spacing-xxxl) var(--spacing-xl); text-align: center; }

.empty-state-text { font-family: var(--font-sans); font-size: 14px; color: var(--steel); margin-bottom: var(--spacing-sm); }

/* ===== Pagination ===== */
.pagination {
  display: flex; align-items: center; justify-content: center;
  gap: var(--spacing-xs); margin-top: var(--spacing-lg);
}

.page-total {
  font-family: var(--font-sans); font-size: var(--body-sm);
  color: var(--steel); margin-right: var(--spacing-sm);
}

.page-btn {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 36px; height: 36px; padding: 8px 12px;
  border: none; border-radius: var(--rounded-sm);
  font-family: var(--font-sans); font-size: var(--body-sm-medium);
  font-weight: var(--weight-medium); line-height: var(--leading-button);
  color: var(--ink); background: transparent;
  cursor: pointer; transition: background 0.15s var(--ease);
}

.page-btn:hover:not(:disabled):not(.active) { background: var(--hairline-soft); }

.page-btn:disabled {
  color: var(--muted); cursor: not-allowed;
}

.page-btn.active {
  background: var(--ink-deep); color: var(--on-dark);
  border-radius: var(--rounded-md); font-weight: var(--weight-semibold);
}

.page-ellipsis {
  display: inline-flex; align-items: center; justify-content: center;
  width: 36px; height: 36px;
  font-family: var(--font-sans); font-size: var(--body-sm-medium);
  color: var(--muted);
}

/* ===== Profile ===== */
.profile-form {
  display: flex; flex-direction: column; gap: var(--spacing-lg);
  max-width: 480px;
}

.form-group { display: flex; flex-direction: column; gap: var(--spacing-xxs); }

.form-label {
  font-family: var(--font-sans); font-size: 14px; font-weight: 500; color: var(--charcoal);
}

.form-input {
  height: 44px; padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--hairline-strong); border-radius: var(--rounded-md);
  font-size: 16px; font-family: var(--font-sans); color: var(--ink);
  background: var(--canvas); outline: none;
  transition: border 0.15s var(--ease);
}

.form-input:focus { border: 2px solid var(--primary); }

.avatar-upload-row { display: flex; align-items: center; gap: var(--spacing-md); }

.avatar-preview {
  width: 64px; height: 64px; border-radius: var(--rounded-full);
  background: var(--primary); color: var(--on-primary);
  font-size: 24px; font-weight: 600;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.profile-saved-hint {
  font-family: var(--font-sans); font-size: 13px; color: var(--semantic-success);
}
</style>
