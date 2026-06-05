import type { ArticleVO, ArticleSaveDTO, ArticlePublishDTO, StudyArticle } from '~/types'

// Mock 数据 — 后端 API 就绪后替换为真实请求
const mockArticles: StudyArticle[] = [
  { id: 1, title: 'Java 虚拟线程：从原理到实践', status: 'PUBLISHED', visibility: 'PUBLIC', tags: ['Java', '并发'], updatedAt: '2026-05-30' },
  { id: 2, title: 'PostgreSQL 查询优化：索引策略与执行计划分析', status: 'PUBLISHED', visibility: 'PRIVATE', tags: ['PostgreSQL', '数据库'], updatedAt: '2026-05-28' },
  { id: 3, title: 'Kubernetes 集群中服务网格的选型与实践', status: 'PUBLISHED', visibility: 'PUBLIC', tags: ['Kubernetes', '分布式'], updatedAt: '2026-05-25' },
  { id: 4, title: '分布式系统中的一致性模型取舍之道', status: 'PUBLISHED', visibility: 'PRIVATE', tags: ['分布式', '一致性', 'Raft'], updatedAt: '2026-06-01' },
  { id: 5, title: 'Spring Boot 4.0 迁移指南', status: 'PUBLISHED', visibility: 'PUBLIC', tags: ['Spring Boot'], updatedAt: '2026-05-20' },
  { id: 6, title: '前端性能监控：Web Vitals 指标详解与优化策略', status: 'UNPUBLISHED', tags: ['前端', '性能优化'], updatedAt: '2026-05-18' },
  { id: 7, title: '深入理解 Go 语言的调度器：GMP 模型全景解析', status: 'PUBLISHED', visibility: 'PUBLIC', tags: ['Go', '并发'], updatedAt: '2026-05-15' },
  { id: 8, title: '代码审查的艺术：团队实践指南', status: 'UNPUBLISHED', tags: ['工程实践'], updatedAt: '2026-05-12' },
  { id: 9, title: 'Redis 源码阅读笔记：事件驱动模型与 IO 多路复用', status: 'PUBLISHED', visibility: 'PUBLIC', tags: ['Redis', '源码'], updatedAt: '2026-05-10' },
  { id: 10, title: '设计模式在微服务架构中的实践应用', status: 'UNPUBLISHED', tags: ['设计模式', '微服务'], updatedAt: '2026-05-08' },
  { id: 11, title: 'Linux 内核调度器 CFS 源码分析', status: 'PUBLISHED', visibility: 'PUBLIC', tags: ['Linux', '源码'], updatedAt: '2026-05-05' },
  { id: 12, title: 'MySQL 索引优化：从 B+ 树到覆盖索引', status: 'PUBLISHED', visibility: 'PRIVATE', tags: ['数据库'], updatedAt: '2026-05-02' },
  { id: 13, title: 'Docker 容器网络原理与跨主机通信', status: 'UNPUBLISHED', tags: ['Docker', '网络'], updatedAt: '2026-04-28' },
  { id: 14, title: 'Python 异步编程：asyncio 事件循环详解', status: 'PUBLISHED', visibility: 'PUBLIC', tags: ['Python', '并发'], updatedAt: '2026-04-25' },
  { id: 15, title: 'HTTPS 证书链验证与中间人攻击防御', status: 'PUBLISHED', visibility: 'PRIVATE', tags: ['安全'], updatedAt: '2026-04-20' },
  { id: 16, title: 'TypeScript 类型体操：infer 与条件类型', status: 'PUBLISHED', visibility: 'PUBLIC', tags: ['TypeScript', '前端'], updatedAt: '2026-04-15' },
  { id: 17, title: 'Elasticsearch 倒排索引与相关性评分', status: 'PUBLISHED', visibility: 'PRIVATE', tags: ['Elasticsearch', '搜索'], updatedAt: '2026-04-10' },
  { id: 18, title: 'RabbitMQ 消息可靠性投递与死信队列', status: 'UNPUBLISHED', tags: ['消息队列'], updatedAt: '2026-04-05' },
  { id: 19, title: 'Nginx 反向代理与负载均衡策略对比', status: 'PUBLISHED', visibility: 'PUBLIC', tags: ['Nginx', '运维'], updatedAt: '2026-04-01' },
]

export function useArticle() {
  const api = useApi()

  /** 获取书房文章列表（mock，后端 API 就绪后切换） */
  function listStudyArticles(): Promise<StudyArticle[]> {
    return Promise.resolve(mockArticles)
  }

  /** 获取单篇文章 */
  function getArticle(id: number): Promise<ArticleVO> {
    return api.get<ArticleVO>(`/api/articles/${id}`)
  }

  /** 创建文章（folderId 可选，不传则无文件夹） */
  function createArticle(folderId?: number): Promise<number> {
    if (folderId != null) {
      return api.post<number>(`/api/articles/create/${folderId}`)
    }
    return api.post<number>('/api/articles/create')
  }

  /** 保存文章（新建返回文章ID，更新返回原ID） */
  function saveArticle(dto: ArticleSaveDTO): Promise<number> {
    return api.put<number>('/api/articles/save', dto)
  }

  /** 发布文章（新建返回文章ID，更新返回原ID） */
  function publishArticle(dto: ArticlePublishDTO): Promise<number> {
    return api.put<number>('/api/articles/publish', dto)
  }

  /** AI 润色文章内容（需要登录） */
  function improve(content: string): Promise<string> {
    return api.post<string>('/api/articles/improve', content)
  }

  /** 上传图片 */
  function uploadImage(file: File): Promise<string> {
    const formData = new FormData()
    formData.append('file', file)
    return api.uploadFormData<string>('/api/storage/image', formData)
  }

  return { getArticle, createArticle, saveArticle, publishArticle, improve, uploadImage, listStudyArticles }
}
