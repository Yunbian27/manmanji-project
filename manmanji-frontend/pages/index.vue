<template>
  <div class="page">
    <TopNav />

    <!-- Mobile hamburger -->
    <button
      v-if="isMobile && !mobileSidebarOpen"
      class="hamburger-btn"
      @click="mobileSidebarOpen = true"
    >
      <IconMenu :size="24" />
    </button>

    <!-- Mobile overlay -->
    <div
      v-if="isMobile && mobileSidebarOpen"
      class="mobile-overlay"
      @click="mobileSidebarOpen = false"
    />

    <AppLayout>
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

      <!-- Main content area -->
      <template v-if="articleLoading">
        <div class="skeleton-article">
          <div class="skeleton-title-bar" />
          <div class="skeleton-meta" />
          <div v-for="i in 8" :key="i" class="skeleton-line" :style="{ width: `${40 + Math.random() * 60}%` }" />
        </div>
      </template>

      <template v-else-if="articleError">
        <div class="error-state">
          <p class="error-msg">{{ articleError }}</p>
          <AppButton variant="primary" @click="retryLoad">重试</AppButton>
        </div>
      </template>

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

      <!-- Empty state -->
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

      <template #right-sidebar>
        <RightSidebar
          v-if="!isTablet"
          :toc-items="tocItems"
          :active-section-id="activeSectionId"
        />
      </template>
    </AppLayout>

    <AiAssistant />
  </div>
</template>

<script setup lang="ts">
import type { ArticleVO, TocItem, CommentVO } from '~/types'

const { isMobile, isTablet } = useDevice()
const mobileSidebarOpen = ref(false)

const folderStore = useFolderStore()
const authStore = useAuthStore()

// Article
const currentArticleId = ref<number | undefined>(undefined)
const currentArticle = ref<ArticleVO | null>(null)
const articleLoading = ref(false)
const articleError = ref<string | null>(null)

// TOC
const tocItems = ref<TocItem[]>([])
const activeSectionId = ref<string | undefined>()

// Comments
const comments = ref<CommentVO[]>([])

// Read time estimate
const readTime = computed(() => {
  if (!currentArticle.value?.content) return 0
  const wordCount = currentArticle.value.content.length
  return Math.max(1, Math.round(wordCount / 500))
})

// Mock data for initial display
const mockArticle: ArticleVO = {
  id: 1,
  title: 'Java 21 虚拟线程实战：从入门到性能调优',
  slug: 'java-virtual-threads',
  content: `## 引言

Java 21 正式发布了虚拟线程（Virtual Threads），这是 Project Loom 的核心成果。虚拟线程为 Java 生态带来了**轻量级并发**的全新范式。

## 什么是虚拟线程

虚拟线程是 JVM 管理的轻量级线程，**不直接映射到操作系统线程**。它们由 JVM 在少量 OS 线程（称为载体线程）上调度。

> 虚拟线程的核心优势在于：当你需要处理百万级并发连接时，不需要创建百万个平台线程。每个虚拟线程的内存开销仅几百字节，而平台线程则需要约 1MB。

## 创建虚拟线程

### 基本用法

以下是创建虚拟线程的几种方式：

\`\`\`java
// 方式一：通过 Thread.ofVirtual()
Thread vThread = Thread.ofVirtual()
    .name("virtual-worker")
    .start(() -> {
        System.out.println("Running in virtual thread");
    });

// 方式二：通过 Executors
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> fetchData());
}
\`\`\`

### 与平台线程的对比

\`\`\`java
// 平台线程（传统方式）
ExecutorService pool = Executors.newFixedThreadPool(200);
for (int i = 0; i < 10000; i++) {
    pool.submit(() -> process());
}

// 虚拟线程（新方式）
ExecutorService vPool = Executors.newVirtualThreadPerTaskExecutor();
for (int i = 0; i < 10000; i++) {
    vPool.submit(() -> process());
}
\`\`\`

## 性能测试

| 指标 | 平台线程(200) | 虚拟线程(10000) |
|------|--------------|-----------------|
| QPS | 12,400 | 126,000 |
| P99 延迟 | 450ms | 12ms |
| 内存占用 | 1.2GB | 280MB |
| 线程创建耗时 | 2.1s | 0.3s |

## 最佳实践

1. **不要池化虚拟线程** — 用完即弃
2. **避免长时间持有 synchronized** — 会 pin 住载体线程
3. **使用 \`Semaphore\` 限流** — 控制并发数
4. **监控载体线程** — 使用 JFR 观察行为

### 限流示例

\`\`\`java
Semaphore semaphore = new Semaphore(100);
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100000; i++) {
        executor.submit(() -> {
            semaphore.acquire();
            try {
                callExternalApi();
            } finally {
                semaphore.release();
            }
        });
    }
}
\`\`\`

## 总结

虚拟线程让 Java 的并发编程变得更加简单。\`newFixedThreadPool(n)\` 变成了 \`newVirtualThreadPerTaskExecutor()\`，这就是 Java 并发的未来。`,
  summary: 'Java 21 虚拟线程实战指南，从基本用法到性能调优',
  coverUrl: null,
  authorId: 1,
  folderId: 1,
  categoryId: null,
  status: 'PUBLISHED',
  sourceType: 'MANUAL',
  sourcePrompt: null,
  viewCount: 2300,
  likeCount: 186,
  commentCount: 12,
  bookmarkCount: 45,
  isOriginal: true,
  sourceUrl: null,
  createdAt: '2026-05-10 10:30:00',
  updatedAt: '2026-05-12 14:20:00',
  publishedAt: '2026-05-10 10:30:00',
}

// Load mock data
onMounted(() => {
  authStore.hydrate()
  folderStore.fetchFolders()
  // Load mock article for demonstration
  currentArticle.value = mockArticle
  currentArticleId.value = mockArticle.id
})

async function selectArticle(id: number) {
  currentArticleId.value = id
  mobileSidebarOpen.value = false
  articleLoading.value = true
  articleError.value = null
  try {
    const { getArticle } = useArticle()
    currentArticle.value = await getArticle(id)
  } catch {
    // Use mock article as fallback
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

function onLike() { /* TODO */ }
function onBookmark() { /* TODO */ }
function onShare() { /* TODO */ }

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

function scrollToComments() {
  document.getElementById('comments')?.scrollIntoView({ behavior: 'smooth' })
}

function onNewFolder() { /* TODO */ }
function onNewArticle() { /* TODO */ }
</script>

<style scoped>
.page { min-height: 100vh; }

.hamburger-btn {
  display: none;
  position: fixed;
  top: calc(var(--nav-height) + 8px);
  left: 12px;
  z-index: calc(var(--z-nav) + 1);
  width: 36px;
  height: 36px;
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

/* Skeleton loading */
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

/* States */
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
.empty-state h3 { font-size: var(--text-title-md); color: var(--ink); margin-bottom: var(--space-xs); }
.empty-state p { font-size: var(--text-caption); color: var(--muted-soft); }
</style>
