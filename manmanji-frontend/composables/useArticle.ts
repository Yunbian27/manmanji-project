// ============================================================
// composables/useArticle.ts — 文章 API 封装
// 对应后端 ArticleController 的三个端点
// ============================================================

import type { ArticleVO, ArticleCreateDTO } from '~/types'

export function useArticle() {
  const api = useApi()

  /** 获取单篇文章 */
  function getArticle(id: number): Promise<ArticleVO> {
    return api.get<ArticleVO>(`/api/articles/${id}`)
  }

  /** 创建文章（需要登录） */
  function createArticle(dto: ArticleCreateDTO): Promise<number> {
    return api.post<number>('/api/articles/create', dto)
  }

  /** AI 润色文章内容（需要登录） */
  function improve(content: string): Promise<string> {
    return api.post<string>('/api/articles/improve', content)
  }

  return { getArticle, createArticle, improve }
}
