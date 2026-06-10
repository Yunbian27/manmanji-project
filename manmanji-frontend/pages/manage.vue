<!--
  pages/manage.vue — 个人管理页面
  左侧菜单 + 右侧内容，复刻 /home 配色
-->
<template>
  <div class="body">
    <!-- ===== Left Sidebar ===== -->
    <aside class="sidebar">
      <SidebarHeader />

      <nav class="sidebar-menu">
        <button
          class="sidebar-menu-item"
          :class="{ active: activeTab === 'articles' }"
          @click="activeTab = 'articles'"
        >
          <IconLucideFileText />
          内容管理
        </button>
        <button
          class="sidebar-menu-item"
          :class="{ active: activeTab === 'profile' }"
          @click="activeTab = 'profile'"
        >
          <IconLucideUser />
          个人设置
        </button>
      </nav>

      <button class="sidebar-back" @click="router.push('/home')">
        <IconLucideArrowLeft />
        返回知识库
      </button>
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
                class="pill-filter"
                :class="{ active: articleFilter === f.key }"
                @click="articleFilter = f.key"
              >
                {{ f.label }}
                <span class="pill-count" v-if="f.count != null">{{ f.count }}</span>
              </button>
            </div>

            <div class="article-list">
              <div v-for="a in filteredArticles" :key="a.id" class="article-row">
                <span class="article-title">{{ a.title || '未命名' }}</span>
                <div class="article-meta">
                  <span class="badge" :class="statusBadgeClass(a.status)">{{ statusLabel(a.status) }}</span>
                  <span class="badge" :class="visibilityBadgeClass(a.visibility)">{{ visibilityLabel(a.visibility) }}</span>
                </div>
                <span class="article-updated">{{ a.updatedAt }}</span>
                <div class="article-actions">
                  <button class="btn-ghost-sm" @click="editArticle(a.id)">编辑</button>
                  <button class="btn-ghost-sm danger" @click="handleDelete(a)">删除</button>
                </div>
              </div>
              <div v-if="articles.length === 0" class="empty-state">
                <p class="empty-state-text">暂无文章</p>
                <button class="btn-ghost-sm" @click="router.push('/write')">去写一篇</button>
              </div>
            </div>
          </template>

          <!-- ── 个人设置 ── -->
          <div v-if="activeTab === 'profile'" class="profile-form">
            <div class="form-group">
              <label class="form-label">头像</label>
              <div class="avatar-upload-row">
                <div class="avatar-preview">{{ avatarChar }}</div>
                <span class="btn-ghost-sm">上传头像</span>
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
import type { StudyArticle } from '~/types'
import IconLucideFileText from '~icons/lucide/file-text'
import IconLucideUser from '~icons/lucide/user'
import IconLucideArrowLeft from '~icons/lucide/arrow-left'

definePageMeta({ layout: 'blank' })

const router = useRouter()
const auth = useAuthStore()

const avatarChar = computed(() => auth.user?.nickname?.charAt(0) || '慢')

// ── Tab ──
const activeTab = ref<'articles' | 'profile'>('articles')

// ── Articles ──
const articles = ref<StudyArticle[]>([])
const articleFilter = ref('ALL')

const statusFilters = computed(() => {
  const list = articles.value
  return [
    { key: 'ALL', label: '全部', count: list.length },
    { key: 'DRAFT', label: '草稿', count: list.filter(a => a.status === 'DRAFT').length },
    { key: 'PUBLISHED', label: '已发布', count: list.filter(a => a.status === 'PUBLISHED').length },
    { key: 'REVIEWING', label: '审核中', count: list.filter(a => a.status === 'REVIEWING').length },
  ]
})

const filteredArticles = computed(() => {
  if (articleFilter.value === 'ALL') return articles.value
  return articles.value.filter(a => a.status === articleFilter.value)
})

function statusLabel(s: string) {
  const map: Record<string, string> = { DRAFT: '草稿', PUBLISHED: '已发布', REVIEWING: '审核中', REJECTED: '已退回', PRIVATE: '私密', BOOKMARKED: '收藏' }
  return map[s] || s
}

function statusBadgeClass(s: string) {
  return { DRAFT: 'badge-draft', PUBLISHED: 'badge-published', REVIEWING: 'badge-reviewing', REJECTED: 'badge-draft' }[s] || 'badge-draft'
}

function visibilityLabel(v?: string) {
  return v === 'PRIVATE' ? '私密' : '公开'
}

function visibilityBadgeClass(v?: string) {
  return v === 'PRIVATE' ? 'badge-private' : 'badge-public'
}

function editArticle(id: number) {
  router.push(`/write?articleId=${id}`)
}

async function handleDelete(a: StudyArticle) {
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

// ── Init ──
onMounted(async () => {
  const { listStudyArticles } = useArticle()
  articles.value = await listStudyArticles()
})
</script>

<style scoped>
/* ===== Layout ===== */
.body {
  display: flex; height: 100vh; overflow: hidden;
  background: var(--surface-soft);
}

/* ===== Sidebar ===== */
.sidebar {
  width: var(--sidebar-width); flex-shrink: 0;
  display: flex; flex-direction: column;
  background: var(--surface);
  border-right: 1px solid var(--hairline);
}

.sidebar-menu { display: flex; flex-direction: column; gap: 2px; padding: var(--spacing-sm) var(--spacing-md); }

.sidebar-menu-item {
  display: flex; align-items: center; gap: var(--spacing-xs);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--rounded-md);
  font-size: var(--body-sm); font-weight: var(--weight-regular); color: var(--charcoal);
  cursor: pointer; border: none; background: transparent; font-family: var(--font-sans);
  text-align: left; transition: background 0.15s var(--ease); line-height: 1.5;
}

.sidebar-menu-item:hover { background: var(--hairline-soft); }

.sidebar-menu-item.active {
  background: var(--canvas); font-weight: var(--weight-medium); color: var(--ink);
}

.sidebar-back {
  margin-top: auto; display: flex; align-items: center; gap: var(--spacing-xs);
  padding: var(--spacing-xs) var(--spacing-sm); border-radius: var(--rounded-md);
  font-size: var(--body-sm); color: var(--steel); cursor: pointer; border: none; font-family: var(--font-sans);
  background: transparent; transition: color 0.15s var(--ease);
  margin: var(--spacing-xl) var(--spacing-md);
}

.sidebar-back:hover { color: var(--ink); }

/* ===== Content ===== */
.content {
  flex: 1; display: flex; flex-direction: column;
  background: var(--canvas); overflow: hidden;
}

.content-body {
  flex: 1; overflow-y: auto;
  padding: var(--spacing-xl) var(--spacing-xxl);
  display: flex; justify-content: center;
}

.content-inner { width: 100%; max-width: 780px; }

/* ── Filters ── */
.filter-bar {
  display: flex; align-items: center; gap: var(--spacing-xs);
  margin-bottom: var(--spacing-md);
}

.pill-filter {
  padding: var(--spacing-xxs) var(--spacing-md);
  border-radius: var(--rounded-full);
  font-size: 14px; font-weight: 500; font-family: var(--font-sans);
  color: var(--steel); border: 1px solid var(--hairline);
  background: transparent; cursor: pointer; line-height: 1.5;
  transition: all 0.15s var(--ease);
}

.pill-filter:hover { color: var(--ink); border-color: var(--hairline-strong); }

.pill-filter.active {
  background: var(--ink-deep); color: var(--surface);
  border-color: var(--ink-deep);
}

.pill-count { font-weight: 400; color: var(--muted); margin-left: 2px; }

.pill-filter.active .pill-count { color: var(--stone); }

/* ── Article List ── */
.article-list { display: flex; flex-direction: column; gap: var(--spacing-xs); }

.article-row {
  display: flex; align-items: center; gap: var(--spacing-md);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--canvas);
  border-radius: var(--rounded-md);
  border: 1px solid var(--hairline);
  transition: box-shadow 0.15s var(--ease);
}

.article-row:hover { box-shadow: rgba(15,15,15,0.08) 0px 4px 12px 0px; }

.article-title {
  flex: 1; font-size: 14px; font-weight: 500; color: var(--ink);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.article-meta { display: flex; align-items: center; gap: var(--spacing-xs); flex-shrink: 0; }

.article-updated { font-size: 13px; color: var(--steel); white-space: nowrap; min-width: 80px; }

.article-actions { display: flex; gap: var(--spacing-xxs); flex-shrink: 0; }

/* ── Badges ── */
.badge {
  display: inline-flex; align-items: center;
  padding: 2px 8px; border-radius: var(--rounded-sm);
  font-size: 13px; font-weight: 600; line-height: 1.4; white-space: nowrap;
}

.badge-draft     { background: #f0eeec; color: var(--steel); }
.badge-published { background: #d9f3e1; color: var(--semantic-success); }
.badge-reviewing { background: #fef7d6; color: var(--semantic-warning); }
.badge-public    { background: #e6e0f5; color: #391c57; }
.badge-private   { background: #ffe8d4; color: #793400; }

/* ── Buttons ── */
.btn-ghost-sm {
  display: inline-flex; align-items: center;
  padding: 4px 10px; border-radius: var(--rounded-sm);
  font-size: 13px; font-weight: 500; color: var(--charcoal); font-family: var(--font-sans);
  background: transparent; border: none; cursor: pointer;
  transition: background 0.15s var(--ease); line-height: 1.4;
}

.btn-ghost-sm:hover { background: var(--surface); }
.btn-ghost-sm.danger { color: var(--semantic-error); }
.btn-ghost-sm.danger:hover { background: #fde8e8; }

/* ── Empty ── */
.empty-state { padding: var(--spacing-xxxl) var(--spacing-xl); text-align: center; }

.empty-state-text { font-size: 14px; color: var(--steel); margin-bottom: var(--spacing-sm); }

/* ── Profile ── */
.profile-form {
  display: flex; flex-direction: column; gap: var(--spacing-lg);
}

.form-group { display: flex; flex-direction: column; gap: var(--spacing-xxs); }

.form-label { font-size: 14px; font-weight: 500; color: var(--charcoal); }

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

.btn-primary {
  display: inline-flex; align-items: center; justify-content: center;
  height: 40px; padding: 0 18px; border: none; border-radius: var(--rounded-md);
  font-size: 14px; font-weight: 500; font-family: var(--font-sans);
  background: var(--primary); color: var(--on-primary); cursor: pointer;
  transition: background 0.15s var(--ease); line-height: 1; align-self: flex-start;
}

.btn-primary:hover { background: var(--primary-pressed); }

.profile-saved-hint { font-size: 13px; color: var(--semantic-success); }

/* ── Scrollbar ── */
.content-body::-webkit-scrollbar { width: 5px; }
.content-body::-webkit-scrollbar-track { background: transparent; }
.content-body::-webkit-scrollbar-thumb { background: transparent; border-radius: 3px; }
.content-body.scrolling::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.15); }
.content-body::-webkit-scrollbar-thumb:hover { background: rgba(0,0,0,0.25); }
</style>
