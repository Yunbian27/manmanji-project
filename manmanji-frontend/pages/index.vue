<!--
  pages/index.vue — 主页面（文章阅读页）
  Nuxt 根据文件名自动路由：pages/index.vue → URL "/"

  这是整个项目最复杂的组件，负责：
  1. 组合所有子组件形成完整页面
  2. 管理五层状态（loading / error / empty / data / mobile toggle）
  3. 协调组件间通信（选文章 → 更新 TOC → 更新右侧栏）

  Vue 3 关键概念在本文件中的应用：
  - <template> 中使用 v-if / v-else-if / v-else 条件渲染
  - <script setup> 中使用 ref / computed / onMounted / watch
  - 组件通过 props 向下传数据，通过 events 向上传事件
  - 命名 slot (#left-sidebar, #right-sidebar) 向 AppLayout 传递内容
  - Pinia store (useAuthStore, useFolderStore) 全局状态共享
-->
<template>
  <div class="page">
    <!-- ===== 顶部导航栏 ===== -->
    <TopNav @write-article="openEditor" />

    <!-- ===== 全屏 Markdown 编辑器 ===== -->
    <EditorView v-if="editorOpen" @close="closeEditor" @published="onPublished" />

    <!-- ===== 手机端汉堡菜单按钮 ===== -->
    <button
      v-if="!editorOpen && isMobile && !mobileSidebarOpen"
      class="hamburger-btn"
      @click="mobileSidebarOpen = true"
    >
      <IconMenu :size="24" />
    </button>

    <!-- ===== 手机端半透明遮罩层 ===== -->
    <div
      v-if="!editorOpen && isMobile && mobileSidebarOpen"
      class="mobile-overlay"
      @click="mobileSidebarOpen = false"
    />

    <!-- ===== 三栏布局容器 ===== -->
    <AppLayout v-if="!editorOpen">
      <!-- 左侧栏 -->
      <template #left-sidebar>
        <LeftSidebar
          :folders="folderStore.folders"
          :loading="folderStore.loading"
          :error="folderStore.error"
          :current-article-id="currentArticleId"
          :mobile-open="mobileSidebarOpen"
          @select-article="selectArticle"
          @new-folder="onNewFolder"
          @new-article="onNewArticle"
        />
      </template>

      <!-- === 主内容区：五层条件渲染 === -->

      <!-- 状态1：加载中 — 骨架屏 -->
      <template v-if="articleLoading">
        <div class="skeleton-article">
          <div class="skeleton-title-bar" />
          <div class="skeleton-meta" />
          <div v-for="i in 8" :key="i" class="skeleton-line" :style="{ width: `${40 + Math.random() * 60}%` }" />
        </div>
      </template>

      <!-- 状态2：加载出错 — 错误 + 重试按钮 -->
      <template v-else-if="articleError">
        <div class="error-state">
          <p class="error-msg">{{ articleError }}</p>
          <AppButton variant="primary" @click="retryLoad">重试</AppButton>
        </div>
      </template>

      <!-- 状态3：正常 — 渲染完整文章 -->
      <template v-else-if="currentArticle">
        <ArticleHeader
          :title="currentArticle.title"
          :author="{ id: currentArticle.authorId, username: '', nickname: '作者', avatarUrl: null }"
          :category="null"
          :tags="[]"
          :published-at="currentArticle.publishedAt"
          :view-count="currentArticle.viewCount"
          :read-time="readTime"
        />
        <ArticleBody
          :content="currentArticle.content"
          @toc-updated="tocItems = $event"
        />
        <ArticleActions
          :like-count="currentArticle.likeCount"
          :comment-count="currentArticle.commentCount"
          :bookmark-count="currentArticle.bookmarkCount"
          :liked="false"
          :bookmarked="false"
          @like="onLike"
          @bookmark="onBookmark"
          @share="onShare"
          @comment-click="scrollToComments"
        />
        <CommentSection
          :comments="comments"
          @add-comment="onAddComment"
          @reply="onReply"
        />
      </template>

      <!-- 状态4：空 — 未选择文章 -->
      <template v-else>
        <div class="empty-state">
          <div class="empty-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" opacity="0.3">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
              <line x1="16" y1="13" x2="8" y2="13" />
              <line x1="16" y1="17" x2="8" y2="17" />
            </svg>
          </div>
          <h3>选择一篇文章</h3>
          <p>从左侧列表选择一篇文章开始阅读</p>
        </div>
      </template>

      <!-- 右侧目录栏：平板以下隐藏 -->
      <template #right-sidebar>
        <RightSidebar
          v-if="!isTablet"
          :toc-items="tocItems"
          :active-section-id="activeSectionId"
        />
      </template>
    </AppLayout>

    <!-- AI 浮动助手 -->
    <AiAssistant v-if="!editorOpen" />
  </div>
</template>

<script setup lang="ts">
import type { ArticleVO, TocItem, CommentVO } from '~/types'
// 演示用 mock 数据 — 从独立文件导入，避免模板字面量与 Vue SFC 编译器的转义冲突
import { mockArticle } from '~/data/mockArticle'

// ---- 设备检测（响应式） ----
const { isMobile, isTablet } = useDevice()
const mobileSidebarOpen = ref(false)
const editorOpen = ref(false)

// ---- Pinia 全局状态 ----
const folderStore = useFolderStore()
const authStore = useAuthStore()

// ---- 文章状态 ----
const currentArticleId = ref<number | undefined>(undefined)
const currentArticle = ref<ArticleVO | null>(null)
const articleLoading = ref(false)
const articleError = ref<string | null>(null)

// ---- TOC 目录（由 ArticleBody emit 更新） ----
const tocItems = ref<TocItem[]>([])
const activeSectionId = ref<string | undefined>()

// ---- 评论（前端模拟，后端控制器暂未实现） ----
const comments = ref<CommentVO[]>([])

// ---- 计算属性 ----
/** 预估阅读时长 = 总字符数 / 500（中文平均阅读速度），最少显示 1 分钟 */
const readTime = computed(() => {
  if (!currentArticle.value?.content) return 0
  return Math.max(1, Math.round(currentArticle.value.content.length / 500))
})

// ---- 生命周期 ----
onMounted(() => {
  authStore.hydrate()
  folderStore.fetchFolders()
  // 加载演示文章（实际应用中应根据路由参数加载）
  currentArticle.value = mockArticle
  currentArticleId.value = mockArticle.id
})

// ---- 事件处理 ----
/** 用户点击左侧栏某篇文章 */
async function selectArticle(id: number) {
  currentArticleId.value = id
  mobileSidebarOpen.value = false
  articleLoading.value = true
  articleError.value = null
  try {
    const { getArticle } = useArticle()
    currentArticle.value = await getArticle(id)
  } catch {
    if (id === mockArticle.id) {
      currentArticle.value = mockArticle
    } else {
      articleError.value = '文章加载失败'
    }
  } finally {
    articleLoading.value = false
  }
}

function retryLoad() {
  if (currentArticleId.value) selectArticle(currentArticleId.value)
}

function onLike() { /* TODO: 调用后端点赞 API */ }
function onBookmark() { /* TODO: 调用后端收藏 API */ }
function onShare() { /* TODO: 调用浏览器分享 API 或复制链接 */ }

/** 添加评论（前端模拟，实际应调用后端 API） */
function onAddComment(content: string) {
  comments.value.push({
    id: Date.now(),
    articleId: currentArticleId.value || 0,
    author: { id: 1, username: 'user', nickname: '我', avatarUrl: null },
    content,
    parentId: null,
    likeCount: 0,
    createdAt: new Date().toISOString(),
  })
}

function onReply(_commentId: number) { /* TODO */ }

/** 滚动到评论区（由 ArticleActions 的 @comment-click 触发） */
function scrollToComments() {
  document.getElementById('comments')?.scrollIntoView({ behavior: 'smooth' })
}

function onNewFolder() { /* TODO: 打开新建文件夹弹窗 */ }
function onNewArticle() { openEditor() }

function openEditor() {
  editorOpen.value = true
}

function closeEditor() {
  editorOpen.value = false
}

function onPublished(_articleId: number) {
  editorOpen.value = false
  // 刷新左侧文章列表
  folderStore.fetchFolders()
}
</script>

<style scoped>
.page { min-height: 100vh; }

/* === 手机端汉堡菜单按钮 === */
.hamburger-btn {
  display: none;
  position: fixed;
  top: calc(var(--nav-height) + 8px);
  left: 12px;
  z-index: calc(var(--z-nav) + 1);
  width: 36px; height: 36px;
  border-radius: var(--radius-md);
  border: 1px solid var(--hairline);
  background: var(--surface-card);
  color: var(--ink);
  cursor: pointer;
  align-items: center;
  justify-content: center;
}

.mobile-overlay {
  display: none;
  position: fixed;
  inset: 0;
  top: var(--nav-height);
  background: rgba(0, 0, 0, 0.5);
  z-index: calc(var(--z-nav) - 1);
}

@media (max-width: 768px) {
  .hamburger-btn { display: flex; }
  .mobile-overlay { display: block; }
}

/* === 骨架屏（loading 状态） === */
.skeleton-article { max-width: 780px; }
.skeleton-title-bar {
  height: 36px;
  background: var(--surface-elevated);
  border-radius: var(--radius-sm);
  width: 70%;
  margin-bottom: var(--space-lg);
}
.skeleton-meta {
  height: 14px;
  background: var(--surface-elevated);
  border-radius: var(--radius-sm);
  width: 40%;
  margin-bottom: var(--space-xxl);
}
.skeleton-line {
  height: 16px;
  background: var(--surface-elevated);
  border-radius: var(--radius-sm);
  margin-bottom: var(--space-md);
}

/* === 错误状态 === */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-md);
  padding: var(--space-xxl);
  text-align: center;
}
.error-msg { color: var(--muted); font-size: var(--text-body); }

/* === 空状态（未选文章） === */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-section) 0;
  color: var(--muted);
  text-align: center;
}
.empty-icon { margin-bottom: var(--space-md); color: var(--muted-soft); }
.empty-state h3 {
  font-size: var(--text-title-md);
  color: var(--ink);
  margin-bottom: var(--space-xs);
}
.empty-state p {
  font-size: var(--text-caption);
  color: var(--muted-soft);
}
</style>
