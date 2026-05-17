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
    <!-- ===== 手机端汉堡菜单按钮 ===== -->
    <button
      v-if="isMobile && !mobileSidebarOpen"
      class="hamburger-btn"
      @click="mobileSidebarOpen = true"
    >
      <IconMenu :size="24" />
    </button>

    <!-- ===== 手机端半透明遮罩层 ===== -->
    <div
      v-if="isMobile && mobileSidebarOpen"
      class="mobile-overlay"
      @click="mobileSidebarOpen = false"
    />

    <!-- ===== 三栏布局容器 ===== -->
    <AppLayout>
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
    <AiAssistant />

    <!-- 右键上下文菜单 -->
    <ContextMenu
      :visible="contextVisible"
      :items="contextItems"
      :position="contextPosition"
      @select="onContextSelect"
      @close="contextVisible = false"
    />

    <!-- 文本输入弹窗（新建文件夹 / 重命名文件夹 / 重命名文章） -->
    <PromptModal
      :visible="prompt.visible"
      :title="prompt.title"
      :placeholder="prompt.placeholder"
      :confirm-text="prompt.confirmText"
      :loading="prompt.loading"
      :error="prompt.error"
      @confirm="onPromptConfirm"
      @cancel="onPromptCancel"
    />

    <!-- 确认删除弹窗 -->
    <ConfirmModal
      :visible="confirm.visible"
      :title="confirm.title"
      :message="confirm.message"
      :confirm-text="confirm.confirmText"
      :loading="confirm.loading"
      :error="confirm.error"
      @confirm="onConfirmConfirm"
      @cancel="onConfirmCancel"
    />
  </div>
</template>

<script setup lang="ts">
import type { ArticleVO, TocItem, CommentVO } from '~/types'
// 演示用 mock 数据 — 从独立文件导入，避免模板字面量与 Vue SFC 编译器的转义冲突
import { mockArticle } from '~/data/mockArticle'
import type { MenuItem } from '~/components/common/ContextMenu.vue'

definePageMeta({ middleware: 'auth-client' })

// ---- 设备检测（响应式） ----
const { isMobile, isTablet } = useDevice()
const mobileSidebarOpen = ref(false)

// ---- Pinia 全局状态 ----
const folderStore = useFolderStore()

// ---- 右键上下文菜单状态 ----
const contextVisible = ref(false)
const contextPosition = ref({ x: 0, y: 0 })
const contextItems = ref<MenuItem[]>([])
let contextTargetId: number | undefined

function openContextMenu(event: MouseEvent, type: 'blank' | 'folder' | 'article', targetId?: number) {
  contextTargetId = targetId

  switch (type) {
    case 'blank':
      contextItems.value = [
        { key: 'new-folder', label: '新建文件夹' },
        { key: 'new-article', label: '新建文章' },
      ]
      break
    case 'folder':
      contextItems.value = [
        { key: 'new-subfolder', label: '新建子文件夹' },
        { key: 'new-article-in-folder', label: '新建文章' },
        { key: 'rename-folder', label: '重命名' },
        { key: 'delete-folder', label: '删除', danger: true },
      ]
      break
    case 'article':
      contextItems.value = [
        { key: 'rename-article', label: '重命名' },
        { key: 'delete-article', label: '删除', danger: true },
        { key: 'copy-link', label: '复制链接' },
      ]
      break
  }

  contextPosition.value = { x: event.clientX, y: event.clientY }
  contextVisible.value = true
}

function onContextSelect(key: string) {
  contextVisible.value = false

  switch (key) {
    case 'new-folder':
      handleCreateFolder()
      break
    case 'new-subfolder':
      handleCreateFolder(contextTargetId)
      break
    case 'new-article':
      handleNewArticle()
      break
    case 'new-article-in-folder':
      handleNewArticle(contextTargetId)
      break
    case 'rename-folder':
      if (contextTargetId) handleRenameFolder(contextTargetId)
      break
    case 'rename-article':
      if (contextTargetId) handleRenameArticle(contextTargetId)
      break
    case 'delete-folder':
      if (contextTargetId) handleDeleteFolder(contextTargetId)
      break
    case 'delete-article':
      if (contextTargetId) handleDeleteArticle(contextTargetId)
      break
    case 'copy-link':
      if (contextTargetId) handleCopyLink(contextTargetId)
      break
  }
}

// ---- 文本输入弹窗状态 ----
const prompt = reactive({
  visible: false,
  title: '',
  placeholder: '',
  confirmText: '确定',
  loading: false,
  error: null as string | null,
})

let promptResolve: ((value: string | null) => void) | null = null

function showPrompt(config: { title: string; placeholder: string; confirmText?: string }): Promise<string | null> {
  prompt.title = config.title
  prompt.placeholder = config.placeholder
  prompt.confirmText = config.confirmText || '确定'
  prompt.loading = false
  prompt.error = null
  prompt.visible = true
  return new Promise((resolve) => {
    promptResolve = resolve
  })
}

function onPromptConfirm(value: string) {
  promptResolve?.(value)
}

function onPromptCancel() {
  promptResolve?.(null)
  prompt.visible = false
}

// ---- 确认弹窗状态 ----
const confirm = reactive({
  visible: false,
  title: '',
  message: '',
  confirmText: '删除',
  loading: false,
  error: null as string | null,
})

let confirmResolve: (() => void) | null = null

function showConfirmDialog(config: { title: string; message: string; confirmText?: string }): Promise<void> {
  confirm.title = config.title
  confirm.message = config.message
  confirm.confirmText = config.confirmText || '删除'
  confirm.loading = false
  confirm.error = null
  confirm.visible = true
  return new Promise((resolve) => {
    confirmResolve = resolve
  })
}

function onConfirmConfirm() {
  confirmResolve?.()
}

function onConfirmCancel() {
  confirm.visible = false
}

async function handleCreateFolder(parentId?: number) {
  const name = await showPrompt({ title: '新建文件夹', placeholder: '输入文件夹名称' })
  if (!name) return
  prompt.loading = true
  prompt.error = null
  try {
    await folderStore.createFolder(name, parentId)
    prompt.visible = false
  } catch (e) {
    prompt.error = e instanceof Error ? e.message : '创建文件夹失败'
  } finally {
    prompt.loading = false
  }
}

async function handleRenameFolder(id: number) {
  const name = await showPrompt({ title: '重命名文件夹', placeholder: '输入新名称' })
  if (!name) return
  prompt.loading = true
  prompt.error = null
  try {
    await folderStore.renameFolder(id, name)
    prompt.visible = false
  } catch (e) {
    prompt.error = e instanceof Error ? e.message : '重命名失败'
  } finally {
    prompt.loading = false
  }
}

async function handleDeleteFolder(id: number) {
  await showConfirmDialog({ title: '删除文件夹', message: '确定删除此文件夹？子文件夹和文章也会被删除。' })
  confirm.loading = true
  confirm.error = null
  try {
    await folderStore.deleteFolder(id)
    confirm.visible = false
  } catch (e) {
    confirm.error = e instanceof Error ? e.message : '删除失败'
  } finally {
    confirm.loading = false
  }
}

function handleNewArticle(folderId?: number) {
  if (folderId) {
    navigateTo(`/write?folderId=${folderId}`)
  } else {
    navigateTo('/write')
  }
}

async function handleRenameArticle(id: number) {
  const title = await showPrompt({ title: '重命名文章', placeholder: '输入新标题' })
  if (!title) return
  prompt.loading = true
  prompt.error = null
  try {
    await folderStore.renameArticle(id, title)
    prompt.visible = false
  } catch (e) {
    prompt.error = e instanceof Error ? e.message : '重命名失败'
  } finally {
    prompt.loading = false
  }
}

async function handleDeleteArticle(id: number) {
  await showConfirmDialog({ title: '删除文章', message: '确定删除此文章？' })
  confirm.loading = true
  confirm.error = null
  try {
    await folderStore.deleteArticle(id)
    confirm.visible = false
    if (currentArticleId.value === id) {
      currentArticle.value = null
      currentArticleId.value = undefined
    }
  } catch (e) {
    confirm.error = e instanceof Error ? e.message : '删除失败'
  } finally {
    confirm.loading = false
  }
}

function handleCopyLink(articleId: number) {
  const url = `${window.location.origin}?article=${articleId}`
  navigator.clipboard.writeText(url).then(() => {
    // 复制成功
  }).catch(() => {
    const ta = document.createElement('textarea')
    ta.value = url
    ta.style.position = 'fixed'
    ta.style.opacity = '0'
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
  })
}

provide('openContextMenu', openContextMenu)

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

function onNewFolder() { handleCreateFolder() }
function onNewArticle() { navigateTo('/write') }
</script>

<style scoped>

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
