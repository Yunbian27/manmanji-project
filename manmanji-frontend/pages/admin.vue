<!--
  pages/admin.vue — 管理员后台
  审核管理 + 用户管理（预留）
-->
<template>
  <div class="body">
    <!-- ===== Left Sidebar ===== -->
    <aside class="sidebar">
      <div class="sidebar-brand">
        <img src="/favicon.svg" alt="" class="sidebar-brand-icon" />
        <span class="sidebar-brand-text">后台管理</span>
      </div>

      <nav class="sidebar-menu">
        <button
          class="sidebar-menu-item"
          :class="{ active: activeTab === 'review' }"
          @click="activeTab = 'review'"
        >
          <IconLucideClipboardCheck />
          审核管理
        </button>
        <button
          class="sidebar-menu-item disabled"
          title="功能开发中"
        >
          <IconLucideUsers />
          用户管理
        </button>
      </nav>

      <button class="sidebar-logout" @click="handleLogout">
        <IconLucideLogOut />
        退出登录
      </button>
    </aside>

    <!-- ===== Content Area ===== -->
    <main class="content">
      <div class="content-body">
        <div class="content-inner">
          <!-- ── 审核管理 ── -->
          <template v-if="activeTab === 'review'">
            <div class="filter-bar">
              <button
                v-for="f in reviewFilters"
                :key="f.key"
                class="segmented-tab"
                :class="{ active: reviewFilter === f.key }"
                @click="reviewFilter = f.key"
              >
                {{ f.label }}
              </button>
            </div>

            <div class="review-list">
              <div v-for="item in reviews" :key="item.id" class="review-row">
                <div class="review-thumb">
                  <img v-if="item.coverUrl" :src="item.coverUrl" alt="" @error="onThumbError" />
                  <span class="review-thumb-placeholder" :class="{ show: !item.coverUrl }">未设置封面</span>
                </div>

                <div class="review-body">
                  <p class="review-title">{{ item.title || '未命名' }}</p>
                  <p class="review-summary">{{ item.summary || '暂无摘要' }}</p>
                  <p class="review-meta">
                    <span>{{ item.author }}</span>
                    <span class="meta-dot">·</span>
                    <span>{{ formatTime(item.submittedAt) }}</span>
                  </p>
                </div>

                <div class="review-right">
                  <button class="btn-detail" @click="openDetail(item)">详情</button>
                </div>
              </div>

              <div v-if="reviews.length === 0 && !loading" class="empty-state">
                <p class="empty-state-text">暂无数据</p>
              </div>
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
        </div>
      </div>
    </main>

    <!-- ===== 驳回弹窗 ===== -->
    <Transition name="modal-fade">
      <div v-if="rejectTarget" class="modal-backdrop" @click.self="cancelReject">
        <div class="modal-card">
          <h4 class="modal-title">驳回理由</h4>
          <textarea
            v-model="rejectReason"
            class="modal-textarea"
            placeholder="请输入驳回理由（可选，最多 100 字）"
            maxlength="100"
            rows="2"
          />
          <div class="modal-footer">
            <button class="btn-ghost" @click="cancelReject">取消</button>
            <button class="btn-primary" @click="confirmReject">确认驳回</button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ===== 详情弹窗 ===== -->
    <Transition name="modal-fade">
      <div v-if="detailTarget" class="modal-backdrop" @click.self="closeDetail">
        <div class="detail-card">
          <div class="detail-header">
            <h3 class="detail-header-title">文章详情</h3>
            <button class="detail-close" @click="closeDetail">✕</button>
          </div>
          <div class="detail-body">
            <img v-if="detailArticle?.coverUrl" :src="detailArticle.coverUrl" class="detail-cover" alt="" />
            <h2 class="detail-title">{{ detailArticle?.title || detailTarget.title || '未命名' }}</h2>
            <div class="detail-meta">
              <span>{{ detailTarget.author }}</span>
              <span class="meta-dot">·</span>
              <span>{{ formatTime(detailTarget.submittedAt) }}</span>
            </div>
            <div
              v-if="detailTarget.reviewStatus !== 'REVIEWING' && detailTarget.reviewReason"
              class="detail-review-reason"
            >
              {{ statusLabel(detailTarget.reviewStatus) }}意见：{{ detailTarget.reviewReason }}
            </div>
            <hr class="detail-divider" />
            <div v-if="detailLoading" class="detail-loading">加载中…</div>
            <div v-else class="detail-content markdown-body" v-html="detailHtml" />
          </div>
          <div v-if="detailTarget.reviewStatus === 'REVIEWING'" class="detail-footer">
            <button class="btn-reject-outline" @click="openRejectDialog(detailTarget)">驳回</button>
            <button class="btn-approve-dark" @click="handleApprove(detailTarget)">通过</button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import IconLucideClipboardCheck from '~icons/lucide/clipboard-check'
import IconLucideUsers from '~icons/lucide/users'
import IconLucideLogOut from '~icons/lucide/log-out'
import type { ArticleVO } from '~/types'

definePageMeta({ layout: 'blank', middleware: 'role-guard' })

const router = useRouter()

async function handleLogout() {
  const { logout } = useAuth()
  await logout()
  router.replace('/login')
}

const activeTab = ref<'review'>('review')

// ── 审核数据 ──
interface ReviewItem {
  id: number
  title: string
  summary?: string
  coverUrl?: string
  author: string
  submittedAt: string
  reviewStatus: 'REVIEWING' | 'PUBLISHED' | 'REJECTED'
  reviewReason?: string
}

interface ReviewPage {
  total: number
  page: number
  size: number
  records: ReviewItem[]
}

const reviews = ref<ReviewItem[]>([])
const loading = ref(false)
const currentPage = ref(1)
const totalCount = ref(0)
const pageSize = 10

function formatTime(iso: string): string {
  if (!iso) return ''
  const d = new Date(iso)
  if (isNaN(d.getTime())) return iso
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const reviewFilter = ref('REVIEWING')

const totalPages = computed(() => Math.max(1, Math.ceil(totalCount.value / pageSize)))

interface PageItem {
  label: string
  value: number | null  // null = 省略号
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

const reviewFilters = [
  { key: 'REVIEWING', label: '待审核' },
  { key: 'PUBLISHED', label: '已通过' },
  { key: 'REJECTED', label: '已驳回' },
]

async function fetchReviews() {
  loading.value = true
  reviews.value = []
  try {
    const api = useApi()
    const status = reviewFilter.value
    const res = await api.get<ReviewPage>(`/api/admin/articles?status=${status}&page=${currentPage.value}&size=${pageSize}`)
    reviews.value = res?.records || []
    totalCount.value = res?.total || 0
  } catch (e) {
    console.error('获取审核列表失败', e)
  } finally {
    loading.value = false
  }
}

function goToPage(page: number) {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return
  currentPage.value = page
  fetchReviews()
}

watch(reviewFilter, () => {
  currentPage.value = 1
  fetchReviews()
})
onMounted(() => fetchReviews())

function statusLabel(s: string) {
  const map: Record<string, string> = { REVIEWING: '待审核', PUBLISHED: '已通过', REJECTED: '已驳回' }
  return map[s] || s
}

// ── 详情弹窗 ──
const detailTarget = ref<ReviewItem | null>(null)
const detailArticle = ref<ArticleVO | null>(null)
const detailLoading = ref(false)
const detailContent = computed(() => detailArticle.value?.content || '')

const { renderedHtml: detailHtml } = useMarkdownRenderer(detailContent)

async function onThumbError(e: Event) {
  const img = e.target as HTMLImageElement
  img.style.display = 'none'
  ;(img.nextElementSibling as HTMLElement).classList.add('show')
}

async function openDetail(item: ReviewItem) {
  detailTarget.value = item
  detailLoading.value = true
  detailArticle.value = null
  try {
    const api = useApi()
    detailArticle.value = await api.get<ArticleVO>(`/api/articles/${item.id}`)
  } catch { /* ignore */ }
  finally { detailLoading.value = false }
}

function closeDetail() {
  detailTarget.value = null
  detailArticle.value = null
}

async function handleApprove(item: ReviewItem) {
  try {
    const api = useApi()
    await api.put(`/api/admin/articles/${item.id}/review`, { action: 'APPROVE', reason: '审核通过' })
    closeDetail()
    fetchReviews()
  } catch { /* ignore */ }
}

// ── 驳回弹窗 ──
const rejectTarget = ref<ReviewItem | null>(null)
const rejectReason = ref('')

function openRejectDialog(item: ReviewItem) {
  detailTarget.value = null  // 关闭详情弹窗
  rejectTarget.value = item
  rejectReason.value = ''
}

function cancelReject() {
  rejectTarget.value = null
  rejectReason.value = ''
}

async function confirmReject() {
  if (rejectTarget.value) {
    try {
      const api = useApi()
      await api.put(`/api/admin/articles/${rejectTarget.value.id}/review`, {
        action: 'REJECT',
        reason: rejectReason.value || '未提供理由',
      })
      cancelReject()
      fetchReviews()
    } catch { cancelReject() }
  } else {
    cancelReject()
  }
}
</script>

<style scoped>
/* ===== Layout ===== */
.body {
  display: flex; height: 100%; overflow: hidden;
  background: var(--surface-soft);
}

/* ===== Sidebar ===== */
.sidebar {
  width: var(--sidebar-width); flex-shrink: 0;
  display: flex; flex-direction: column;
  background: var(--surface);
  border-right: 1px solid var(--hairline);
}

.sidebar-brand {
  display: flex; align-items: center; gap: var(--spacing-sm);
  padding: var(--spacing-lg) var(--spacing-lg) var(--spacing-md);
}

.sidebar-brand-icon {
  width: 24px; height: 24px; display: block;
}

.sidebar-brand-text {
  font-family: var(--font-sans); font-size: var(--body-md);
  font-weight: var(--weight-semibold); color: var(--ink);
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

.sidebar-menu-item.disabled {
  color: var(--muted); cursor: not-allowed;
}

.sidebar-menu-item.disabled:hover { background: transparent; }

.sidebar-logout {
  margin-top: auto; display: flex; align-items: center; gap: var(--spacing-xs);
  padding: var(--spacing-xs) var(--spacing-sm); border-radius: var(--rounded-md);
  font-family: var(--font-sans); font-size: var(--body-sm); color: var(--steel);
  cursor: pointer; border: none; background: transparent;
  transition: color 0.15s var(--ease);
  margin: var(--spacing-xl) var(--spacing-md);
}

.sidebar-logout:hover { color: var(--semantic-error); }

/* ===== Content ===== */
.content {
  flex: 1; display: flex; flex-direction: column;
  background: var(--canvas); overflow: hidden;
}

.content-body {
  flex: 1; overflow-y: auto;
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

/* ===== Review List ===== */
.review-list { display: flex; flex-direction: column; }

.review-row {
  display: flex; align-items: center; gap: var(--spacing-xl);
  padding: var(--spacing-lg) 0;
}

.review-row:first-child { padding-top: 0; }
.review-row:last-child { padding-bottom: 0; }

.review-row:not(:last-child) {
  border-bottom: 1px solid var(--hairline-soft);
}

/* ── Thumbnail ── */
.review-thumb {
  width: 176px; height: 112px; flex-shrink: 0;
  border-radius: var(--rounded-sm); overflow: hidden;
  background: var(--surface);
}

.review-thumb img {
  width: 100%; height: 100%; object-fit: cover; display: block;
}

.review-thumb-placeholder {
  display: none; align-items: center; justify-content: center;
  width: 100%; height: 100%;
  background: var(--surface);
  font-family: var(--font-sans); font-size: 13px; color: var(--muted);
}

.review-thumb-placeholder.show { display: flex; }

/* ── Body ── */
.review-body {
  flex: 1; min-width: 0;
  display: flex; flex-direction: column; gap: 4px;
}

.review-title {
  font-family: var(--font-sans); font-size: var(--body-md);
  font-weight: var(--weight-medium); color: var(--ink);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  margin: 0;
}

.review-summary {
  font-family: var(--font-sans); font-size: var(--body-sm); color: var(--steel);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  margin: 0;
}

.review-meta {
  font-family: var(--font-sans); font-size: 13px; color: var(--muted);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  margin: 0; display: flex; align-items: center; gap: 4px;
}

.meta-dot { color: var(--hairline-strong); }

/* ── Right ── */
.review-right {
  display: flex; align-items: center; flex-shrink: 0;
}

/* ── Detail Button ── */
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

/* ===== Detail Modal ===== */
.detail-card {
  width: 100%; max-width: 720px; max-height: 85vh;
  display: flex; flex-direction: column;
  padding: var(--spacing-xl);
  background: var(--canvas);
  border-radius: var(--rounded-lg);
  border: 1px solid var(--hairline);
  box-shadow: rgba(15, 15, 15, 0.16) 0px 16px 48px -8px;
}

.detail-header {
  display: flex; align-items: center; justify-content: space-between;
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--hairline-soft);
}

.detail-header-title {
  font-family: var(--font-sans); font-size: var(--heading-5);
  font-weight: var(--weight-semibold); color: var(--ink);
  margin: 0; line-height: var(--leading-heading);
}

.detail-close {
  display: inline-flex; align-items: center; justify-content: center;
  width: 36px; height: 36px; border: none; border-radius: var(--rounded-sm);
  background: transparent; color: var(--steel); cursor: pointer;
  font-size: 18px; transition: background 0.15s var(--ease);
}

.detail-close:hover { background: var(--hairline-soft); color: var(--ink); }

.detail-body {
  flex: 1; overflow-y: auto; padding-top: var(--spacing-md);
}

.detail-cover {
  width: 100%; max-height: 240px; object-fit: cover;
  border-radius: var(--rounded-sm); margin-bottom: var(--spacing-md);
}

.detail-title {
  font-family: var(--font-sans); font-size: 22px; font-weight: var(--weight-semibold);
  color: var(--ink); margin: 0 0 var(--spacing-sm); line-height: var(--leading-heading);
}

.detail-meta {
  font-family: var(--font-sans); font-size: var(--body-sm); color: var(--steel);
  display: flex; align-items: center; gap: 4px; margin-bottom: var(--spacing-md);
}

.detail-review-reason {
  font-family: var(--font-sans); font-size: var(--body-sm); color: var(--slate);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--surface); border-radius: var(--rounded-sm);
  margin-bottom: var(--spacing-md);
}

.detail-divider {
  border: none; border-top: 1px solid var(--hairline-soft);
  margin: var(--spacing-md) 0;
}

.detail-loading {
  font-family: var(--font-sans); font-size: var(--body-sm);
  color: var(--muted); text-align: center; padding: var(--spacing-xxxl) 0;
}

.detail-content {
  max-width: max(calc(50% - 24.5px), calc(50vw - 92.5px));
  font-family: var(--font-sans); font-size: var(--body-md);
  color: var(--ink); line-height: var(--leading-body);
}

.detail-footer {
  display: flex; justify-content: flex-end; gap: var(--spacing-sm);
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--hairline-soft);
  margin-top: var(--spacing-md);
}

.btn-reject-outline {
  display: inline-flex; align-items: center; justify-content: center;
  height: 40px; padding: 10px 18px;
  border: 1px solid var(--hairline-strong); border-radius: var(--rounded-md);
  font-family: var(--font-sans); font-size: var(--button-md);
  font-weight: var(--weight-medium); line-height: var(--leading-button);
  background: transparent; color: var(--ink);
  cursor: pointer; transition: all 0.15s var(--ease);
}

.btn-reject-outline:hover { border-color: var(--ink); }

.btn-approve-dark {
  display: inline-flex; align-items: center; justify-content: center;
  height: 40px; padding: 10px 18px;
  border: none; border-radius: var(--rounded-md);
  font-family: var(--font-sans); font-size: var(--button-md);
  font-weight: var(--weight-medium); line-height: var(--leading-button);
  background: var(--ink-deep); color: var(--on-dark);
  cursor: pointer; transition: background 0.15s var(--ease);
}

.btn-approve-dark:hover { background: var(--charcoal); }

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

/* ===== Empty ===== */
.empty-state { padding: var(--spacing-xxxl) var(--spacing-xl); text-align: center; }

.empty-state-text { font-family: var(--font-sans); font-size: 14px; color: var(--steel); }

/* ===== Modal ===== */
.modal-backdrop {
  position: fixed; inset: 0; z-index: 400;
  display: flex; align-items: center; justify-content: center;
  background: rgba(0, 0, 0, 0.2);
}

.modal-card {
  width: 100%; max-width: 420px;
  padding: var(--spacing-xl);
  background: var(--canvas);
  border-radius: var(--rounded-lg);
  border: 1px solid var(--hairline);
  box-shadow: rgba(15, 15, 15, 0.16) 0px 16px 48px -8px;
}

.modal-title {
  font-family: var(--font-sans); font-size: 16px; font-weight: 600;
  color: var(--ink); margin: 0 0 var(--spacing-md);
}

.modal-textarea {
  width: 100%; padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--hairline-strong); border-radius: var(--rounded-md);
  font-family: var(--font-sans); font-size: 14px; color: var(--ink);
  background: var(--canvas); outline: none; resize: vertical;
  transition: border 0.15s var(--ease); box-sizing: border-box;
}

.modal-textarea:focus { border: 2px solid var(--primary); }

.modal-footer {
  display: flex; justify-content: flex-end; gap: var(--spacing-sm);
  margin-top: var(--spacing-md);
}

/* ===== Transitions ===== */
.modal-fade-enter-active,
.modal-fade-leave-active { transition: opacity 0.2s var(--ease); }

.modal-fade-enter-from,
.modal-fade-leave-to { opacity: 0; }
</style>
