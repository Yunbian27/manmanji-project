// ============================================================
// data/mockArticle.ts — 演示用文章数据
// 后端未启动或数据为空时使用，展示完整的 Markdown 排版效果
//
// 为什么 mock 数据放在独立文件中？
// 因为 Markdown 内容包含代码块（```），直接写在 Vue SFC 的
// 模板字面量中会导致反引号转义与 Vue 编译器产生冲突
// ============================================================

import type { ArticleVO } from '~/types'

export const mockArticle: ArticleVO = {
  id: 1,
  title: 'Java 21 虚拟线程实战：从入门到性能调优',
  slug: 'java-virtual-threads',
  // Markdown 内容：使用数组 join 替代模板字面量，避免反引号转义问题
  content: [
    '## 引言', '',
    'Java 21 正式发布了虚拟线程（Virtual Threads），这是 Project Loom 的核心成果。', '',
    '## 什么是虚拟线程', '',
    '虚拟线程是 JVM 管理的轻量级线程，**不直接映射到操作系统线程**。',
    '> 虚拟线程的核心优势：每个虚拟线程的内存开销仅几百字节，而平台线程需要约 1MB。', '',
    '## 创建虚拟线程', '',
    '```java',
    'Thread vThread = Thread.ofVirtual().name("worker").start(() -> {',
    '    System.out.println("Running in virtual thread");',
    '});',
    'try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {',
    '    executor.submit(() -> fetchData());',
    '}',
    '```', '',
    '## 性能测试', '',
    '| 指标 | 平台线程(200) | 虚拟线程(10000) |',
    '|------|--------------|-----------------|',
    '| QPS | 12,400 | 126,000 |',
    '| P99 延迟 | 450ms | 12ms |',
    '| 内存占用 | 1.2GB | 280MB |',
    '| 线程创建耗时 | 2.1s | 0.3s |', '',
    '## 总结', '',
    '虚拟线程让 Java 的并发编程更加简单。`newFixedThreadPool(n)` 变成了 `newVirtualThreadPerTaskExecutor()`。',
  ].join('\n'),
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
